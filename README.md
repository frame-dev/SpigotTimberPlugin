# 🌲 SpigotTimber

<img src="logo.png" width="200" align="right" alt="SpigotTimber Logo">

**SpigotTimber** is a lightweight Minecraft plugin that adds a fun and efficient tree-chopping mechanic. When enabled, breaking a single log causes the entire tree to fall — just like timber! It also supports optional leaf decay, item durability handling, and natural item drops.

## 📦 Features

- ✅ Toggleable timber mode per player (`/timber`)
- 🌳 Automatically breaks all connected logs in a tree
- 💨 Optional natural drop behavior
- 🍃 Optional leaf decay for realism
- ⛏ Proper tool durability reduction (axes only)
- 🧹 Configurable dropped item remover

## 📥 Installation

1. Download the plugin JAR file.
2. Drop it into the `plugins` folder of your Spigot or Paper server.
3. Restart or reload your server.

## ⚙️ Configuration

```yaml
# config.yml

dropNaturally: true

removeItem:
  use: true
  timer: 5 # in minutes
