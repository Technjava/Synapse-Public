package net.techneon.synapse.api.send;

import net.techneon.synapse.api.packet.Packet;
import net.techneon.synapse.api.server.ServerInfo;

import java.util.Optional;

/**
 * The outcome of a successful acknowledgement: which server confirmed receipt,
 * how long the round trip took, and any reply payload that came back with the
 * acknowledgement.
 *
 * <p>Delivered to {@link SendResult#onAck(java.util.function.Consumer)} and,
 * equivalently, to handlers of {@link PacketAckEvent}.
 *
 * @since 1.0
 */
public interface AckResult {

    /**
     * The server that acknowledged the packet.
     *
     * @return the responding server; never {@code null}
     */
    ServerInfo getRespondingServer();

    /**
     * The measured round-trip time, in milliseconds, from send to acknowledgement.
     *
     * @return the round-trip time in milliseconds
     */
    long getRoundTripMillis();

    /**
     * The optional reply packet that the receiver attached when acknowledging,
     * for example via {@link net.techneon.synapse.api.packet.PacketEvent#respond}.
     *
     * @return the reply packet, or {@link Optional#empty()} if the
     *         acknowledgement carried no payload
     */
    Optional<Packet> getResponse();
}
