package org.apache.camel.component.dataprovider;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.NoSuchBeanException;
import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.spi.Registry;
import org.apache.camel.util.CamelContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * {@link UriEndpointComponent} extension for {@link IDataProvider}.
 * <p>
 * Lookups in the registry for {@link IDataProvider} implementations and instantiates then {@link DataProviderEndpoint}
 * with found implementation.
 * </p>
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
// See 'JpaComponent', 'DataSetComponent' and
// 'http://blog.javaforge.net/post/68180949840/creating-apache-camel-custom-component' for some examples how to create
// Camel components.
public class DataProviderComponent extends UriEndpointComponent {

    private static final Logger LOG = LoggerFactory.getLogger(DataProviderComponent.class);

    public DataProviderComponent() {
        super(DataProviderEndpoint.class);
    }

    public DataProviderComponent(CamelContext camelContext) {
        super(camelContext, DataProviderEndpoint.class);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Registry registry = getCamelContext().getRegistry();
        Class<?> type = IDataProvider.class;
        Set<?> found = registry.findByType(type);
        IDataProvider<?> dataProvider;
        switch (found.size()) {
            case 0:
                throw new NoSuchBeanException(remaining, type.getSimpleName());
            case 1:
                // If we only have one, we do NOT need 'remaining'
                dataProvider = (IDataProvider<?>) found.stream().findFirst().get();
                LOG.info("One (AND ONLY one) data provider '{}' found.", dataProvider);
                break;
            default:
                dataProvider = (IDataProvider<?>) CamelContextHelper.mandatoryLookup(getCamelContext(), remaining,
                        type);
                LOG.info("Data provider '{}' named '{}' found.", dataProvider, remaining);
        }
        return new DataProviderEndpoint(uri, this, dataProvider);
    }

}
