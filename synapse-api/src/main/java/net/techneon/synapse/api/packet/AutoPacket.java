package net.techneon.synapse.api.packet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link Packet} class for automatic registration by
 * {@link net.techneon.synapse.api.SynapseScope#scan}.
 *
 * <p>Instead of calling {@code Synapse.register(...)} for every packet, annotate
 * the class and scan the package once:
 *
 * <pre>{@code
 * @AutoPacket
 * public class PlayerTransferPacket implements Packet { ... }
 *
 * // at start-up:
 * Synapse.scan(MyPlugin.class, "com.example.myplugin.packets");
 * }</pre>
 *
 * @since 1.1
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoPacket {

    /**
     * An explicit wire id. Leave as {@code 0} to derive a stable id from the
     * class name (the default and recommended behaviour).
     *
     * @return the explicit packet id, or {@code 0} to auto-derive
     */
    int id() default 0;

    /**
     * The namespace to register this packet in. Empty means the default
     * namespace.
     *
     * @return the namespace name, or {@code ""} for the default
     */
    String namespace() default "";
}
