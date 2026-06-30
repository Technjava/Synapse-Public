package net.techneon.synapse.api.packet;

/**
 * Marker interface implemented by every message that travels across the Synapse
 * network.
 *
 * <p>A packet is just a plain Java object &mdash; there is no mandatory method to
 * implement. By default packets are serialized with Kryo, so a no-argument
 * constructor and ordinary fields are usually all you need:
 *
 * <pre>{@code
 * public class PlayerTransferPacket implements Packet {
 *     private String playerName;
 *     private String targetServer;
 *
 *     public PlayerTransferPacket() { } // required by Kryo
 *
 *     public PlayerTransferPacket(String playerName, String targetServer) {
 *         this.playerName = playerName;
 *         this.targetServer = targetServer;
 *     }
 *
 *     public String getPlayerName()   { return playerName; }
 *     public String getTargetServer() { return targetServer; }
 * }
 * }</pre>
 *
 * <p>Each packet type must be registered once (per namespace) before it can be
 * sent or received, so that both ends agree on a stable numeric id instead of
 * relying on fragile fully-qualified class names:
 *
 * <pre>{@code
 * Synapse.register(PlayerTransferPacket.class);
 * }</pre>
 *
 * <p>Developers who need byte-level control can bypass Kryo entirely by
 * supplying a {@link PacketSerializer} at registration time.
 *
 * @see PacketListener
 * @see PacketSerializer
 * @since 1.0
 */
public interface Packet {
}
