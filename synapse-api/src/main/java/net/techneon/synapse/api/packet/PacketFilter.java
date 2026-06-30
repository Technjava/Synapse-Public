package net.techneon.synapse.api.packet;

/**
 * A declarative, fail-fast guard that inspects a decoded packet and decides
 * whether it is allowed to reach any {@link PacketHandler}.
 *
 * <p>Filters run after deserialization but before dispatch, making them the
 * right place to reject malformed or unauthorized packets cheaply:
 *
 * <pre>{@code
 * Synapse.addFilter(PlayerTransferPacket.class,
 *         (packet, event) -> packet.getPlayerName() != null);
 * }</pre>
 *
 * <p>Returning {@code false} drops the packet silently (it is reported through
 * the {@code onPacketDropped} lifecycle hook) and no handler is invoked.
 *
 * @param <T> the packet type this filter inspects
 * @see net.techneon.synapse.api.interceptor.Interceptor
 * @since 1.0
 */
@FunctionalInterface
public interface PacketFilter<T extends Packet> {

    /**
     * Decides whether the given packet should be delivered to handlers.
     *
     * @param packet the decoded packet
     * @param event  metadata about the delivery
     * @return {@code true} to allow the packet through, {@code false} to drop it
     */
    boolean accept(T packet, PacketEvent event);
}
