package org.apache.camel.component.dataprovider;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * A collection of utilities around {@link Lock}
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class LockUtils {

    private LockUtils() {
        // Can NOT be instantiated
    }

    /**
     * Executes given <i>runnable</i> surrounded by given <i>lock</i>.
     *
     * @param lock the {@link Lock} to work with. Can NOT be <code>null</code>.
     * @param runnable the {@link Runnable} to execute. Can NOT be <code>null</code>.
     */
    public static void runWithLock(Lock lock, Runnable runnable) {
        assert lock != null : "Unspecified lock";
        assert runnable != null : "Unspecified runnable";
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes given <i>supplier</i> surrounded by given <i>lock</i> and returns.
     *
     * @param lock the {@link Lock} to work with. Can NOT be <code>null</code>.
     * @param supplier the {@link Supplier} to apply. Can NOT be <code>null</code>.
     */
    public static <R> R getWithLock(Lock lock, Supplier<R> supplier) {
        assert lock != null : "Unspecified lock";
        assert supplier != null : "Unspecified supplier";
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }
}
