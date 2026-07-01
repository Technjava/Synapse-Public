# Configuration Reference

Synapse uses one `config.yml` per server, stored in the plugin's data folder
(`plugins/Synapse/config.yml` on Bukkit/Bungee, `plugins/synapse/config.yml` on
Velocity). The **same format is used on every platform**. A documented default is
written on first start.

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

network:
  reconnect-interval-ms: 5000
  heartbeat-interval-ms: 10000
  connect-timeout-ms: 5000

security:
  tls-enabled: true
  shared-token: ""
```

## `server` — this node's identity

| Key | Type | Default | Description |
|---|---|---|---|
| `name` | string | generated | Unique logical name. Other servers address this node by this name. **Set this.** |
| `group` | string | _(none)_ | Optional logical cluster, used by `broadcastToGroup`. |
| `bind` | string | `0.0.0.0` | Local interface to listen on. `0.0.0.0` = all interfaces. |
| `port` | int | `25600` | TCP port Synapse listens on. Must be reachable by peers. |
| `advertised-host` | string | _(auto)_ | The host other servers should use to reach this node, shared during auto-discovery. Leave empty to let peers use the address they observe. Set it when behind NAT or on multiple interfaces. |

## `servers` — peers to connect to

A list of remote servers this node opens persistent connections to. Each entry:

| Key | Type | Required | Description |
|---|---|---|---|
| `name` | string | yes | The peer's logical name (must match its `server.name`). |
| `host` | string | yes | IP or DNS name to reach the peer. |
| `port` | int | no (`25600`) | The peer's `server.port`. |
| `group` | string | no | The peer's group. Lets this node target it via `broadcastToGroup`. |

Connections are bidirectional. Listing a peer on one side is enough to form a
link; listing it on both sides simply re-establishes the link faster after a
restart. Duplicate links (both sides connecting) are harmless.

## `network` — connection behaviour

| Key | Type | Default | Description |
|---|---|---|---|
| `reconnect-interval-ms` | long | `5000` | Delay between reconnect attempts after a link drops. |
| `heartbeat-interval-ms` | long | `10000` | How often a heartbeat is sent, used for latency measurement and dead-link detection. |
| `connect-timeout-ms` | long | `5000` | How long to wait for an outbound connection to establish. |

## `security` — mutual TLS

| Key | Type | Default | Description |
|---|---|---|---|
| `tls-enabled` | bool | `true` | Encrypt and mutually authenticate every connection. Strongly recommended. |
| `shared-token` | string | `""` | Optional shared secret checked during the handshake as defense in depth, in addition to TLS. Keep it identical on every node. Empty disables it. |

When `tls-enabled` is true, Synapse generates and stores its identity under
`plugins/Synapse/security/`:

```
security/
  node-key.pem      # this node's private key
  node-cert.pem     # this node's self-signed certificate
  trusted/          # one .pem per trusted peer (managed by enrollment)
```

You never edit these by hand — use `/synapse enroll` / `/synapse join` to manage
trust. See [Security & TLS](security.md).

## Ports and firewalls

Open the configured `port` between your servers (and only between them). Synapse
needs a single TCP port per node; no extra infrastructure or broker is required.

## Admin commands

| Platform | Command |
|---|---|
| Spigot / Paper | `/synapse <status\|ping\|enroll\|join\|rotate-certs>` |
| BungeeCord / Velocity | `/synapseproxy <…>` (alias `/psyn`) |

Subcommands:

| Subcommand | Description |
|---|---|
| `status` | Status: uptime, listen address, TLS fingerprint, and every peer's state, direction, latency, encryption, and address. |
| `debug` | The most detailed diagnostic dump: node, network, security, registry, runtime queues, servers (with source), distributed maps, and network players. |
| `ping <server>` | Multi-sample round-trip test with min/avg/max and packet loss. |
| `enroll` | Issue a single-use enrollment token (opens a short trust window). |
| `join <token>` | Join an existing network using a token from another node. |
| `rotate-certs` | Regenerate this server's TLS identity. |
| `reload` | Re-read `config.yml` and reconcile the server list (add/remove peers). |

Command output is colour-coded on every platform.

The proxy command is deliberately **not** `/synapse`: on a proxy, a `/synapse`
command would be intercepted before it could reach a backend server that also
runs Synapse. Permission: `synapse.admin`.
