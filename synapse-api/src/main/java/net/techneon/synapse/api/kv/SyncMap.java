package net.techneon.synapse.api.kv;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A distributed, eventually-consistent key-value map replicated across every
 * server on the network. Reads are local and instant; writes are applied locally
 * and propagated to all peers, using last-write-wins to resolve concurrent
 * updates to the same key.
 *
 * <pre>{@code
 * SyncMap economy = Synapse.map("economy");
 * economy.put(playerId.toString(), 500);
 * int coins = economy.get(playerId.toString(), Integer.class).orElse(0);
 *
 * economy.addListener(new SyncMapListener() {
 *     public void onPut(String key, Object value, String origin) {
 *         // reflect the change locally
 *     }
 * });
 * }</pre>
 *
 * <p>Values may be any type Synapse can serialize (Kryo by default). When a new
 * server connects it receives a full snapshot, so late joiners converge to the
 * same state.
 *
 * @since 1.1
 */
public interface SyncMap {

    /**
     * The name of this map (shared by all servers).
     *
     * @return the map name
     */
    String getName();

    /**
     * Inserts or updates a key, replicating the change to every server.
     *
     * @param key   the key
     * @param value the value (must be serializable)
     */
    void put(String key, Object value);

    /**
     * Reads a value.
     *
     * @param key the key
     * @return the value, or {@link Optional#empty()} if absent
     */
    Optional<Object> get(String key);

    /**
     * Reads a value cast to the expected type.
     *
     * @param key  the key
     * @param type the expected value type
     * @param <T>  the value type
     * @return the value, or {@link Optional#empty()} if absent
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Removes a key, replicating the removal to every server.
     *
     * @param key the key
     */
    void remove(String key);

    /**
     * @param key a key
     * @return whether the key is present
     */
    boolean containsKey(String key);

    /**
     * @return an immutable snapshot of all keys
     */
    Set<String> keySet();

    /**
     * @return an immutable snapshot of the whole map
     */
    Map<String, Object> asMap();

    /**
     * @return the number of entries
     */
    int size();

    /**
     * Registers a listener for local and replicated changes.
     *
     * @param listener the listener
     */
    void addListener(SyncMapListener listener);
}
