package org.apache.camel.component.dataprovider;

import com.google.common.collect.Range;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledBatchPollingConsumer;
import org.apache.camel.util.CastUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ScheduledBatchPollingConsumer} extension for {@link IDataProvider}.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class DataProviderConsumer extends ScheduledBatchPollingConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(DataProviderConsumer.class);

    private final AtomicReference<Range<Integer>> rangeReference = new AtomicReference<>();

    public DataProviderConsumer(DataProviderEndpoint dataProviderEndpoint, Processor processor) {
        super(dataProviderEndpoint, processor);
    }

    @Override
    protected void doStart() throws Exception {
        IDataProvider<?> dataProvider = getDataProviderEndoint().getDataProvider();
        int size = dataProvider.getSize();
        rangeReference.set(Range.closedOpen(0, Math.min(size, maxMessagesPerPoll)));
        LogUtils.info(LOG, () -> String.format("Preparing to handle %d partition(s) (%d / %d)",
                (int) Math.ceil((float) size / maxMessagesPerPoll), size, maxMessagesPerPoll));
        super.doStart();
    }

    @Override
    public int processBatch(Queue<Object> exchanges) throws Exception {
        assert exchanges != null : "Unspecified exchanges";
        final int total = exchanges.size();
        for (int index = 0; index < total && isBatchAllowed(); index++) {
            Exchange exchange = (Exchange) exchanges.poll();
            // Add current index and total as properties
            exchange.setProperty(Exchange.BATCH_INDEX, index);
            exchange.setProperty(Exchange.BATCH_SIZE, total);
            exchange.setProperty(Exchange.BATCH_COMPLETE, index == total - 1);
            // Update pending number of exchanges
            pendingExchanges = total - index - 1;
            // Process the current exchange
            getProcessor().process(exchange);
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        }
        return total;
    }

    @Override
    protected int poll() throws Exception {
        DataProviderEndpoint endpoint = getDataProviderEndoint();
        IDataProvider<?> dataProvider = endpoint.getDataProvider();
        final Range<Integer> range = this.rangeReference.get();
        if (range.isEmpty()) {
            return 0;
        }
        LogUtils.info(LOG, () -> String.format("Handling range '%s'.", range));
        int size = dataProvider.getSize();
        Queue<Exchange> exchanges = new LinkedList<Exchange>();
        for (Object item : dataProvider.partition(range)) {
            Exchange exchange = endpoint.createExchange();
            exchange.getIn().setBody(item);
            exchanges.add(exchange);
        }
        this.rangeReference.set(createNextRange(range.upperEndpoint(), size));
        // 'processBatch' has to be invoked. It will NOT be invoked somewhere else. Bad interface design actually... :(
        return processBatch(CastUtils.cast(exchanges));
    }

    Range<Integer> createNextRange(int upper, int size) {
        if (upper < size) {
            return Range.closedOpen(upper, Math.min(size, upper + maxMessagesPerPoll));
        } else {
            return Range.closedOpen(upper, upper);
        }
    }

    private DataProviderEndpoint getDataProviderEndoint() {
        return (DataProviderEndpoint) getEndpoint();
    }
}
