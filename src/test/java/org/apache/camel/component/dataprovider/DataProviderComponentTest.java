package org.apache.camel.component.dataprovider;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Test cases for corresponding class {@link DataProviderComponent}.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class DataProviderComponentTest extends CamelTestSupport {

    @Test
    public void testDataProvider() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        // It will wait until it reaches the expected count
        mock.expectedMessageCount(100);
        mock.setRetainFirst(1);
        mock.setRetainLast(1);
        mock.assertIsSatisfied();
        List<Exchange> exchanges = mock.getExchanges();
        assertEquals(exchanges.size(), 2);
        // Last
        Exchange lastExchange = exchanges.get(1);
        assertNotNull(lastExchange);
        assertEquals(lastExchange.getProperty(DataProviderConstants.LAST_EXCHANGE), true);
        assertEquals(lastExchange.getProperty(DataProviderConstants.LAST_BATCH), true);
        assertEquals(lastExchange.getProperty(Exchange.BATCH_COMPLETE), true);
        assertEquals(lastExchange.getProperty(Exchange.BATCH_SIZE), 20);
        assertEquals(lastExchange.getProperty(Exchange.BATCH_INDEX), 19);
        // First
        Exchange firstExchange = exchanges.get(0);
        assertNotNull(firstExchange);
        assertEquals(firstExchange.getProperty(DataProviderConstants.LAST_EXCHANGE), false);
        assertEquals(firstExchange.getProperty(DataProviderConstants.LAST_BATCH), false);
        assertEquals(firstExchange.getProperty(Exchange.BATCH_COMPLETE), false);
        assertEquals(firstExchange.getProperty(Exchange.BATCH_SIZE), 20);
        assertEquals(firstExchange.getProperty(Exchange.BATCH_INDEX), 0);
    }

    @Test
    public void testDataProviderNoMoreThan100() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        // It will wait until it reaches the expected count
        mock.expectedMessageCount(101);
        mock.assertIsNotSatisfied();
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        List<String> strings = new ArrayList<>(100);
        IntStream.range(0, 100).forEach(i -> strings.add(createRandomString()));
        registry.bind("foo", new StaticDataProvider<>(strings));
        return registry;
    }

    @Override
    protected void doPostSetup() throws Exception {
        CamelContext context = context();
        DataProviderComponent dataProviderComponent = new DataProviderComponent(context);
        String componentName = "dataprovider";
        if (context.hasComponent(componentName) == null) {
            context.addComponent(componentName, dataProviderComponent);
        }
    }

    private static String createRandomString() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("dataprovider://foo?consumer.useFixedDelay=true&consumer.maxMessagesPerPoll=20&initialDelay=20")
                        .to("mock:result");
            }
        };
    }
}
