package org.artaphy.betterDoors.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DoorsListener implements Listener {

    private final boolean doubleDoors;

    public DoorsListener(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        this.doubleDoors = config.getBoolean("double-doors", true);
    }

    @EventHandler
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getBlockData() instanceof Door && this.doubleDoors) {
            handleDoorInteraction(block);
        }
    }

    private void handleDoorInteraction(Block block) {
        Door door = (Door) block.getBlockData();
        BlockFace face = door.getFacing();
        boolean opened = !door.isOpen();
        Door.Hinge hinge = door.getHinge();

        BlockFace[] adjacentFaces = getAdjacentFaces(face);

        for (BlockFace adjacentFace : adjacentFaces) {
            Block neighbourBlock = block.getRelative(adjacentFace);
            if (neighbourBlock.getBlockData() instanceof Door neighbourDoor) {
                if (neighbourDoor.getHinge() != hinge && neighbourDoor.getFacing() == face) {
                    neighbourDoor.setOpen(opened);
                    neighbourBlock.setBlockData(neighbourDoor);

                    Block otherHalf = neighbourDoor.getHalf() == Bisected.Half.BOTTOM ?
                            neighbourBlock.getRelative(BlockFace.UP) : neighbourBlock.getRelative(BlockFace.DOWN);
                    if (otherHalf.getBlockData() instanceof Door otherDoor) {
                        otherDoor.setOpen(opened);
                        otherHalf.setBlockData(otherDoor);
                    }
                }
            }
        }

        Block otherHalf = door.getHalf() == Bisected.Half.BOTTOM ?
                block.getRelative(BlockFace.UP) : block.getRelative(BlockFace.DOWN);
        if (otherHalf.getBlockData() instanceof Door otherDoor) {
            otherDoor.setOpen(opened);
            otherHalf.setBlockData(otherDoor);
        }
    }

    private BlockFace[] getAdjacentFaces(BlockFace face) {
        return switch (face) {
            case EAST -> new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH};
            case WEST -> new BlockFace[]{BlockFace.SOUTH, BlockFace.NORTH};
            case SOUTH -> new BlockFace[]{BlockFace.EAST, BlockFace.WEST};
            case NORTH -> new BlockFace[]{BlockFace.WEST, BlockFace.EAST};
            default -> new BlockFace[]{};
        };
    }
}
