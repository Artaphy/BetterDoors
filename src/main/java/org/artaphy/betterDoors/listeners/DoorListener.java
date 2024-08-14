package org.artaphy.betterDoors.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public class DoorListener implements Listener {
    private final boolean openingIronDoorsWithHands;
    private final boolean knocking;
    private final boolean knockingRequireEmptyHand;
    private final boolean knockingRequireSneaking;

    public DoorListener(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        this.openingIronDoorsWithHands = config.getBoolean("opening-irondoors-with-hands", true);
        this.knocking = config.getBoolean("knocking", true);
        this.knockingRequireEmptyHand = config.getBoolean("knocking-require-empty-hand", true);
        this.knockingRequireSneaking = config.getBoolean("knocking-require-sneaking", true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        boolean isDoor = block.getState().getBlockData() instanceof Door;

        if (knocking && event.getAction() == Action.LEFT_CLICK_BLOCK && isDoor) {
            if (!knockingRequireEmptyHand || event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
                if (!knockingRequireSneaking || event.getPlayer().isSneaking()) {
                    Sound knockSound = block.getType() == Material.IRON_DOOR
                            ? Sound.BLOCK_IRON_DOOR_OPEN
                            : Sound.ITEM_SHIELD_BLOCK;
                    event.getPlayer().playSound(block.getLocation(), knockSound, 0.8f, 1.0f);
                    event.setCancelled(true);
                }
            }
        }

        if (block.getType() != Material.IRON_DOOR) return;
        if (!openingIronDoorsWithHands) return;

        Door door = (Door) block.getBlockData();
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND
                && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            toggleDoor(block, door, player);
            event.setCancelled(true);
        }
    }

    private void toggleDoor(Block block, Door door, Player player) {
        boolean wasOpen = door.isOpen();
        door.setOpen(!wasOpen);
        block.setBlockData(door);

        Block otherHalf = door.getHalf() == Bisected.Half.BOTTOM ? block.getRelative(BlockFace.UP) : block.getRelative(BlockFace.DOWN);
        if (otherHalf.getBlockData() instanceof Door otherDoor) {
            otherDoor.setOpen(!wasOpen);
            otherHalf.setBlockData(otherDoor);
        }

        Sound sound = wasOpen ? Sound.BLOCK_IRON_DOOR_CLOSE : Sound.BLOCK_IRON_DOOR_OPEN;
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }
}
