# Availe — Kotlin Multiplatform NLIP Client and Gateway

**Availe** is an unofficial Kotlin Multiplatform implementation of the Natural Language Interaction Protocol (NLIP), designed to simplify experimenting with NLIP across Android, iOS, Desktop, Web (Wasm), and Server platforms.

---

## What is NLIP?

The Natural Language Interaction Protocol (NLIP) is an open, structured protocol for exchanging messages in natural language format. While is primarily intended for use with LLMs, it can technically also support non-AI applications, such as an echo server. NLIP is currently a draft standard at ECMA TC-56.

- [Official NLIP GitHub](https://github.com/nlip-project)
- [NLIP Website](https://nlip-project.org/#/)

---

## Project Status

Availe is at an early stage. It's functional but rapidly evolving, featuring a basic UI and no persistent conversation history yet.

---

## Features

- Cross-platform support: Android, iOS, Desktop, Web (Wasm), and Server.
- Built-in adapter for local LLM (Ollama).
- Compatible with official NLIP servers and SDK.
- Minimal but usable chat interface.

---

## Quick Start

Make sure you have [Ollama](https://ollama.com/) installed along with the following models:

- `granite3-moe:latest` *(used by Availe)*
- `granite3.2:2b` *(used by some official NLIP solutions)*

Then run Availe:

```bash
# Start Availe server:
./gradlew :server:run

# Desktop UI:
./gradlew :composeApp:run

# Web UI (Wasm):
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

By default, Availe’s client UI points to:

```
http://localhost:8010/nlip
```

If you're running Availe’s built-in Ollama adapter, start chatting immediately. Otherwise, point it to your desired NLIP endpoint.

---

## Using Official NLIP Solutions

Availe works seamlessly with official NLIP implementations provided in these repositories:

- [nlip_soln](https://github.com/nlip-project/nlip_soln) *(example solutions)*
- [nlip_server](https://github.com/nlip-project/nlip_server) *(NLIP server implementation)*
- [nlip_sdk](https://github.com/nlip-project/nlip_sdk) *(SDK)*

**Important:**  
Clone all three into the same parent directory:

```bash
git clone https://github.com/nlip-project/nlip_server.git
git clone https://github.com/nlip-project/nlip_sdk.git
git clone https://github.com/nlip-project/nlip_soln.git
```

Start an official NLIP solution (example: MCP):

```bash
cd nlip_soln
poetry install
poetry run start-mcp
```

Then, set Availe’s UI to point to the official MCP endpoint:

```
http://localhost:8010/nlip
```

To view the API documentation for your connected NLIP endpoint, open:

```
http://localhost:8010/docs
```

*(Other solutions: `start-chat`, `start-chat2`, `start-integration`, `start-echo`.)*

---

## Roadmap

- Conversation persistence
- Streaming (server-sent events)
- Docker Compose deployment
- UI improvements

---

## License

Licensed under Apache 2.0 — see [LICENSE](LICENSE).
