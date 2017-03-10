package org.apache.camel.component.dataprovider;

import org.apache.camel.Consumer;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

/**
 * {@link ScheduledPollEndpoint} extension for {@link IDataProvider}.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
@UriEndpoint(scheme = "dataprovider", title = "Data Provider", syntax = "dataprovider:beanName", label = "data", consumerOnly = true)
public class DataProviderEndpoint extends ScheduledPollEndpoint {

    @UriPath(name = "name", description = "Name of IDataProvider to lookup in the registry")
    @Metadata(required = "true")
    private final IDataProvider<?> dataProvider;
    @UriParam(defaultValue = "0", description = "Range starting index. Default is 0 and could NOT be negative.")
    private int startingIndex = 0;
    @UriParam(defaultValue = "-1", description = "Range size. -1 means everything.")
    private int rangeSize = -1;

    public DataProviderEndpoint(String uri, DataProviderComponent dataProviderComponent,
                                IDataProvider<?> dataProvider) {
        super(uri, dataProviderComponent);
        assert dataProvider != null : "Unspecified data provider";
        this.dataProvider = dataProvider;
    }

    @Override
    public Producer createProducer() throws Exception {
        throw new UnsupportedOperationException("Currently this component can consume only.");
    }

    @Override
    public PollingConsumer createPollingConsumer() throws Exception {
        DataProviderPollingConsumer pollingConsumer = new DataProviderPollingConsumer(this);
        // Do NOT configure it using 'configurePollingConsumer' as we are NOT using the standard parameters
        // configurePollingConsumer(pollingConsumer);
        return pollingConsumer;
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        DataProviderConsumer consumer = new DataProviderConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    public int getRangeSize() {
        return rangeSize;
    }

    public void setRangeSize(int rangeSize) {
        assertNonNegative(rangeSize, "Range size");
        this.rangeSize = rangeSize;
    }

    public int getStartingIndex() {
        return startingIndex;
    }

    public void setStartingIndex(int startingIndex) {
        assertNonNegative(startingIndex, "Starting index");
        this.startingIndex = startingIndex;
    }

    private static void assertNonNegative(int value, String object) {
        if (value < 0) {
            throw new IllegalArgumentException(String.format("%s could NOT be negative.", object));
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    IDataProvider<?> getDataProvider() {
        return dataProvider;
    }
}
