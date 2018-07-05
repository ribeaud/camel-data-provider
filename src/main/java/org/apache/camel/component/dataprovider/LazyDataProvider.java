package org.apache.camel.component.dataprovider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Lazy {@link IDataProvider} implementation.
 * <p>
 * Lazily loads the data, only when they are needed. Just correctly implement {@link #loadData()}. This class is thread
 * safe.
 * </p>
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public abstract class LazyDataProvider<T> implements IDataProvider<T> {

    private final ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();
    private final Lock readDataLock = dataLock.readLock();
    private final Lock writeDataLock = dataLock.writeLock();

    private ImmutableList<T> data;

    /**
     * Tells how to load the data.
     * <p>
     * Internally will be converted into a {@link ImmutableList}.
     * </p>
     *
     * @return the loaded data. Never <code>null</code> but could be <i>empty</i>.
     */
    public abstract Iterable<T> loadData();

    private void ensureDataLoaded() {
        LockUtils.runWithLock(writeDataLock, () -> {
            if (data == null) {
                Iterable<T> data = loadData();
                if (data instanceof ImmutableList) {
                    this.data = (ImmutableList<T>) data;
                } else {
                    this.data = ImmutableList.copyOf(data);
                }
            }
        });
    }

    @Override
    public int getSize() {
        ensureDataLoaded();
        return LockUtils.getWithLock(readDataLock, () -> data.size());
    }

    @Override
    public Iterable<T> partition(Range<Integer> range) {
        assert range != null : "Unspecified range";
        ensureDataLoaded();
        return LockUtils.getWithLock(readDataLock, () -> data.subList(range.lowerEndpoint(), range.upperEndpoint()));
    }
}