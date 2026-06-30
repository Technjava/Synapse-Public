package net.techneon.synapse.api.packet;

import net.techneon.synapse.api.server.ServerInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Metadata and control surface for a received packet, delivered as the optional
 * second parameter of a {@link PacketHandler} method.
 *
 * <p>It exposes everything Synapse knows about the delivery &mdash; who sent it,
 * when, how long it took, whether the link was encrypted, and any custom headers
 * &mdash; and lets the handler reply to or cancel the packet:
 *
 * <pre>{@code
 * {@literal @}PacketHandler(priority = PacketPriority.HIGH)
 * public void onRequest(PingRequestPacket packet, PacketEvent event) {
 *     if (!event.isEncrypted()) {
 *         event.setCancelled(true);
 *         return;
 *     }
 *     event.respond(new PingResponsePacket(System.currentTimeMillis()));
 * }
 * }</pre>
 *
 * <p>The same instance is passed to every handler for a given packet, so
 * cancellation and header reads/writes are visible across the handler chain.
 *
 * @since 1.0
 */
public interface PacketEvent {

    /**
     * The server that sent this packet.
     *
     * @return the source server; never {@code null}
     */
    ServerInfo getSource();

    /**
     * The group of the {@linkplain #getSource() source server}, if it belongs to
     * one.
     *
     * @return the source group name, or {@link Optional#empty()}
     */
    Optional<String> getSourceGroup();

    /**
     * The wall-clock time, in epoch milliseconds, at which the sender dispatched
     * this packet. Note this relies on the sender's clock.
     *
     * @return the send timestamp in epoch milliseconds
     */
    long getSentTimestamp();

    /**
     * The wall-clock time, in epoch milliseconds, at which this node received the
     * packet.
     *
     * @return the receive timestamp in epoch milliseconds
     */
    long getReceivedTimestamp();

    /**
     * The apparent one-way latency, {@code receivedTimestamp - sentTimestamp}, in
     * milliseconds. Because it spans two machines' clocks it is best treated as a
     * rough indicator unless the clocks are synchronized.
     *
     * @return the apparent latency in milliseconds
     */
    default long getLatencyMillis() {
        return getReceivedTimestamp() - getSentTimestamp();
    }

    /**
     * The numeric id under which the packet type was registered.
     *
     * @return the registered packet id
     */
    int getPacketId();

    /**
     * The Synapse wire protocol version used for this packet, allowing handlers
     * to remain compatible across mixed-version networks.
     *
     * @return the protocol version
     */
    int getProtocolVersion();

    /**
     * Whether the connection that carried this packet is secured with mutual TLS.
     *
     * @return {@code true} if the link is encrypted and mutually authenticated
     */
    boolean isEncrypted();

    /**
     * Reads a custom header attached to this packet by the sender or by a
     * {@code beforeSend} interceptor.
     *
     * @param key the header name
     * @return the header value, or {@link Optional#empty()} if absent
     */
    Optional<String> getHeader(String key);

    /**
     * All custom headers carried by this packet.
     *
     * @return an immutable map of header names to values; never {@code null}
     */
    Map<String, String> getHeaders();

    /**
     * Sends a packet straight back to the {@linkplain #getSource() source} server.
     *
     * <p>If the incoming packet was sent with an acknowledgement or request
     * expectation, the reply is correlated automatically so the original sender's
     * {@code onAck}/{@code sendRequest} future completes.
     *
     * @param packet the reply packet; must not be {@code null}
     */
    void respond(Packet packet);

    /**
     * Whether a prior handler has cancelled this packet. Cancellation stops
     * propagation to handlers that opt out via
     * {@link PacketHandler#ignoreCancelled()}.
     *
     * @return {@code true} if the packet is currently cancelled
     */
    boolean isCancelled();

    /**
     * Sets the cancellation state of this packet.
     *
     * @param cancelled {@code true} to cancel further normal processing
     */
    void setCancelled(boolean cancelled);
}
