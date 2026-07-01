package net.techneon.synapse.api.packet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link PacketListener} class for automatic registration by
 * {@link net.techneon.synapse.api.SynapseScope#scan}. The class must have a
 * public no-argument constructor so it can be instantiated during the scan.
 *
 * <pre>{@code
 * @AutoListener
 * public class MyListener implements PacketListener {
 *     @PacketHandler public void onX(XPacket packet) { ... }
 * }
 *
 * Synapse.scan(MyPlugin.class, "com.example.myplugin.listeners");
 * }</pre>
 *
 * <p>Listeners that need constructor arguments (a plugin instance, services, …)
 * should be registered manually with {@code registerListener(...)} instead.
 *
 * @since 1.1
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoListener {

    /**
     * The namespace to register this listener in. Empty means the default
     * namespace.
     *
     * @return the namespace name, or {@code ""} for the default
     */
    String namespace() default "";
}
