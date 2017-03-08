package org.apache.camel.component.dataprovider;

/**
 * Some constant around <b>dataprovider</b> component.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public final class DataProviderConstants {

    private DataProviderConstants() {
        // Can NOT be instantiated
    }

    /**
     * Constant to specify whether current {@link org.apache.camel.Exchange} is part of the last batch.
     * <p>
     * Value stored should be a <b>boolean</b> or associated object.
     * </p>
     */
    public final static String LAST_BATCH = DataProviderConstants.class.getName() + ".LastBatch";

    /**
     * Constant to specify whether current {@link org.apache.camel.Exchange} is the last one.
     * <p>
     * Value stored should be a <b>boolean</b> or associated object.
     * </p>
     */
    public final static String LAST_EXCHANGE = DataProviderConstants.class.getName() + ".LastExchange";
}
