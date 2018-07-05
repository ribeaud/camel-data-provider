package org.apache.camel.component.dataprovider;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("dataprovider://foo?consumer.maxMessagesPerPoll=20&initialDelay=20&greedy=true")
                        .to("seda:input");
                from("seda:input").threads(20).process(exchange -> Thread.sleep(100L)).to("mock:result");
            }
        };
    }
}
