package ch.framedev.spigottimber;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * SpigotTimber is a plugin that allows players to break trees and drop logs.   
 * It also allows players to remove items from the world.
 * 
 * @author FrameDev
 * @version 1.0
 * @since 1.0
 */
public class SpigotTimber extends JavaPlugin implements Listener {

    private final HashSet<String> timberEnabled = new HashSet<>();

    @Override
    public void onEnable() {

        // Create the config.yml file if it doesn't exist
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Register the timber command
        PluginCommand timberCommand = getCommand("timber");
        if (timberCommand != null)
            timberCommand.setExecutor(this);
        // Enable or disable the item removal task
        if (getConfig().getBoolean("removeItem.use"))
            removeItems();
        getLogger().info("TimberPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TimberPlugin has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        // Enable or disable the timber mode
        if (args.length == 0) {
            if (!timberEnabled.contains(player.getName())) {
                timberEnabled.add(player.getName());
                player.sendMessage("Timber mode enabled!");
            } else {
                timberEnabled.remove(player.getName());
                player.sendMessage("Timber mode disabled!");
            }
            return true;
        } else if (args.length == 1) {
            // Drop logs naturally or not
            if (args[0].equalsIgnoreCase("drop")) {
                if (!getConfig().getBoolean("dropNaturally")) {
                    getConfig().set("dropNaturally", true);
                    saveConfig();
                    player.sendMessage("Naturally dropping logs enabled!");
                } else {
                    getConfig().set("dropNaturally", false);
                    saveConfig();
                    player.sendMessage("Naturally dropping logs disabled!");
                }
            }
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }

    // Check if the block is a log
    private boolean isLog(Block block) {
        Material material = block.getType();
        return material == Material.OAK_LOG || material == Material.SPRUCE_LOG ||
                material == Material.BIRCH_LOG || material == Material.JUNGLE_LOG ||
                material == Material.ACACIA_LOG || material == Material.DARK_OAK_LOG ||
                material == Material.MANGROVE_LOG || material == Material.CHERRY_LOG;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isSneaking()) {
            if (timberEnabled.contains(event.getPlayer().getName())) {
                Block block = event.getBlock();

                // Check if the block is a log
                if (isLog(block)) {
                    // Break the tree
                    breakTree(block, event.getPlayer().getInventory().getItemInMainHand(), new HashSet<>(),
                            block.getLocation()); // Start breaking the tree
                }
            }
        }
    }

    // Break the tree and drop the log
    private void breakTree(Block block, ItemStack itemStack, Set<Block> visited, Location dropLocation) {
        if (!isLog(block) || visited.contains(block)) {
            return; // Stop if the block is not a log or has already been visited
        }
        if (getConfig().getBoolean("dropNaturally")) {
            block.getWorld().dropItem(block.getLocation(), new ItemStack(block.getType())); // Drop the log item
        } else {
            block.getWorld().dropItem(dropLocation, new ItemStack(block.getType())); // Drop the log item
        }
        visited.add(block); // Mark this block as visited
        block.setType(Material.AIR); // Manually break the block
        block.getWorld().spawnParticle(Particle.BLOCK_DUST, block.getLocation(),
                125, block.getBlockData());
        reduceDurability(itemStack);

        // Check all adjacent blocks (in 3D space)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue; // Skip the original block
                    }
                    Block relativeBlock = block.getRelative(dx, dy, dz);
                    breakTree(relativeBlock, itemStack, visited, dropLocation); // Recursively break adjacent logs
                }
            }
        }
        decaySurroundingLeaves(block);
    }

    // Reduce the durability of the item
    private void reduceDurability(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().getMaxDurability() == 0) {
            return; // Return if the item is null or doesn't have durability (like blocks)
        }

        if (!itemStack.getType().name().endsWith("_AXE")) {
            return; // Only axes should lose durability
        }

        ItemMeta meta = itemStack.getItemMeta();

        if (meta instanceof Damageable damageable) {
            int currentDamage = damageable.getDamage(); // Get current damage (inverse of durability)

            // Increment the damage by 1 (reduces remaining durability by 1)
            int newDamage = currentDamage + 1;

            // If the new damage exceeds or equals the max durability, break the item
            if (newDamage >= itemStack.getType().getMaxDurability()) {
                itemStack.setAmount(0); // Break the item by setting its amount to 0
            } else {
                damageable.setDamage(newDamage); // Set the new damage
                itemStack.setItemMeta(meta); // Apply the updated meta
            }
        }
    }

    // Decay surrounding leaves
    private void decaySurroundingLeaves(Block log) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int x = -3; x <= 3; x++) {
                    for (int y = -3; y <= 3; y++) {
                        for (int z = -3; z <= 3; z++) {
                            Block nearby = log.getRelative(x, y, z);
                            if (nearby.getType() == Material.OAK_LEAVES || nearby.getType() == Material.BIRCH_LEAVES ||
                                    nearby.getType() == Material.SPRUCE_LEAVES
                                    || nearby.getType() == Material.JUNGLE_LEAVES ||
                                    nearby.getType() == Material.ACACIA_LEAVES
                                    || nearby.getType() == Material.DARK_OAK_LEAVES) {
                                nearby.breakNaturally();
                            }
                        }
                    }
                }
            }
        }.runTaskLater(this, 10L); // Delay by half a second for more natural decay
    }

    // Remove items from the world
    public void removeItems() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getType() == EntityType.DROPPED_ITEM)
                            entity.remove();
                    }
                }
                getLogger().info("Items removed from all worlds");
            }
        }.runTaskTimer(this, 0, 20L * 60 * getConfig().getInt("removeItem.timer"));
    }
}
