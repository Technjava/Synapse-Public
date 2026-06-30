package net.techneon.synapse.api;

import net.techneon.synapse.api.channel.ChannelSubscriber;
import net.techneon.synapse.api.channel.Subscription;
import net.techneon.synapse.api.event.LifecycleListener;
import net.techneon.synapse.api.interceptor.Interceptor;
import net.techneon.synapse.api.packet.Packet;
import net.techneon.synapse.api.packet.PacketFilter;
import net.techneon.synapse.api.packet.PacketListener;
import net.techneon.synapse.api.packet.PacketSerializer;
import net.techneon.synapse.api.send.SendOptions;
import net.techneon.synapse.api.send.SendResult;
import net.techneon.synapse.api.server.ServerGroup;
import net.techneon.synapse.api.server.ServerInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Static entry point to Synapse &mdash; the one-line, "just call it" facade that
 * forwards to the running {@link SynapseAPI} instance.
 *
 * <p>Everything you need for everyday use lives here:
 *
 * <pre>{@code
 * // register your packet types and listeners once, at start-up
 * Synapse.register(PlayerTransferPacket.class);
 * Synapse.registerListener(new MyListener());
 *
 * // then send from anywhere
 * Synapse.send("survival", new PlayerTransferPacket(name, "survival"));
 * Synapse.broadcast(new ServerAnnouncePacket("Restarting in 5m"));
 *
 * PingResponsePacket pong =
 *     Synapse.sendRequestBlocking("survival", new PingRequestPacket(), 5000);
 * }</pre>
 *
 * <p>For dependency injection or testing, grab the underlying instance with
 * {@link #get()} and call its methods directly. All static methods throw
 * {@link IllegalStateException} if Synapse is not currently running.
 *
 * @since 1.0
 */
public final class Synapse {

    private Synapse() {
    }

    /**
     * Returns the active {@link SynapseAPI} instance.
     *
     * @return the running Synapse service
     * @throws IllegalStateException if Synapse is not currently running
     */
    public static SynapseAPI get() {
        SynapseAPI api = SynapseProvider.getInstance();
        if (api == null) {
            throw new IllegalStateException(
                "Synapse is not running. Ensure the Synapse plugin is installed and enabled "
                    + "and that your plugin declares it as a dependency.");
        }
        return api;
    }

    /**
     * Whether Synapse is currently installed and running. Useful for optional
     * (soft-dependency) integrations.
     *
     * @return {@code true} if a {@link SynapseAPI} is available
     */
    public static boolean isAvailable() {
        return SynapseProvider.getInstance() != null;
    }

    // ------------------------------------------------------------------
    // Registration (default namespace)
    // ------------------------------------------------------------------

    /** @see SynapseScope#register(Class) */
    public static <T extends Packet> void register(Class<T> type) {
        get().register(type);
    }

    /** @see SynapseScope#register(Class, int) */
    public static <T extends Packet> void register(Class<T> type, int id) {
        get().register(type, id);
    }

    /** @see SynapseScope#register(Class, PacketSerializer) */
    public static <T extends Packet> void register(Class<T> type, PacketSerializer<T> serializer) {
        get().register(type, serializer);
    }

    /** @see SynapseScope#registerListener(PacketListener) */
    public static void registerListener(PacketListener listener) {
        get().registerListener(listener);
    }

    /** @see SynapseScope#unregisterListener(PacketListener) */
    public static void unregisterListener(PacketListener listener) {
        get().unregisterListener(listener);
    }

    /** @see SynapseScope#addFilter(Class, PacketFilter) */
    public static <T extends Packet> void addFilter(Class<T> type, PacketFilter<T> filter) {
        get().addFilter(type, filter);
    }

    // ------------------------------------------------------------------
    // Sending (default namespace)
    // ------------------------------------------------------------------

    /** @see SynapseScope#send(String, Packet) */
    public static SendResult send(String server, Packet packet) {
        return get().send(server, packet);
    }

    /** @see SynapseScope#send(String, Packet, SendOptions) */
    public static SendResult send(String server, Packet packet, SendOptions options) {
        return get().send(server, packet, options);
    }

    /** @see SynapseScope#broadcast(Packet) */
    public static void broadcast(Packet packet) {
        get().broadcast(packet);
    }

    /** @see SynapseScope#broadcastToGroup(String, Packet) */
    public static void broadcastToGroup(String group, Packet packet) {
        get().broadcastToGroup(group, packet);
    }

    /** @see SynapseScope#sendExcept(Packet, String...) */
    public static void sendExcept(Packet packet, String... excludedServers) {
        get().sendExcept(packet, excludedServers);
    }

    /** @see SynapseScope#sendIf(Packet, Predicate) */
    public static void sendIf(Packet packet, Predicate<ServerInfo> predicate) {
        get().sendIf(packet, predicate);
    }

    // ------------------------------------------------------------------
    // Request / response (default namespace)
    // ------------------------------------------------------------------

    /** @see SynapseScope#sendRequest(String, Packet) */
    public static <R extends Packet> CompletableFuture<R> sendRequest(String server, Packet request) {
        return get().sendRequest(server, request);
    }

    /** @see SynapseScope#sendRequestBlocking(String, Packet, long) */
    public static <R extends Packet> R sendRequestBlocking(String server, Packet request, long timeoutMillis) {
        return get().sendRequestBlocking(server, request, timeoutMillis);
    }

    // ------------------------------------------------------------------
    // Pub/sub (default namespace)
    // ------------------------------------------------------------------

    /** @see SynapseScope#subscribe(String, ChannelSubscriber) */
    public static Subscription subscribe(String channel, ChannelSubscriber subscriber) {
        return get().subscribe(channel, subscriber);
    }

    /** @see SynapseScope#publish(String, Object) */
    public static void publish(String channel, Object payload) {
        get().publish(channel, payload);
    }

    // ------------------------------------------------------------------
    // Topology, namespaces, lifecycle
    // ------------------------------------------------------------------

    /** @see SynapseAPI#getLocalServer() */
    public static ServerInfo getLocalServer() {
        return get().getLocalServer();
    }

    /** @see SynapseAPI#getServers() */
    public static Collection<ServerInfo> getServers() {
        return get().getServers();
    }

    /** @see SynapseAPI#getServer(String) */
    public static Optional<ServerInfo> getServer(String name) {
        return get().getServer(name);
    }

    /** @see SynapseAPI#getGroups() */
    public static Collection<ServerGroup> getGroups() {
        return get().getGroups();
    }

    /** @see SynapseAPI#getGroup(String) */
    public static Optional<ServerGroup> getGroup(String name) {
        return get().getGroup(name);
    }

    /** @see SynapseAPI#namespace(String) */
    public static Namespace namespace(String name) {
        return get().namespace(name);
    }

    /** @see SynapseAPI#addInterceptor(Interceptor) */
    public static void addInterceptor(Interceptor interceptor) {
        get().addInterceptor(interceptor);
    }

    /** @see SynapseAPI#addLifecycleListener(LifecycleListener) */
    public static void addLifecycleListener(LifecycleListener listener) {
        get().addLifecycleListener(listener);
    }
}
