package net.techneon.synapse.api.interceptor;

import net.techneon.synapse.api.packet.Packet;

import java.util.Map;
import java.util.Optional;

/**
 * Mutable context passed to an {@link Interceptor} for a single packet, on the
 * way out ({@code beforeSend}) or on the way in ({@code beforeReceive}).
 *
 * <p>Interceptors may read and modify headers, inspect the packet, and read the
 * name of the remote server involved. {@code beforeReceive} runs before any
 * {@link net.techneon.synapse.api.packet.PacketFilter} or handler, so it can act
 * as cross-cutting middleware for logging, metrics, or rate limiting.
 *
 * @since 1.0
 */
public interface InterceptContext {

    /** Direction of travel for the packet being intercepted. */
    enum Direction {
        /** The packet is about to be sent to a remote server. */
        OUTBOUND,
        /** The packet has just been received from a remote server. */
        INBOUND
    }

    /**
     * @return whether this packet is {@link Direction#OUTBOUND} or
     *         {@link Direction#INBOUND}
     */
    Direction getDirection();

    /**
     * The packet being sent or received.
     *
     * @return the packet; never {@code null}
     */
    Packet getPacket();

    /**
     * The logical name of the remote server: the destination for outbound
     * packets, or the source for inbound packets.
     *
     * @return the remote server name; never {@code null}
     */
    String getRemoteServer();

    /**
     * The mutable header map for this packet. Outbound interceptors can add
     * headers the receiver will see; inbound interceptors can read (and strip)
     * them before handlers run.
     *
     * @return the live, mutable header map; never {@code null}
     */
    Map<String, String> getHeaders();

    /**
     * Convenience accessor for a single header.
     *
     * @param key the header name
     * @return the header value, or {@link Optional#empty()} if absent
     */
    default Optional<String> getHeader(String key) {
        return Optional.ofNullable(getHeaders().get(key));
    }
}
