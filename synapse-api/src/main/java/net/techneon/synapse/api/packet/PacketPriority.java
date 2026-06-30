package net.techneon.synapse.api.packet;

/**
 * Relative ordering for {@link PacketHandler} methods and for outbound packet
 * scheduling, modeled after Bukkit's {@code EventPriority}.
 *
 * <p>When several handlers are registered for the same packet type, they are
 * invoked in ascending priority order: {@link #LOWEST} first and
 * {@link #MONITOR} last. {@code MONITOR} is intended purely for observation
 * (logging, metrics) and handlers at that level should not modify state or
 * respond to the packet.
 *
 * @since 1.0
 */
public enum PacketPriority {

    /** Called first; lowest importance. */
    LOWEST(0),

    /** Called early. */
    LOW(1),

    /** Default priority for handlers that do not specify one. */
    NORMAL(2),

    /** Called late. */
    HIGH(3),

    /** Called last among acting handlers; highest importance. */
    HIGHEST(4),

    /**
     * Called after every other priority, strictly for observation. Handlers at
     * this level must not change outcomes or respond.
     */
    MONITOR(5);

    private final int slot;

    PacketPriority(int slot) {
        this.slot = slot;
    }

    /**
     * Returns the dispatch slot, where a lower number runs earlier. Useful for
     * sorting handlers without relying on {@link #ordinal()}.
     *
     * @return the zero-based ordering slot
     */
    public int getSlot() {
        return slot;
    }
}
