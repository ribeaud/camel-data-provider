package org.apache.camel.component.dataprovider;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledBatchPollingConsumer;
import org.apache.camel.util.CastUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ScheduledBatchPollingConsumer} extension for {@link IDataProvider}.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class DataProviderConsumer extends ScheduledBatchPollingConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(DataProviderConsumer.class);

    private volatile Range<Integer> range;
    // We need this to avoid multiple INFO logs when all batches have been processed
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final Lock rangeLock = new ReentrantLock();

    DataProviderConsumer(DataProviderEndpoint dataProviderEndpoint, Processor processor) {
        super(dataProviderEndpoint, processor);
    }

    @Override
    protected void doStart() throws Exception {
        IDataProvider<?> dataProvider = getEndpoint().getDataProvider();
        int size = dataProvider.getSize();
        rangeLock.lock();
        try {
            range = Range.closedOpen(0, Math.min(size, maxMessagesPerPoll));
        } finally {
            rangeLock.unlock();
        }
        LogUtils.info(LOG, () -> String.format("[%s] Preparing to handle %d partition(s) (%d / %d).",
                dataProvider.getName(),
                (int) Math.ceil((float) size / maxMessagesPerPoll), size, maxMessagesPerPoll));
        super.doStart();
    }

    @Override
    public int processBatch(Queue<Object> exchanges) throws Exception {
        assert exchanges != null : "Unspecified exchanges";
        final int batchSize = exchanges.size();
        for (int index = 0; index < batchSize && isBatchAllowed(); index++) {
            Exchange exchange = (Exchange) exchanges.poll();
            // Add current index and total as properties
            exchange.setProperty(Exchange.BATCH_INDEX, index);
            exchange.setProperty(Exchange.BATCH_SIZE, batchSize);
            exchange.setProperty(Exchange.BATCH_COMPLETE, index == batchSize - 1);
            // We are handling the last exchange if the last batch is complete
            exchange.setProperty(DataProviderConstants.LAST_EXCHANGE, exchange.getProperty(Exchange.BATCH_COMPLETE, Boolean.class)
                    && exchange.getProperty(DataProviderConstants.LAST_BATCH, Boolean.class));
            // Update pending number of exchanges
            pendingExchanges = batchSize - index - 1;
            // Process the current exchange
            getProcessor().process(exchange);
            Exception exception = exchange.getException();
            if (exception != null) {
                // We expect the exception handler to log the exception. No need to do it again here.
                getExceptionHandler().handleException(
                        String.format("Error while processing exchange located at index %d.", index), exchange,
                        exception);
            }
        }
        return batchSize;
    }

    @Override
    protected int poll() throws Exception {
        DataProviderEndpoint endpoint = getEndpoint();
        IDataProvider<?> dataProvider = endpoint.getDataProvider();
        final Queue<Exchange> exchanges = new LinkedList<>();
        rangeLock.lock();
        try {
            Range<Integer> range = this.range;
            if (range.isEmpty()) {
                if (!finished.getAndSet(true)) {
                    LogUtils.info(LOG, () -> String.format("[%s] Nothing to poll. Last range handled.", dataProvider.getName()));
                }
                return 0;
            }
            // Process current range
            LogUtils.info(LOG, () -> String.format("[%s] Handling range '%s'.", dataProvider.getName(), range));
            int index = range.lowerEndpoint();
            int size = dataProvider.getSize();
            for (Object item : dataProvider.partition(range)) {
                Exchange exchange = endpoint.createExchange();
                exchange.setProperty(DataProviderConstants.INDEX, index++);
                exchange.setProperty(DataProviderConstants.SIZE, size);
                exchange.setProperty(DataProviderConstants.LAST_BATCH, range.upperEndpoint() == size);
                exchange.getIn().setBody(item);
                exchanges.add(exchange);
            }
            // Prepare next range
            Range<Integer> nextRange = createNextRange(range.upperEndpoint(), size);
            LogUtils.debug(LOG, () -> String.format("[%s] Next range will be '%s'.", dataProvider.getName(), nextRange));
            this.range = nextRange;
        } finally {
            rangeLock.unlock();
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        int processBatch = processBatch(CastUtils.cast(exchanges));
        stopwatch.stop();
        LogUtils.debug(LOG, () -> String.format("[%s] Processing of %d exchanges took '%s'.", dataProvider.getName(), processBatch, stopwatch));
        return processBatch;
    }

    Range<Integer> createNextRange(int upper, int size) {
        if (upper < size) {
            return Range.closedOpen(upper, Math.min(size, upper + maxMessagesPerPoll));
        } else {
            return Range.closedOpen(upper, upper);
        }
    }

    @Override
    public DataProviderEndpoint getEndpoint() {
        return (DataProviderEndpoint) super.getEndpoint();
    }
}
