package net.techneon.synapse.api.packet;

import java.io.IOException;

/**
 * Opt-in hook for taking full, byte-level control of how a particular packet
 * type is written to and read from the wire, bypassing the default Kryo
 * serialization.
 *
 * <p>Supply an instance when registering the packet type to use a hand-rolled or
 * proprietary binary format &mdash; useful for squeezing out maximum performance
 * or for interoperating with an existing protocol:
 *
 * <pre>{@code
 * Synapse.register(PositionPacket.class, new PacketSerializer<PositionPacket>() {
 *     public void write(PositionPacket p, DataOutput out) throws IOException {
 *         out.writeDouble(p.x); out.writeDouble(p.y); out.writeDouble(p.z);
 *     }
 *     public PositionPacket read(DataInput in) throws IOException {
 *         return new PositionPacket(in.readDouble(), in.readDouble(), in.readDouble());
 *     }
 * });
 * }</pre>
 *
 * <p>Both ends of a connection must register the same serializer for the packet
 * type; otherwise deserialization will fail.
 *
 * @param <T> the packet type this serializer handles
 * @since 1.0
 */
public interface PacketSerializer<T extends Packet> {

    /**
     * Writes the packet's fields to the given output.
     *
     * @param packet the packet to serialize; never {@code null}
     * @param out    the destination to write bytes to
     * @throws IOException if writing fails
     */
    void write(T packet, java.io.DataOutput out) throws IOException;

    /**
     * Reconstructs a packet instance from the given input. The bytes available
     * are exactly those produced by a prior {@link #write} call for one packet.
     *
     * @param in the source to read bytes from
     * @return the reconstructed packet; never {@code null}
     * @throws IOException if reading fails or the data is malformed
     */
    T read(java.io.DataInput in) throws IOException;
}
