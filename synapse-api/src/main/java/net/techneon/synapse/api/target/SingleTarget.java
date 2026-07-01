package net.techneon.synapse.api.target;

import net.techneon.synapse.api.packet.Packet;
import net.techneon.synapse.api.packet.PacketPriority;
import net.techneon.synapse.api.send.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * A fluent builder for sending to exactly one destination (a named server, or the
 * server currently hosting a player).
 *
 * <pre>{@code
 * Synapse.to("survival")
 *     .requireAck().timeout(2000).retries(2)
 *     .send(packet);
 *
 * PongPacket pong = Synapse.to("survival")
 *     .<PongPacket>request(new PingPacket())
 *     .get();
 *
 * Synapse.toPlayer(playerId).persistent().send(new AlertPacket("hi"));
 * }</pre>
 *
 * @since 1.1
 */
public interface SingleTarget {

    /** @return this target, requiring an acknowledgement */
    SingleTarget requireAck();

    /**
     * @param millis acknowledgement timeout in milliseconds
     * @return this target
     */
    SingleTarget timeout(long millis);

    /**
     * @param retries number of retransmits if no ack arrives
     * @return this target
     */
    SingleTarget retries(int retries);

    /**
     * @param priority outbound scheduling priority
     * @return this target
     */
    SingleTarget priority(PacketPriority priority);

    /**
     * @param millis time-to-live in milliseconds
     * @return this target
     */
    SingleTarget ttl(long millis);

    /** @return this target, with store-and-forward enabled for offline delivery */
    SingleTarget persistent();

    /**
     * @param key   header name
     * @param value header value
     * @return this target
     */
    SingleTarget header(String key, String value);

    /**
     * Sends the packet with the accumulated options.
     *
     * @param packet the packet
     * @return the send result
     */
    SendResult send(Packet packet);

    /**
     * Sends a request and returns a future for the typed response.
     *
     * @param packet the request packet
     * @param <R>    the expected response type
     * @return a future completing with the response
     */
    <R extends Packet> CompletableFuture<R> request(Packet packet);
}
