package net.techneon.synapse.api.send;

/**
 * Event-driven counterpart to the {@link SendResult#onAck} callback: when a
 * packet sent with {@code requireAck(true)} is acknowledged, Synapse also
 * dispatches a {@code PacketAckEvent} to any registered
 * {@link net.techneon.synapse.api.packet.PacketHandler} that accepts it.
 *
 * <p>This lets teams that prefer the listener style receive acknowledgements the
 * same way they receive packets:
 *
 * <pre>{@code
 * {@literal @}PacketHandler
 * public void onAck(PacketAckEvent event) {
 *     getLogger().info(event.getResult().getRespondingServer().getName()
 *         + " acked in " + event.getResult().getRoundTripMillis() + "ms");
 * }
 * }</pre>
 *
 * @since 1.0
 */
public interface PacketAckEvent {

    /**
     * The id correlating this acknowledgement to the original send. The same id
     * is available on the {@link SendResult} returned by the send call.
     *
     * @return the correlation id
     */
    long getCorrelationId();

    /**
     * The acknowledgement outcome: responding server, round-trip time, and any
     * reply payload.
     *
     * @return the acknowledgement result; never {@code null}
     */
    AckResult getResult();
}
