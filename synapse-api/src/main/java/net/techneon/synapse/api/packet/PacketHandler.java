package net.techneon.synapse.api.packet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method inside a {@link PacketListener} as a handler for incoming
 * packets, mirroring Bukkit's {@code @EventHandler}.
 *
 * <p>The annotated method must be {@code public} and declare the packet type as
 * its first parameter. A second {@link PacketEvent} parameter is optional and
 * unlocks metadata and reply support:
 *
 * <pre>{@code
 * {@literal @}PacketHandler
 * public void onTransfer(PlayerTransferPacket packet) {
 *     // simple case: just the packet
 * }
 *
 * {@literal @}PacketHandler(priority = PacketPriority.HIGH)
 * public void onRequest(PingRequestPacket packet, PacketEvent event) {
 *     // advanced case: packet + metadata
 *     event.respond(new PingResponsePacket(System.currentTimeMillis()));
 * }
 * }</pre>
 *
 * <p>The handler's packet type also determines which packets it receives, so no
 * manual {@code instanceof} dispatch is ever required.
 *
 * @see PacketListener
 * @see PacketPriority
 * @see PacketEvent
 * @since 1.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketHandler {

    /**
     * The priority at which this handler is invoked relative to other handlers
     * for the same packet type.
     *
     * @return the handler priority; defaults to {@link PacketPriority#NORMAL}
     */
    PacketPriority priority() default PacketPriority.NORMAL;

    /**
     * When {@code true}, this handler is skipped for packets that a previous
     * handler has already cancelled via {@link PacketEvent#setCancelled(boolean)}.
     *
     * @return whether to ignore cancelled packets; defaults to {@code false}
     */
    boolean ignoreCancelled() default false;
}
