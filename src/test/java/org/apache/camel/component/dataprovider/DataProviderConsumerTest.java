package org.apache.camel.component.dataprovider;

import com.google.common.collect.Range;
import org.apache.camel.Processor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for corresponding class {@link DataProviderConsumer}.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class DataProviderConsumerTest {

    @Mock
    private DataProviderEndpoint endpoint;
    @Mock
    private Processor processor;
    private DataProviderConsumer consumer;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        consumer = new DataProviderConsumer(endpoint, processor);
        consumer.setMaxMessagesPerPoll(2);
    }

    @Test
    public void createNextRange() {
        Range<Integer> range = consumer.createNextRange(2, 4);
        assertEquals(range, Range.closedOpen(2, 4));
        range = consumer.createNextRange(0, 20);
        assertEquals(range, Range.closedOpen(0, 2));
        range = consumer.createNextRange(3, 4);
        assertEquals(range, Range.closedOpen(3, 4));
        range = consumer.createNextRange(4, 4);
        Range<Integer> empty = Range.closedOpen(4, 4);
        assertEquals(range, empty);
        assertTrue(empty.isEmpty());
    }
}
