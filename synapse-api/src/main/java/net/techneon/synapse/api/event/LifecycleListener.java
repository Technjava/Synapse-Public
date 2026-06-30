package net.techneon.synapse.api.event;

import net.techneon.synapse.api.packet.Packet;
import net.techneon.synapse.api.server.ServerInfo;

/**
 * Callbacks for network-wide lifecycle events. Every method has a no-op default,
 * so implementations override only the events they care about:
 *
 * <pre>{@code
 * Synapse.addLifecycleListener(new LifecycleListener() {
 *     {@literal @}Override public void onReady() {
 *         getLogger().info("Synapse is up");
 *     }
 *     {@literal @}Override public void onServerConnect(ServerInfo server) {
 *         getLogger().info(server.getName() + " connected");
 *     }
 * });
 * }</pre>
 *
 * @since 1.0
 */
public interface LifecycleListener {

    /**
     * Called once the local node has finished starting up and is ready to send
     * and receive packets.
     */
    default void onReady() {
    }

    /**
     * Called when the local node is shutting down, before connections close.
     */
    default void onShutdown() {
    }

    /**
     * Called when a connection to a remote server is established (and, when TLS
     * is enabled, mutually authenticated).
     *
     * @param server the server that just connected
     */
    default void onServerConnect(ServerInfo server) {
    }

    /**
     * Called when the connection to a remote server is lost. Synapse will keep
     * attempting to reconnect in the background.
     *
     * @param server the server that disconnected
     */
    default void onServerDisconnect(ServerInfo server) {
    }

    /**
     * Called when a packet is dropped before delivery &mdash; for example because
     * an interceptor or filter rejected it, its time-to-live expired, or no
     * handler was registered.
     *
     * @param packet the dropped packet (may be {@code null} if it could not be
     *               deserialized)
     * @param reason a short, human-readable explanation
     */
    default void onPacketDropped(Packet packet, String reason) {
    }
}
