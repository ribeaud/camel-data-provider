package org.apache.camel.component.dataprovider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

/**
 * {@link IDataProvider} implementation which already specifies all the data in the constructor.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class StaticDataProvider<T> implements IDataProvider<T> {

    // Immutable list is expected here
    private final ImmutableList<T> data;

    public StaticDataProvider(T... data) {
        this(ImmutableList.copyOf(data));
    }

    /**
     * Constructor with provided {@link Iterable} <i>data</i>.
     * <p>
     * Internally will be converted into a {@link ImmutableList}.
     * </p>
     *
     * @param data the {@link Iterable} data. Can NOT be <code>null</code>.
     */
    public StaticDataProvider(Iterable<T> data) {
        assert data != null : "Unspecified data";
        if (data instanceof ImmutableList == false) {
            this.data = ImmutableList.copyOf(data);
        } else {
            this.data = (ImmutableList<T>) data;
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
