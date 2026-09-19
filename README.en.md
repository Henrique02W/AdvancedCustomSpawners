# 🐚 AdvancedCustomSpawners

[![Build](https://github.com/Henrique02W/AdvancedCustomSpawners/actions/workflows/build.yml/badge.svg)](https://github.com/Henrique02W/AdvancedCustomSpawners/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Henrique02W/AdvancedCustomSpawners?display_name=tag)](https://github.com/Henrique02W/AdvancedCustomSpawners/releases/latest)
![Minecraft](https://img.shields.io/badge/minecraft-26.2-brightgreen)
![Paper](https://img.shields.io/badge/paper-26.2-blue)
![Java](https://img.shields.io/badge/java-25-orange)
![License](https://img.shields.io/badge/license-Non--Commercial-blue)
![Vault](https://img.shields.io/badge/economy-Vault-yellow)

[🇧🇷 Português](README.md) · 🇺🇸 English

> 🐚 Custom spawners with upgrades, fuel, internal storage and routed drops.

---

## 📖 About

**AdvancedCustomSpawners** replaces vanilla spawners with a much more complete version: portable spawners that can be captured, upgraded, fueled, linked to containers for automatic drop delivery, and managed through a GUI with holograms and economy integration.

Design goals:

- Highly configurable (upgrades, fuels, blocked mobs, GUI items)
- Flexible drop destination (internal storage or a linked container)
- Integrated with the rest of the server (Vault, PlaceholderAPI)

> The default in-game messages and item texts are in Brazilian Portuguese (`pt_BR`). Everything is editable in `config.yml` and `messages.yml`.

---

## 🛠️ Features

- 🥚 **Mob capture**: filled or empty capture items to set the spawner's mob type
- ⚙️ **7 independent upgrades**, each with multiple levels and configurable costs:
  * ⚡ Speed (min/max spawn delay)
  * 🔢 Amount (mobs per spawn)
  * 📏 Range (player distance required to activate)
  * 🌍 Conditions (ignores vanilla environment rules — light, block, water, day/night)
  * 💎 Drops (item drop multiplier)
  * ✨ XP (experience multiplier)
  * 🔥 Fuel (reduced consumption)
- 🔥 **Fuel system**: coal, charcoal, blaze rods and lava buckets, each with its own duration and efficiency
- 📦 **Internal drop storage** with a configurable stack limit
- 🔗 **Container linking**: routes drops to a chest (or any container) within a configurable distance
- 🔀 **Configurable fallback modes** when the target container is full or missing (internal storage, pause, or drop on the ground)
- 🖼️ **Full GUI**: on/off toggle, item collection, link/unlink, removal and upgrades
- 💡 **Holograms** showing spawner info
- 💰 **Vault** integration for money-based upgrade costs (falls back to emeralds when Vault is unavailable)
- 🚫 Configurable **blocked mobs** list (Ender Dragon, Wither and Warden by default)
- 📋 Removing a spawner keeps its type, upgrades and stored fuel on the returned item
- 🧩 Optional **PlaceholderAPI** integration

---

## 🚀 Getting started

### Requirements

- **Paper 26.2** server
- **Java 25** or newer
- (Optional) Vault + an economy plugin, and/or PlaceholderAPI

> Starting with **2.0.0** the plugin only supports Minecraft 26.2 (Paper). The 1.x line, built for 1.21.8, is no longer supported.

### Installation

1. Download the latest `.jar` from [Releases](https://github.com/Henrique02W/AdvancedCustomSpawners/releases/latest)
2. Drop it into your server's `plugins/` folder
3. Start the server and edit `plugins/AdvancedCustomSpawners/config.yml` and `messages.yml`
4. Run `/spawner reload` to apply changes

### Upgrading from 1.x (1.21.8)

- Back up `plugins/AdvancedCustomSpawners/` (especially `data.yml`).
- The plugin name, internal keys and `data.yml` format are unchanged, so existing spawners and items should still be recognised. Test on a copy of your server first.
- Update the server to Paper 26.2 on Java 25 and replace the jar with the 2.x version.

### Building from source

Requires **JDK 25** and Maven.

```
git clone https://github.com/Henrique02W/AdvancedCustomSpawners.git
cd AdvancedCustomSpawners
mvn package
```

The jar is written to `target/AdvancedCustomSpawners-<version>.jar`.

---

## 🧠 Usage

The main command is `/spawner` (aliases: `/customspawner`, `/acs`).

- `/spawner give <player> <mob> [amount]` — give a custom spawner
- `/spawner give <player> <mob> [amount] egg` — give a filled capture item
- `/spawner giveempty <player> [amount]` — give an empty capture item
- `/spawner reload` — reload configuration
- `/spawner info` — show data for the spawner you are looking at
- `/spawner link` — start linking the targeted spawner to a container
- `/spawner unlink` — remove the link
- `/spawner remove` — remove the targeted spawner and return it to the player

### Permissions

| Permission                | Description                                  | Default |
| ------------------------- | -------------------------------------------- | ------- |
| `advancedspawners.use`    | Use the custom spawner GUIs                  | `true`  |
| `advancedspawners.give`   | Give spawners and capture items              | `op`    |
| `advancedspawners.reload` | Reload the configuration                     | `op`    |
| `advancedspawners.info`   | Read spawner info                            | `op`    |
| `advancedspawners.remove` | Remove spawners through the GUI or command   | `true`  |
| `advancedspawners.link`   | Link spawners to containers                  | `true`  |
| `advancedspawners.bypass` | Bypass some placement and type restrictions  | `op`    |
| `advancedspawners.admin`  | Access to all admin commands                 | `op`    |

### Placeholders

With PlaceholderAPI installed:

- `%advancedspawners_total%`
- `%advancedspawners_stored_items%`
- `%advancedspawners_pending_routes%`

---

## 🔒 License

This project is under the **Custom Non-Commercial Software License v1.0** (see [`LICENSE.md`](LICENSE.md); a Portuguese version is in [`LICENSE_pt.md`](LICENSE_pt.md), and the English version prevails in case of conflict).

⚠️ **Commercial use is strictly prohibited.** You may use it personally or for education, and fork and modify it. You may not sell the plugin or monetize any part of the project. For commercial use, contact the author.

## 🤝 Contributing & issues

See [`CONTRIBUTING.md`](CONTRIBUTING.md). Found a bug or have an idea? Open an [issue](https://github.com/Henrique02W/AdvancedCustomSpawners/issues) with your Paper and plugin versions and logs if possible.

## 📬 Contact

👤 **Henrique02W** — GitHub: <https://github.com/Henrique02W> · Discord: henrique02#7075
