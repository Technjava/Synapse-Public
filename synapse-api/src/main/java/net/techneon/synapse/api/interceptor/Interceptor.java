package net.techneon.synapse.api.interceptor;

/**
 * A middleware-style hook that observes or vetoes packets as they flow through
 * Synapse, before serialization on the way out and before deserialized packets
 * reach any filter or handler on the way in.
 *
 * <p>Interceptors are ideal for cross-cutting concerns &mdash; structured
 * logging, metrics, tracing, rate limiting, or attaching/inspecting headers:
 *
 * <pre>{@code
 * Synapse.addInterceptor(new Interceptor() {
 *     {@literal @}Override
 *     public boolean beforeSend(InterceptContext ctx) {
 *         ctx.getHeaders().put("trace-id", newTraceId());
 *         return true; // allow the send to proceed
 *     }
 *
 *     {@literal @}Override
 *     public boolean beforeReceive(InterceptContext ctx) {
 *         return rateLimiter.tryAcquire(ctx.getRemoteServer());
 *     }
 * });
 * }</pre>
 *
 * <p>Returning {@code false} from either method cancels processing of that
 * packet; a cancelled outbound packet is never sent, and a cancelled inbound
 * packet never reaches a handler (it is reported via the {@code onPacketDropped}
 * lifecycle hook). Interceptors run in registration order.
 *
 * @since 1.0
 */
public interface Interceptor {

    /**
     * Called for every outbound packet before it is serialized and queued.
     *
     * @param context the mutable outbound context
     * @return {@code true} to allow the send, {@code false} to cancel it
     */
    default boolean beforeSend(InterceptContext context) {
        return true;
    }

    /**
     * Called for every inbound packet after deserialization but before filters
     * and handlers run.
     *
     * @param context the mutable inbound context
     * @return {@code true} to allow delivery, {@code false} to drop the packet
     */
    default boolean beforeReceive(InterceptContext context) {
        return true;
    }
}
