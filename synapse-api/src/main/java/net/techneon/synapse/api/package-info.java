/**
 * Synapse's public, platform-agnostic API.
 *
 * <p>This package and its sub-packages are the only types third-party plugins
 * should compile against. They are deliberately free of any Netty, Kryo, or
 * platform (Bukkit/BungeeCord/Velocity) dependency, so a plugin written against
 * them runs unchanged on every supported platform and server version.
 *
 * <p>Start with the static {@link net.techneon.synapse.api.Synapse} facade:
 *
 * <pre>{@code
 * Synapse.register(MyPacket.class);
 * Synapse.registerListener(new MyListener());
 * Synapse.send("survival", new MyPacket());
 * }</pre>
 *
 * <ul>
 *   <li>{@code packet} &mdash; packets, listeners, handlers, events, filters,
 *       custom serialization.</li>
 *   <li>{@code send} &mdash; send options, results, acknowledgements.</li>
 *   <li>{@code server} &mdash; topology: servers, groups, status.</li>
 *   <li>{@code interceptor} &mdash; middleware pipeline.</li>
 *   <li>{@code channel} &mdash; named pub/sub channels.</li>
 *   <li>{@code event} &mdash; lifecycle hooks.</li>
 *   <li>{@code exception} &mdash; the exception hierarchy.</li>
 * </ul>
 *
 * @since 1.0
 */
package net.techneon.synapse.api;
