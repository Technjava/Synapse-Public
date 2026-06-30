# Getting Started

This guide takes you from zero to your first cross-server packet in a few
minutes.

## 1. Install the plugin

Build the project (`mvn clean install`) or grab the release jars, then drop the
jar matching each server's platform into its `plugins/` folder:

| Platform | Jar |
|---|---|
| Spigot / **Paper** / Purpur | `Synapse-Bukkit-1.0.0.jar` |
| BungeeCord / Waterfall | `Synapse-Bungee-1.0.0.jar` |
| Velocity | `Synapse-Velocity-1.0.0.jar` |

> **Paper users:** there is no separate "Paper build" because there doesn't need
> to be. Paper is a fork of Spigot and runs Bukkit plugins unchanged, so the
> `Synapse-Bukkit` jar is the Paper jar — drop it straight into `plugins/`. The
> same applies to Purpur, Pufferfish, and other Spigot/Paper forks.

Start each server once. Synapse generates a default `config.yml` and, on first
boot, a self-signed TLS certificate — you never touch certificate files by hand.

## 2. Configure `config.yml`

Open `plugins/Synapse/config.yml` on each server and set its identity and the
peers it should connect to. A minimal two-server setup:

**Lobby (`lobby-1`):**

```yaml
server:
  name: "lobby-1"
  group: "lobby-cluster"
  bind: "0.0.0.0"
  port: 25600
servers:
  - name: "survival-1"
    host: "127.0.0.1"
    port: 25601
    group: "survival-cluster"
security:
  tls-enabled: true
  shared-token: "pick-a-shared-secret"
```

**Survival (`survival-1`):**

```yaml
server:
  name: "survival-1"
  group: "survival-cluster"
  bind: "0.0.0.0"
  port: 25601
servers:
  - name: "lobby-1"
    host: "127.0.0.1"
    port: 25600
    group: "lobby-cluster"
security:
  tls-enabled: true
  shared-token: "pick-a-shared-secret"
```

Connections are bidirectional — you only have to list a peer on one side, but
listing it on both reconnects faster. See the
[Configuration Reference](configuration.md) for every option.

> **First-time TLS:** the two servers don't trust each other's certificate yet.
> Run `/synapse enroll` on one and `/synapse join <token>` on the other to
> establish trust automatically. See [Security & TLS](security.md). To try things
> out without TLS, set `tls-enabled: false` on both.

Run `/synapse status` (or `/synapseproxy status` on a proxy) to see connection
state and latency.

## 3. Add the API to your plugin

```xml
<dependency>
  <groupId>net.techneon</groupId>
  <artifactId>synapse-api</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
</dependency>
```

Make Synapse load before your plugin by declaring it as a dependency (Bukkit
`plugin.yml`: `depend: [Synapse]` or `softdepend: [Synapse]`).

## 4. Define a packet

A packet is a plain class implementing `Packet`. A no-arg constructor is required
by the default Kryo serialization.

```java
import net.techneon.synapse.api.packet.Packet;

public class PlayerTransferPacket implements Packet {
    private String playerName;
    private String targetServer;

    public PlayerTransferPacket() { } // required by Kryo

    public PlayerTransferPacket(String playerName, String targetServer) {
        this.playerName = playerName;
        this.targetServer = targetServer;
    }

    public String getPlayerName()   { return playerName; }
    public String getTargetServer() { return targetServer; }
}
```

## 5. Register, listen, and send

```java
import net.techneon.synapse.api.Synapse;
import net.techneon.synapse.api.packet.*;

public final class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // register the packet type on every server that sends OR receives it
        Synapse.register(PlayerTransferPacket.class);

        // register your listener
        Synapse.registerListener(new TransferListener());
    }

    public void requestTransfer(Player player) {
        Synapse.send("survival-1", new PlayerTransferPacket(player.getName(), "survival-1"));
    }
}

public class TransferListener implements PacketListener {
    @PacketHandler
    public void onTransfer(PlayerTransferPacket packet) {
        getLogger().info(packet.getPlayerName() + " is heading to " + packet.getTargetServer());
    }
}
```

That's the whole loop: **register → listen → send**. The same packet class must
be registered on both the sending and receiving servers so they agree on its id.

## Next steps

- [Send options, ACKs, and request/response](api-reference.md#sending)
- [Groups, broadcasts, and topology](api-reference.md#topology-and-groups)
- [Interceptors, filters, namespaces, custom serialization](api-reference.md#advanced)
- [Securing the network with mTLS](security.md)

Progress checks: run `/synapse status`.
