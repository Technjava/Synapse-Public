package net.techneon.synapse.api.kv;

/**
 * Receives change notifications for a {@link SyncMap}, whether the change was
 * made locally or replicated from another server.
 *
 * <p>Every method has a no-op default; override only what you need.
 *
 * @since 1.1
 */
public interface SyncMapListener {

    /**
     * Called when a key is inserted or updated.
     *
     * @param key    the key
     * @param value  the new value
     * @param origin the logical name of the server that made the change
     */
    default void onPut(String key, Object value, String origin) {
    }

    /**
     * Called when a key is removed.
     *
     * @param key    the key
     * @param origin the logical name of the server that made the change
     */
    default void onRemove(String key, String origin) {
    }
}
