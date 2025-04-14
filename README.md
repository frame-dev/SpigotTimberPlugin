# 🌲 SpigotTimber

<img src="logo.png" width="200" align="right" alt="SpigotTimber Logo">

**SpigotTimber** is a lightweight Minecraft plugin that adds a fun and efficient tree-chopping mechanic. When enabled, breaking a single log causes the entire tree to fall — just like timber! It also supports leaf decay, tool durability handling, and configurable item drop behavior.

## 📦 Features

- ✅ Toggleable timber mode per player (`/timber`)
- 🌳 Automatically breaks all connected logs in a tree
- 💨 Configurable drop behavior (at original location or natural drop)
- 🍃 Automatic leaf decay for realism
- ⛏ Proper tool durability reduction (axes only)
- 🧹 Configurable dropped item cleanup system

## 📥 Installation

1. Download the plugin JAR file.
2. Drop it into the `plugins` folder of your Spigot or Paper server.
3. Restart or reload your server.

## ⚙️ Configuration

```yaml
# config.yml

# Whether logs should drop naturally (at their original location)
# or at the location of the first broken block
dropNaturally: false

# Configure automatic item cleanup
removeItem:
  use: false         # Enable/disable automatic item cleanup
  timer: 25          # Cleanup interval in minutes
```
```