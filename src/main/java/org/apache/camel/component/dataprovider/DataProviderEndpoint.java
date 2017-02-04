package org.apache.camel.component.dataprovider;

import org.apache.camel.Consumer;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriPath;

/**
 * {@link ScheduledPollEndpoint} extension for {@link IDataProvider}.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
@UriEndpoint(scheme = "dataprovider", title = "Data Provider", syntax = "dataprovider:name")
public class DataProviderEndpoint extends ScheduledPollEndpoint {

    @UriPath(name = "name", description = "Name of IDataProvider to lookup in the registry")
    @Metadata(required = "true")
    private final IDataProvider<?> dataProvider;

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
        throw new UnsupportedOperationException("No PollingConsumer has been implemented yet.");
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        DataProviderConsumer consumer = new DataProviderConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    IDataProvider<?> getDataProvider() {
        return dataProvider;
    }
}
