# Examples

Copy-paste-ready recipes, from one-liners to advanced patterns. Bukkit is used
for illustration; the `Synapse` API is identical on every platform.

- [1. Cross-server broadcast message](#1-cross-server-broadcast-message)
- [2. Send a player's data on transfer](#2-send-a-players-data-on-transfer)
- [3. Request/response: query a server's online count](#3-requestresponse-query-a-servers-online-count)
- [4. Acknowledged delivery with retries](#4-acknowledged-delivery-with-retries)
- [5. Target a group, or everyone except one](#5-target-a-group-or-everyone-except-one)
- [6. Pub/sub channel for global chat](#6-pubsub-channel-for-global-chat)
- [7. Interceptor for tracing every packet](#7-interceptor-for-tracing-every-packet)
- [8. Filter out malformed packets](#8-filter-out-malformed-packets)
- [9. Namespacing your plugin](#9-namespacing-your-plugin)
- [10. Custom binary serialization](#10-custom-binary-serialization)
- [11. React to topology changes](#11-react-to-topology-changes)

---

## 1. Cross-server broadcast message

```java
public class AnnouncePacket implements Packet {
    private String message;
    public AnnouncePacket() { }
    public AnnouncePacket(String message) { this.message = message; }
    public String getMessage() { return message; }
}

// startup (every server)
Synapse.register(AnnouncePacket.class);
Synapse.registerListener(new AnnounceListener());

// send to all
Synapse.broadcast(new AnnouncePacket("Network restart in 5 minutes!"));
```

```java
public class AnnounceListener implements PacketListener {
    @PacketHandler
    public void onAnnounce(AnnouncePacket packet) {
        Bukkit.getScheduler().runTask(plugin, () ->
            Bukkit.broadcastMessage(packet.getMessage()));
    }
}
```

---

## 2. Send a player's data on transfer

```java
public class PlayerDataPacket implements Packet {
    private String name; private int coins; private String rank;
    public PlayerDataPacket() { }
    public PlayerDataPacket(String name, int coins, String rank) {
        this.name = name; this.coins = coins; this.rank = rank;
    }
    public String getName() { return name; }
    public int getCoins()  { return coins; }
    public String getRank() { return rank; }
}

// before sending the player to "survival-1"
Synapse.send("survival-1", new PlayerDataPacket(p.getName(), economy.get(p), perms.rank(p)));
```

```java
@PacketHandler
public void onData(PlayerDataPacket packet) {
    cache.put(packet.getName(), packet); // ready for when they arrive
}
```

---

## 3. Request/response: query a server's online count

```java
public class CountRequest implements Packet { public CountRequest() { } }

public class CountResponse implements Packet {
    private int count;
    public CountResponse() { }
    public CountResponse(int count) { this.count = count; }
    public int getCount() { return count; }
}
```

Responder:

```java
@PacketHandler
public void onCount(CountRequest req, PacketEvent event) {
    event.respond(new CountResponse(Bukkit.getOnlinePlayers().size()));
}
```

Requester (non-blocking):

```java
Synapse.<CountResponse>sendRequest("survival-1", new CountRequest())
    .thenAccept(resp -> getLogger().info("survival-1 has " + resp.getCount() + " players"))
    .exceptionally(err -> { getLogger().warning("query failed: " + err); return null; });
```

---

## 4. Acknowledged delivery with retries

Guarantee a critical packet is delivered (or you find out it wasn't):

```java
SendOptions reliable = SendOptions.builder()
    .requireAck(true)
    .timeout(1500)
    .retries(3)        // up to 3 retransmits before giving up
    .build();

Synapse.send("bank-server", new WithdrawPacket(player, 500), reliable)
    .onAck(ack    -> getLogger().info("confirmed in " + ack.getRoundTripMillis() + "ms"))
    .onTimeout(() -> rollback(player, 500))
    .onError(err  -> rollback(player, 500));
```

---

## 5. Target a group, or everyone except one

```java
Synapse.broadcastToGroup("survival-cluster", new EventStartPacket());

Synapse.sendExcept(new LobbyOnlyPacket(), "survival-1", "survival-2");

Synapse.sendIf(new RegionPacket(), server ->
    server.getGroup().filter("eu-cluster"::equals).isPresent());
```

---

## 6. Pub/sub channel for global chat

```java
// subscribe at startup
Synapse.subscribe("global-chat", message -> {
    String line = message.getPayloadAs(String.class);
    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(line));
});

// publish whenever a player chats
@EventHandler
public void onChat(AsyncPlayerChatEvent e) {
    Synapse.publish("global-chat", "[" + serverName + "] " + e.getPlayer().getName() + ": " + e.getMessage());
}
```

---

## 7. Interceptor for tracing every packet

```java
Synapse.addInterceptor(new Interceptor() {
    @Override public boolean beforeSend(InterceptContext ctx) {
        ctx.getHeaders().put("trace-id", UUID.randomUUID().toString());
        return true;
    }
    @Override public boolean beforeReceive(InterceptContext ctx) {
        getLogger().info("recv " + ctx.getPacket().getClass().getSimpleName()
            + " from " + ctx.getRemoteServer()
            + " trace=" + ctx.getHeader("trace-id").orElse("-"));
        return true;
    }
});
```

---

## 8. Filter out malformed packets

```java
Synapse.addFilter(PlayerDataPacket.class, (packet, event) ->
    packet.getName() != null && packet.getCoins() >= 0);
```

Rejected packets never reach a handler and are reported via `onPacketDropped`.

---

## 9. Namespacing your plugin

Avoid colliding with other plugins that also use Synapse:

```java
public final class MyPlugin extends JavaPlugin {
    private Namespace synapse;

    @Override public void onEnable() {
        synapse = Synapse.namespace("myplugin");
        synapse.register(MyPacket.class);
        synapse.registerListener(new MyListener());
    }

    public void ping(String server) {
        synapse.send(server, new MyPacket());
    }
}
```

---

## 10. Custom binary serialization

For a hot-path packet where you want full control over the bytes:

```java
public class PositionPacket implements Packet {
    public double x, y, z;
    public PositionPacket() { }
    public PositionPacket(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
}

Synapse.register(PositionPacket.class, new PacketSerializer<PositionPacket>() {
    public void write(PositionPacket p, DataOutput out) throws IOException {
        out.writeDouble(p.x); out.writeDouble(p.y); out.writeDouble(p.z);
    }
    public PositionPacket read(DataInput in) throws IOException {
        return new PositionPacket(in.readDouble(), in.readDouble(), in.readDouble());
    }
});
```

Register the *same* serializer on every server that handles the packet.

---

## 11. React to topology changes

```java
Synapse.addLifecycleListener(new LifecycleListener() {
    @Override public void onReady() {
        getLogger().info("Synapse is up; " + Synapse.getServers().size() + " peers configured");
    }
    @Override public void onServerConnect(ServerInfo s) {
        getLogger().info(s.getName() + " connected (" + (s.isConnected() ? "ok" : "?") + ")");
    }
    @Override public void onServerDisconnect(ServerInfo s) {
        getLogger().warning(s.getName() + " went away");
    }
});
```
