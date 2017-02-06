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
public class DataProviderComponentTest extends CamelTestSupport {

    @Test
    public void testDataProvider() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        assertMockEndpointsSatisfied();
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("foo", new StaticDataProvider<>("Hello", "Choubidou"));
        return registry;
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
