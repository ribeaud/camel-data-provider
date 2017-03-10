package org.apache.camel.component.dataprovider;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.Test;

/**
 * Test cases for corresponding class {@link DataProviderComponent}.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class DataProviderComponentMultithreadingTest extends CamelTestSupport {

    @Test
    public void testDataProvider() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        // It will wait until it reaches the expected count
        mock.expectedMessageCount(1000);
        mock.assertIsSatisfied();
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("foo", new StaticDataProvider<>(DataProviderComponentTest.getRandomStrings(1000)));
        return registry;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("dataprovider://foo?consumer.useFixedDelay=true&consumer.delay=20&consumer.maxMessagesPerPoll=20&initialDelay=20").
                       to("mock:result");
            }
        };
    }
}
