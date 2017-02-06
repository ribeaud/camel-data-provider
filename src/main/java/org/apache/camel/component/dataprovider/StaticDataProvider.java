package org.apache.camel.component.dataprovider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * {@link IDataProvider} implementation which already specifies all the data in the constructor.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class StaticDataProvider<T> implements IDataProvider<T> {

    private final List<T> data;

    public StaticDataProvider(T... data) {
        this(Arrays.asList(data));
    }

    public StaticDataProvider(Collection<T> data) {
        assert data != null : "Unspecified data";
        if (data instanceof List == false) {
            this.data = ImmutableList.copyOf(data);
        } else {
            this.data = ((List<T>) data);
        }
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public Iterable<T> partition(Range<Integer> range) {
        assert range != null : "Unspecified range";
        return data.subList(range.lowerEndpoint(), range.upperEndpoint());
    }
}
