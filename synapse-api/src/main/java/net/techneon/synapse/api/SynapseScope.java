package net.techneon.synapse.api;

import net.techneon.synapse.api.channel.ChannelSubscriber;
import net.techneon.synapse.api.channel.Subscription;
import net.techneon.synapse.api.packet.Packet;
import net.techneon.synapse.api.packet.PacketFilter;
import net.techneon.synapse.api.packet.PacketListener;
import net.techneon.synapse.api.packet.PacketSerializer;
import net.techneon.synapse.api.send.SendOptions;
import net.techneon.synapse.api.send.SendResult;
import net.techneon.synapse.api.server.ServerInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * The set of messaging operations that are scoped to a single namespace:
 * registering packet types, listeners and filters, and sending, broadcasting,
 * requesting, and publishing.
 *
 * <p>Both the global {@link SynapseAPI} and every {@link Namespace} expose these
 * operations. Packet ids, listeners, and filters registered through one scope are
 * isolated from those registered through another, which is what lets multiple
 * plugins share a server without colliding.
 *
 * @since 1.0
 */
public interface SynapseScope {

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    /**
     * Registers a packet type using the default Kryo serialization. The type's
     * id is derived deterministically from its name so every node agrees on it.
     *
     * @param type the packet class to register
     * @param <T>  the packet type
     * @throws net.techneon.synapse.api.exception.PacketRegistrationException if
     *         the id collides with an already-registered type
     */
    <T extends Packet> void register(Class<T> type);

    /**
     * Registers a packet type with an explicit numeric id and the default Kryo
     * serialization. Use this when you want full control over wire ids.
     *
     * @param type the packet class to register
     * @param id   the stable numeric id to assign
     * @param <T>  the packet type
     */
    <T extends Packet> void register(Class<T> type, int id);

    /**
     * Registers a packet type with a custom {@link PacketSerializer}, bypassing
     * Kryo entirely for this type.
     *
     * @param type       the packet class to register
     * @param serializer the serializer to use for this type
     * @param <T>        the packet type
     */
    <T extends Packet> void register(Class<T> type, PacketSerializer<T> serializer);

    /**
     * Registers all {@code @PacketHandler} methods on the given listener.
     *
     * @param listener the listener instance to register
     */
    void registerListener(PacketListener listener);

    /**
     * Unregisters a previously registered listener, removing all of its handlers.
     *
     * @param listener the listener instance to remove
     */
    void unregisterListener(PacketListener listener);

    /**
     * Adds a declarative filter that can reject packets of the given type before
     * any handler sees them.
     *
     * @param type   the packet type to guard
     * @param filter the filter to apply
     * @param <T>    the packet type
     */
    <T extends Packet> void addFilter(Class<T> type, PacketFilter<T> filter);

    // ------------------------------------------------------------------
    // Sending
    // ------------------------------------------------------------------

    /**
     * Sends a packet to a single named server with default options.
     *
     * @param server the destination server name
     * @param packet the packet to send
     * @return a {@link SendResult} handle
     * @throws net.techneon.synapse.api.exception.UnknownServerException if no
     *         such server is configured
     */
    SendResult send(String server, Packet packet);

    /**
     * Sends a packet to a single named server with explicit options (timeout,
     * retries, acknowledgement, priority, TTL, headers).
     *
     * @param server  the destination server name
     * @param packet  the packet to send
     * @param options the send options
     * @return a {@link SendResult} handle
     */
    SendResult send(String server, Packet packet, SendOptions options);

    /**
     * Broadcasts a packet to every connected server.
     *
     * @param packet the packet to send
     */
    void broadcast(Packet packet);

    /**
     * Broadcasts a packet to every connected member of a group.
     *
     * @param group  the target group name
     * @param packet the packet to send
     */
    void broadcastToGroup(String group, Packet packet);

    /**
     * Broadcasts a packet to every connected server except those named.
     *
     * @param packet           the packet to send
     * @param excludedServers  server names to skip
     */
    void sendExcept(Packet packet, String... excludedServers);

    /**
     * Sends a packet to every connected server for which the predicate returns
     * {@code true}.
     *
     * @param packet    the packet to send
     * @param predicate the per-server condition
     */
    void sendIf(Packet packet, Predicate<ServerInfo> predicate);

    // ------------------------------------------------------------------
    // Request / response
    // ------------------------------------------------------------------

    /**
     * Sends a request and returns a future that completes with the typed
     * response packet the receiver replies with.
     *
     * @param server  the destination server name
     * @param request the request packet
     * @param <R>     the expected response packet type
     * @return a future completing with the response, or completing exceptionally
     *         on timeout or transport error
     */
    <R extends Packet> CompletableFuture<R> sendRequest(String server, Packet request);

    /**
     * Sends a request and blocks the calling thread until the response arrives or
     * the timeout elapses. Never call this from a server main thread.
     *
     * @param server        the destination server name
     * @param request       the request packet
     * @param timeoutMillis the maximum time to wait, in milliseconds
     * @param <R>           the expected response packet type
     * @return the response packet
     * @throws net.techneon.synapse.api.exception.SynapseException if the request
     *         times out or fails
     */
    <R extends Packet> R sendRequestBlocking(String server, Packet request, long timeoutMillis);

    // ------------------------------------------------------------------
    // Named pub/sub channels
    // ------------------------------------------------------------------

    /**
     * Subscribes to a named channel, receiving every message published to it
     * across the network.
     *
     * @param channel    the channel name
     * @param subscriber the callback to invoke for each message
     * @return a {@link Subscription} handle used to cancel the subscription
     */
    Subscription subscribe(String channel, ChannelSubscriber subscriber);

    /**
     * Publishes a payload to a named channel on every connected server.
     *
     * @param channel the channel name
     * @param payload the payload object (serialized with Kryo)
     */
    void publish(String channel, Object payload);
}
