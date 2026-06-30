# Synapse

**Fast, secure, event-driven inter-server messaging for Minecraft networks.**

Synapse opens direct, persistent, TLS-encrypted TCP connections between your
servers and gives plugin developers a Bukkit-style, event-driven API to send
typed packets between them — from a one-line broadcast to full low-level access
to the underlying Netty channel.

It runs on **Spigot, Paper, BungeeCord, and Velocity**, on any version, from a
single shared, platform-agnostic API.

```java
// 1. Register a packet and a listener (once, at start-up)
Synapse.register(PlayerTransferPacket.class);
Synapse.registerListener(new MyListener());

// 2. Send it anywhere
Synapse.send("survival", new PlayerTransferPacket(player.getName(), "survival"));
```

```java
public class MyListener implements PacketListener {
    @PacketHandler
    public void onTransfer(PlayerTransferPacket packet) {
        // react to it on the other side
    }
}
```

---

## Why Synapse?

| | Synapse | Redis pub/sub | Proxy plugin messaging |
|---|---|---|---|
| External infrastructure | **None** | Redis server | — |
| Transport | Direct TCP (Netty) | Via Redis broker | Via proxy only |
| Encryption | **Built-in mTLS** | Manual | None |
| Typed packets + listeners | **Yes** | DIY | DIY |
| Request/response & ACKs | **Yes** | DIY | DIY |
| Works without a proxy | **Yes** | Yes | No |

No Redis, no message broker, no public domain or paid certificate. Servers trust
each other directly via auto-generated certificates and a Tailscale-style
enrollment token.

---

## Documentation

- **[Getting Started](docs/getting-started.md)** — install, configure, first packet in under 10 lines.
- **[Configuration Reference](docs/configuration.md)** — every `config.yml` option.
- **[API Reference](docs/api-reference.md)** — packets, listeners, send options, ACKs, groups, interceptors, channels, namespaces.
- **[Examples](docs/examples.md)** — simple one-liners through advanced custom serialization.
- **[Security & TLS Setup](docs/security.md)** — mTLS, the enrollment token workflow, certificate rotation.

Every public class and method ships with full Javadoc.

---

## Modules

| Module | What it is |
|---|---|
| `synapse-api` | The stable, platform-agnostic API your plugin compiles against. No Netty/Bukkit leakage. |
| `synapse-core` | The engine: Netty transport, Kryo serialization, mTLS, packet dispatch. |
| `synapse-platform-bukkit` | Spigot/Paper plugin (`Synapse-Bukkit-x.y.z.jar`). |
| `synapse-platform-bungee` | BungeeCord/Waterfall plugin (`Synapse-Bungee-x.y.z.jar`). |
| `synapse-platform-velocity` | Velocity plugin (`Synapse-Velocity-x.y.z.jar`). |

Install the jar matching each server's platform. They all speak the same wire
protocol, so a Velocity proxy, a Paper lobby, and a Spigot survival server form
one Synapse network.

---

## Building

Requires JDK 11+ (the build targets Java 8 bytecode for the game-server modules
and Java 11 for the Velocity module) and Maven 3.8+.

```bash
mvn clean install
```

The runnable plugin jars are produced at:

```
synapse-platform-bukkit/target/Synapse-Bukkit-1.0.0.jar
synapse-platform-bungee/target/Synapse-Bungee-1.0.0.jar
synapse-platform-velocity/target/Synapse-Velocity-1.0.0.jar
```

All third-party libraries (Netty, Kryo, Bouncy Castle, SnakeYAML) are shaded and
relocated under `net.techneon.synapse.libs`, so Synapse never conflicts with
anything your server already bundles.

---

## Depending on the API in your plugin

```xml
<dependency>
  <groupId>net.techneon</groupId>
  <artifactId>synapse-api</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
</dependency>
```

Then declare Synapse as a (soft-)dependency of your plugin so it loads first, and
use the static `Synapse` facade. See **[Getting Started](docs/getting-started.md)**.

## License

See repository for license details.
