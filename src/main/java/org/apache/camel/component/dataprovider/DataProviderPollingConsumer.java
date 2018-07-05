package org.apache.camel.component.dataprovider;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.PollingConsumerSupport;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link PollingConsumerSupport} extension for {@link IDataProvider}.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class DataProviderPollingConsumer extends PollingConsumerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DataProviderPollingConsumer.class);

    private ExecutorService executorService;

    DataProviderPollingConsumer(DataProviderEndpoint dataProviderEndpoint) {
        super(dataProviderEndpoint);
    }

    @Override
    public Exchange receive() {
        Range<Integer> range = createRange();
        IDataProvider<?> dataProvider = getEndpoint().getDataProvider();
        LogUtils.info(LOG, () -> String.format("[%s] Handling range '%s'.", dataProvider.getName(), range));
        Iterable<?> partition = dataProvider.partition(range);
        Exchange exchange = getEndpoint().createExchange();
        int size = Iterables.size(partition);
        Message in = exchange.getIn();
        switch (size) {
            case 1:
                in.setBody(Iterables.get(partition, 0));
                break;
            default:
                in.setBody(partition);
        }
        return exchange;
    }

    @Override
    public Exchange receiveNoWait() {
        return receive();
    }

    @Override
    public Exchange receive(long timeout) {
        // the task is the receive method
        Future<Exchange> future = executorService.submit((Callable<Exchange>) this::receive);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        } catch (TimeoutException e) {
            // ignore as we hit timeout then return null
        }
        return null;
    }

    @Override
    protected void doStart() {
        executorService = getCamelContext().getExecutorServiceManager().newDefaultThreadPool(this, getClass().getSimpleName());
    }

    @Override
    protected void doStop() {
        getCamelContext().getExecutorServiceManager().shutdown(executorService);
    }

    private CamelContext getCamelContext() {
        return getEndpoint().getCamelContext();
    }

    @Override
    public DataProviderEndpoint getEndpoint() {
        return (DataProviderEndpoint) super.getEndpoint();
    }

    private Range<Integer> createRange() {
        int size = getEndpoint().getDataProvider().getSize();
        int rangeSize = getEndpoint().getRangeSize();
        rangeSize = rangeSize < 0 ? size : rangeSize;
        int lower = getEndpoint().getStartingIndex();
        return Range.closedOpen(Math.min(lower, size - 1), Math.min(size, lower + rangeSize));
    }
}
