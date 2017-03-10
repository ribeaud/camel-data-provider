package org.apache.camel.component.dataprovider;

import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Test cases for corresponding class {@link DataProviderPollingConsumer}.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class DataProviderPollingConsumerTest extends CamelTestSupport {

    @Test
    public void testReceiveBodyWithRangeSizeAndStartingIndex() {
        List<String> body = consumer.receiveBody("dataprovider://foo?rangeSize=2&startingIndex=3", List.class);
        assertEquals(body.size(), 2);
        assertEquals(body.get(0), "Four");
    }

    @Test
    public void testReceiveBody() {
        List<String> body = consumer.receiveBody("dataprovider://foo", List.class);
        assertEquals(body.size(), 10);
    }

    @Test
    public void testReceiveBodyWithOneString() {
        String body = consumer.receiveBody("dataprovider://foo?rangeSize=1&startingIndex=12345", String.class);
        assertEquals(body, "Ten");
    }

    @Test
    public void testReceiveBodyWithZeroRangeSize() {
        List<String> body = consumer.receiveBody("dataprovider://foo?rangeSize=0&startingIndex=4", List.class);
        assertEquals(body.size(), 0);
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("foo", new StaticDataProvider<>(Arrays.asList("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten")));
        return registry;
    }
}
