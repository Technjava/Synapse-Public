package net.techneon.synapse.api.target;

import net.techneon.synapse.api.packet.Packet;
import net.techneon.synapse.api.packet.PacketPriority;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A fluent builder for sending to many destinations at once (a group, everyone,
 * everyone-except, or a predicate), including scatter-gather requests.
 *
 * <pre>{@code
 * Synapse.toGroup("survival-cluster").send(packet);
 *
 * // scatter-gather: ask a whole group and collect every reply
 * List<CountPacket> counts = Synapse.toAll()
 *     .<CountPacket>requestAll(new CountRequest())
 *     .get();
 * int total = counts.stream().mapToInt(CountPacket::getCount).sum();
 * }</pre>
 *
 * @since 1.1
 */
public interface MultiTarget {

    /** @return this target, requiring an acknowledgement per recipient */
    MultiTarget requireAck();

    /**
     * @param millis acknowledgement / gather timeout in milliseconds
     * @return this target
     */
    MultiTarget timeout(long millis);

    /**
     * @param retries number of retransmits if no ack arrives
     * @return this target
     */
    MultiTarget retries(int retries);

    /**
     * @param priority outbound scheduling priority
     * @return this target
     */
    MultiTarget priority(PacketPriority priority);

    /**
     * @param millis time-to-live in milliseconds
     * @return this target
     */
    MultiTarget ttl(long millis);

    /** @return this target, with store-and-forward enabled for offline recipients */
    MultiTarget persistent();

    /**
     * @param key   header name
     * @param value header value
     * @return this target
     */
    MultiTarget header(String key, String value);

    /**
     * @return the number of currently-connected servers this target matches
     */
    int size();

    /**
     * Sends the packet to every matched server.
     *
     * @param packet the packet
     */
    void send(Packet packet);

    /**
     * Scatter-gather: sends a request to every matched server and completes with
     * the list of responses received before the timeout elapses. Servers that do
     * not answer in time are simply omitted from the list.
     *
     * @param packet the request packet
     * @param <R>    the expected response type
     * @return a future completing with all collected responses
     */
    <R extends Packet> CompletableFuture<List<R>> requestAll(Packet packet);
}
