# Security &amp; TLS Setup

Synapse secures every inter-server connection with **mutual TLS (mTLS)**:
confidentiality, authentication, and integrity, with no public domain, no paid
certificate authority, and no OpenSSL dependency. Servers trust each other
directly — trust is based on **certificate possession**, not on domain ownership
or network topology, so it works identically whether your servers are on one
machine, one datacenter, or scattered across the world.

As a plugin developer you never touch any of this. If you care whether a
particular connection is secured, read `event.isEncrypted()`. Everything below is
for server operators.

## How it works

1. **Auto-generated identity.** On first boot with `tls-enabled: true`, Synapse
   generates a self-signed certificate and private key (via Bouncy Castle) under
   `plugins/Synapse/security/`. No manual steps.
2. **Mutual authentication.** Both ends present their certificate during the TLS
   handshake; a peer is accepted only if its certificate is in the local trusted
   set.
3. **Enrollment.** New servers join the trusted set with a short-lived,
   single-use **enrollment token** — no copying certificate files around.
4. **Defense in depth.** An optional `shared-token` is checked during the
   application handshake in addition to TLS.

```
plugins/Synapse/security/
  node-key.pem      # this node's private key  (keep secret)
  node-cert.pem     # this node's certificate
  trusted/          # one .pem per trusted peer (managed automatically)
```

## The enrollment workflow

Inspired by tools like Tailscale: instead of exchanging certificate files, you
pass a short token from an existing server to the joining one.

Suppose `lobby-1` is already running and you're adding `survival-1`.

### 1. On an existing, trusted server — issue a token

```
/synapse enroll
```

```
Enrollment token: XJ4K-9P2L
Valid for 5 minutes, single use.
On the new server, run:  /synapse join XJ4K-9P2L
```

This opens a brief enrollment window on `lobby-1`.

### 2. On the new server — join with the token

Make sure `survival-1`'s `config.yml` lists `lobby-1` under `servers`, then:

```
/synapse join XJ4K-9P2L
```

`survival-1` presents the token to `lobby-1` on its next connection. The
handshake completes, **both servers enroll each other's certificate
automatically**, and the trust is persisted to `trusted/`. The token is consumed
and cannot be reused.

### 3. Verify

```
/synapse status
```

You should see the peer as `CONNECTED` with a measured latency.

> On a proxy (BungeeCord/Velocity) the command is `/synapseproxy enroll`,
> `/synapseproxy join <token>`, etc. — a distinct name so it doesn't shadow the
> backend servers' `/synapse` command.

### Whole-network trust (automatic)

You only enroll a new server with **one** existing node — you do **not** have to
run `enroll`/`join` against every other server. Synapse propagates trust across
the mesh automatically (a "trust gossip"):

- When a node learns to trust a new peer's certificate, it shares that
  certificate with all of its other already-trusted peers.
- When two nodes connect, they exchange their full set of trusted certificates.

So if `server-1` and `server-2` already trust each other and you join `server-3`
through `server-2`, `server-1` learns and pins `server-3`'s certificate too —
without you doing anything on `server-1`. The trust set converges to the whole
network within seconds. (For a node to actually *connect* to `server-3`, it still
needs `server-3` listed in its `servers:` config — gossip handles trust, config
handles routing.)

This is a deliberate **transitive trust** model suited to a private cluster: by
trusting one node you trust the network it vouches for. Trust-sync is only ever
accepted over an already mutually-authenticated TLS connection, so an outside
party cannot inject certificates.

## Why a token *and* a shared token?

- **Enrollment token** — one-time, short-lived; establishes *trust* (which
  certificates are accepted) when a new node joins.
- **Shared token** (`security.shared-token`) — long-lived, identical on every
  node; checked on every handshake as an extra secret in addition to TLS. Optional
  but recommended for defense in depth.

## Rotating certificates

If you suspect a key was compromised, regenerate this node's identity:

```
/synapse rotate-certs
```

```
TLS identity rotated. Peers must re-enroll this server.
```

The node gets a brand-new certificate, so its peers no longer trust it until you
re-run the [enrollment workflow](#the-enrollment-workflow) to re-establish trust.

## Operational notes

- **Keep `node-key.pem` secret.** It is this server's private identity. Back up
  the `security/` folder if you want to preserve trust across reinstalls.
- **Firewall the Synapse port** so only your own servers can reach it.
- **Trust model.** During an open enrollment window, unknown certificates are
  accepted at the TLS layer specifically so a joining node can complete the
  handshake; the application handshake still requires a valid token (or, for the
  joining side, that it initiated the join) before a peer is trusted and pinned.
  Outside an enrollment window, only already-trusted certificates are accepted.
  Keep enrollment windows short (they expire automatically after 5 minutes) and
  only run `enroll`/`join` when you are actively adding a server.
- **Plaintext mode.** Setting `tls-enabled: false` disables all of the above and
  sends traffic unencrypted. Use it only on a trusted private network, ideally
  with a `shared-token` set.

## For developers

The only security-related API surface is read-only:

```java
@PacketHandler
public void onSensitive(SecretPacket packet, PacketEvent event) {
    if (!event.isEncrypted()) {
        event.setCancelled(true); // refuse to act on an unencrypted link
        return;
    }
    // … safe to proceed
}
```
