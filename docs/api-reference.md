# API Reference

Everything here lives under `net.techneon.synapse.api`. The static
[`Synapse`](#the-synapse-facade) facade is the entry point; it forwards to the
running engine. For dependency injection or testing, call `Synapse.get()` to hold
the `SynapseAPI` instance directly.

- [The `Synapse` facade](#the-synapse-facade)
- [Packets](#packets)
- [Listeners and handlers](#listeners-and-handlers)
- [The `PacketEvent`](#the-packetevent)
- [Sending](#sending)
- [Send options](#send-options)
- [Acknowledgements](#acknowledgements)
- [Request / response](#request--response)
- [Topology and groups](#topology-and-groups)
- [Advanced](#advanced)
  - [Interceptors](#interceptors)
  - [Filters](#filters)
  - [Named pub/sub channels](#named-pubsub-channels)
  - [Namespaces](#namespaces)
  - [Custom serialization](#custom-serialization)
  - [Lifecycle hooks](#lifecycle-hooks)
  - [Low-level Netty access](#low-level-netty-access)
- [Threading](#threading)
- [Exceptions](#exceptions)

---

## The `Synapse` facade

```java
Synapse.isAvailable();           // is Synapse running? (for soft-depends)
SynapseAPI api = Synapse.get();  // the underlying instance

Synapse.register(MyPacket.class);
Synapse.registerListener(new MyListener());
Synapse.send("survival", new MyPacket());
```

Every static method throws `IllegalStateException` if Synapse is not running, so
guard optional integrations with `Synapse.isAvailable()`.

---

## Packets

A packet is any class implementing `Packet`. With the default Kryo serialization
you just need a no-arg constructor and ordinary fields.

```java
public class PingRequestPacket implements Packet {
    public PingRequestPacket() { }
}
```

Register each type **once per server** (on both senders and receivers) before
sending or receiving it:

```java
Synapse.register(PingRequestPacket.class);          // auto-assigned stable id
Synapse.register(PingRequestPacket.class, 4201);    // explicit id
```

Ids default to a deterministic hash of the class name, so every node agrees on
them without sharing class files. Assign an explicit id only to resolve a rare
collision or to pin a value across refactors. Packets are identified on the wire
by this id, never by raw class name, so two unrelated plugins can't collide.

---

## Listeners and handlers

Implement `PacketListener` and annotate methods with `@PacketHandler`, exactly
like Bukkit's `Listener` / `@EventHandler`.

```java
public class MyListener implements PacketListener {

    @PacketHandler
    public void onTransfer(PlayerTransferPacket packet) {
        // simple case: just the packet
    }

    @PacketHandler(priority = PacketPriority.HIGH)
    public void onRequest(PingRequestPacket packet, PacketEvent event) {
        // advanced case: packet + metadata
        event.respond(new PingResponsePacket(System.currentTimeMillis()));
    }
}

Synapse.registerListener(new MyListener());
```

- The first parameter's type selects which packets the method receives — no
  manual `instanceof`.
- An optional second `PacketEvent` parameter unlocks metadata and replies.
- `@PacketHandler(priority = …)` orders handlers from `LOWEST` to `MONITOR`.
- `@PacketHandler(ignoreCancelled = true)` skips packets a prior handler cancelled.

Handlers are dispatched through cached `MethodHandle`s, not reflection, so the
per-packet overhead is minimal.

---

## The `PacketEvent`

The optional second handler parameter. It carries everything Synapse knows about
the delivery and lets you reply or cancel.

| Method | Returns |
|---|---|
| `getSource()` | `ServerInfo` of the sender |
| `getSourceGroup()` | `Optional<String>` |
| `getSentTimestamp()` / `getReceivedTimestamp()` | epoch millis |
| `getLatencyMillis()` | apparent one-way latency |
| `getPacketId()` / `getProtocolVersion()` | ints |
| `isEncrypted()` | `true` if the link is mTLS |
| `getHeader(key)` / `getHeaders()` | custom headers |
| `respond(packet)` | reply straight to the sender |
| `isCancelled()` / `setCancelled(boolean)` | cancellation |

```java
@PacketHandler
public void onSecure(SensitivePacket packet, PacketEvent event) {
    if (!event.isEncrypted()) { event.setCancelled(true); return; }
    String trace = event.getHeader("trace-id").orElse("none");
    event.respond(new AckPacket());
}
```

---

## Sending

All of these are on `Synapse` (default namespace) and on every `Namespace`.

```java
Synapse.send("survival", packet);                          // one server
Synapse.broadcast(packet);                                 // every connected server
Synapse.broadcastToGroup("survival-cluster", packet);      // a group
Synapse.sendExcept(packet, "lobby");                       // all but some
Synapse.sendIf(packet, server -> server.getGroup()         // a predicate
        .filter("eu"::equals).isPresent());
```

`send` returns a [`SendResult`](#acknowledgements); the broadcast variants are
fire-and-forget.

---

## Send options

`SendOptions` is immutable; build it with the fluent builder and reuse it.

```java
SendOptions options = SendOptions.builder()
    .timeout(2000)                 // ack timeout (ms)
    .retries(3)                    // retransmits if no ack (implies requireAck)
    .requireAck(true)              // require an acknowledgement
    .priority(PacketPriority.HIGH) // outbound scheduling priority
    .ttl(5000)                     // drop if undelivered within 5s
    .header("trace-id", traceId)   // custom header, readable by the receiver
    .build();

Synapse.send("survival", packet, options);
```

`SendOptions.defaults()` is what the no-options overloads use: 5s timeout, no
retries, no ack, `NORMAL` priority, no TTL.

---

## Acknowledgements

When a send requires an ack, the `SendResult` exposes the outcome as both a
chainable callback and a `CompletableFuture`.

```java
Synapse.send("survival", packet, options)
    .onAck(ack -> getLogger().info(
        ack.getRespondingServer().getName() + " acked in " + ack.getRoundTripMillis() + "ms"))
    .onTimeout(() -> getLogger().warning("no ack received"))
    .onError(err -> getLogger().warning("send failed: " + err.getMessage()));
```

The same information is also delivered as an event, for teams that prefer the
listener style:

```java
@PacketHandler
public void onAck(PacketAckEvent event) {
    AckResult result = event.getResult();
    // result.getRespondingServer(), getRoundTripMillis(), getResponse()
}
```

`AckResult.getResponse()` is present when the receiver attached a reply via
`event.respond(...)`.

---

## Request / response

A typed round trip, built on acknowledgements.

```java
// non-blocking
CompletableFuture<PingResponsePacket> future =
    Synapse.sendRequest("survival", new PingRequestPacket());
future.thenAccept(pong -> getLogger().info("pong: " + pong.getTime()));

// blocking (never call on a server main thread)
PingResponsePacket pong =
    Synapse.sendRequestBlocking("survival", new PingRequestPacket(), 5000);
```

The responder simply calls `event.respond(...)` from its handler:

```java
@PacketHandler
public void onPing(PingRequestPacket req, PacketEvent event) {
    event.respond(new PingResponsePacket(System.currentTimeMillis()));
}
```

---

## Topology and groups

```java
ServerInfo me = Synapse.getLocalServer();

for (ServerInfo s : Synapse.getServers()) {
    s.getName();          // logical name
    s.getStatus();        // CONNECTED / CONNECTING / DISCONNECTED
    s.isConnected();
    s.getLatencyMillis(); // -1 if unknown
    s.getGroup();         // Optional<String>
}

Synapse.getServer("survival").ifPresent(s -> { /* … */ });

for (ServerGroup g : Synapse.getGroups()) {
    g.getName();
    g.getServers();
    g.hasConnectedMember();
}
```

Groups are declared in `config.yml` (`server.group` and each peer's `group`).

---

## Advanced

### Interceptors

Middleware that observes or vetoes every packet, before serialization on the way
out and before any filter/handler on the way in. Great for logging, metrics,
tracing, or rate limiting. Registered globally (across namespaces).

```java
Synapse.addInterceptor(new Interceptor() {
    @Override public boolean beforeSend(InterceptContext ctx) {
        ctx.getHeaders().put("trace-id", newTraceId());
        return true;            // false cancels the send
    }
    @Override public boolean beforeReceive(InterceptContext ctx) {
        return rateLimiter.tryAcquire(ctx.getRemoteServer()); // false drops it
    }
});
```

`InterceptContext` exposes `getDirection()`, `getPacket()`, `getRemoteServer()`,
and a mutable `getHeaders()` map.

### Filters

Declarative, per-type guards that reject malformed packets before any handler
runs. Returning `false` drops the packet (reported via `onPacketDropped`).

```java
Synapse.addFilter(PlayerTransferPacket.class,
    (packet, event) -> packet.getPlayerName() != null);
```

### Named pub/sub channels

A Redis-like, loosely typed alternative to typed packets.

```java
Subscription sub = Synapse.subscribe("chat-broadcast", message ->
    Bukkit.broadcastMessage(message.getPayloadAs(String.class)));

Synapse.publish("chat-broadcast", "Hello from " + serverName);

sub.close(); // stop receiving
```

`ChannelMessage` gives `getChannel()`, `getSource()`, `getPayload()`, and the
typed `getPayloadAs(Class)`. Payloads are serialized with Kryo and may be any
serializable object. Published messages reach subscribers on other servers.

### Namespaces

Isolate multiple plugins that all use Synapse on the same server. Packet ids,
listeners, filters, and channels in one namespace are invisible to another.

```java
Namespace mine = Synapse.namespace("my-plugin");
mine.register(MyPacket.class);
mine.registerListener(new MyListener());
mine.send("survival", new MyPacket());
```

A `Namespace` has the full sending/registration API (`SynapseScope`); calling
`Synapse.namespace(name)` again with the same name returns the same instance.
The default scope used by the static facade is the `"default"` namespace.

### Custom serialization

Bypass Kryo for a specific packet — for maximum performance or a proprietary
binary format. Both ends must register the same serializer.

```java
Synapse.register(PositionPacket.class, new PacketSerializer<PositionPacket>() {
    public void write(PositionPacket p, DataOutput out) throws IOException {
        out.writeDouble(p.x); out.writeDouble(p.y); out.writeDouble(p.z);
    }
    public PositionPacket read(DataInput in) throws IOException {
        return new PositionPacket(in.readDouble(), in.readDouble(), in.readDouble());
    }
});
```

### Lifecycle hooks

```java
Synapse.addLifecycleListener(new LifecycleListener() {
    @Override public void onReady() { }
    @Override public void onShutdown() { }
    @Override public void onServerConnect(ServerInfo server) { }
    @Override public void onServerDisconnect(ServerInfo server) { }
    @Override public void onPacketDropped(Packet packet, String reason) { }
});
```

Every method has a no-op default; override only what you need.

### Low-level Netty access

For power users who need raw control:

```java
Optional<Object> channel = Synapse.get().getUnderlyingChannel("survival");
```

The returned object is the relocated `io.netty.channel.Channel` for that
connection. Don't close it — Synapse owns its lifecycle.

---

## Threading

Handler methods, interceptors, filters, subscribers, and ack/timeout callbacks
run on Synapse's internal I/O and scheduler threads, **not** a server main
thread. If you need to touch game state (Bukkit world, players, etc.), hop back
onto the main thread with your platform's scheduler:

```java
@PacketHandler
public void onTransfer(PlayerTransferPacket packet) {
    Bukkit.getScheduler().runTask(plugin, () -> {
        Player p = Bukkit.getPlayer(packet.getPlayerName());
        // … safe to touch the world here
    });
}
```

`sendRequestBlocking` blocks the calling thread; never call it from the main
thread.

---

## Exceptions

All extend `SynapseException` (unchecked):

| Exception | When |
|---|---|
| `UnknownServerException` | sending to a server name not in `config.yml` |
| `PacketRegistrationException` | id collision or sending an unregistered type |
| `SerializationException` | a packet can't be (de)serialized |

Connectivity failures (peer offline) surface through `SendResult.onError` /
`asFuture()` for ack-required sends, or via `onPacketDropped` for fire-and-forget.
