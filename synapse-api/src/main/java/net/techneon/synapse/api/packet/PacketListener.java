package net.techneon.synapse.api.packet;

/**
 * Marker interface for a class that contains one or more {@link PacketHandler}
 * methods, mirroring Bukkit's {@code Listener} convention.
 *
 * <p>Implement this interface, annotate handler methods with
 * {@link PacketHandler}, then register the instance:
 *
 * <pre>{@code
 * public class MyListener implements PacketListener {
 *
 *     {@literal @}PacketHandler
 *     public void onTransfer(PlayerTransferPacket packet) {
 *         // react to the packet
 *     }
 * }
 *
 * Synapse.registerListener(new MyListener());
 * }</pre>
 *
 * <p>A handler method takes the packet as its first parameter and may declare an
 * optional second {@link PacketEvent} parameter for metadata and reply support.
 *
 * @see PacketHandler
 * @see PacketEvent
 * @since 1.0
 */
public interface PacketListener {
}
