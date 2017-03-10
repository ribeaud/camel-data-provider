package org.apache.camel.component.dataprovider;

import com.google.common.collect.Range;

/**
 * Data provider.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public interface IDataProvider<T> {

    /**
     * Returns the data size.
     * <p>
     * This method should proceed fast as it could be invoked frequently.
     * </p>
     */
    public int getSize();

    /**
     * Returns the partition associated to given <i>range</i>.
     *
     * @param range the range. Can NOT be <code>null</code>.
     * @return the partition associated to given <i>range</i>. Never <code>null</code>.
     */
    public Iterable<T> partition(Range<Integer> range);

    /**
     * Returns a name uniquely identifying this data provider.
     * <p>
     * Especially useful when dealing with several data providers.
     * </p>
     *
     * @return {@code getClass().getSimpleName()} by default.
     */
    public default String getName() {
        return getClass().getSimpleName();
    }
}
