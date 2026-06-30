package net.techneon.synapse.api;

/**
 * Internal registry holding the single active {@link SynapseAPI} instance.
 *
 * <p>This is plumbing used by the platform bootstrap (the Bukkit/BungeeCord/
 * Velocity plugin) to install the implementation at start-up and remove it at
 * shutdown. Application developers should use the {@link Synapse} facade or
 * {@link Synapse#get()} instead of calling these methods.
 *
 * @since 1.0
 */
public final class SynapseProvider {

    private static volatile SynapseAPI instance;

    private SynapseProvider() {
    }

    /**
     * Installs the active implementation. Called once by the platform bootstrap.
     *
     * @param api the implementation to install
     * @throws IllegalStateException if an implementation is already installed
     */
    public static void register(SynapseAPI api) {
        if (api == null) {
            throw new IllegalArgumentException("api must not be null");
        }
        synchronized (SynapseProvider.class) {
            if (instance != null) {
                throw new IllegalStateException("A SynapseAPI implementation is already registered");
            }
            instance = api;
        }
    }

    /**
     * Removes the active implementation. Called once by the platform bootstrap at
     * shutdown.
     */
    public static void unregister() {
        synchronized (SynapseProvider.class) {
            instance = null;
        }
    }

    /**
     * @return the active implementation, or {@code null} if Synapse is not running
     */
    public static SynapseAPI getInstance() {
        return instance;
    }
}
