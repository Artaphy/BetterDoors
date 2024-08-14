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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // 导入 SLF4J 的 Logger

/**
 * 监听玩家与门交互的事件处理类
 */
public class DoorsListener implements Listener {

    private static final Logger logger = LoggerFactory.getLogger(DoorsListener.class); // 创建 Logger 实例
    /**
     * 是否启用双开门配置
     */
    private final boolean doubleDoors;

    /**
     * 构造函数，用于初始化DoorsListener
     *
     * @param plugin 插件实例，用于获取配置
     */
    public DoorsListener(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        this.doubleDoors = config.getBoolean("double-doors", true);
        logger.info("Initialized with double doors setting: {}", this.doubleDoors); // 添加初始化日志
    }

    /**
     * 处理玩家与方块交互事件
     *
     * @param event 交互事件
     */
    @EventHandler
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block == null) {
            logger.warn("Clicked block is null in PlayerInteractEvent.");
            return;
        }

        // 检查是否为右击门方块，并且双开门功能已启用
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getBlockData() instanceof Door && this.doubleDoors) {
            logger.debug("Handling door interaction for block at {}", block.getLocation());
            handleDoorInteraction(block);
        }
    }

    /**
     * 处理门的交互逻辑
     *
     * @param block 被交互的门方块
     */
    private void handleDoorInteraction(Block block) {
        Door door = (Door) block.getBlockData();
        BlockFace face = door.getFacing();
        boolean opened = !door.isOpen();
        Door.Hinge hinge = door.getHinge();

        BlockFace[] adjacentFaces = getAdjacentFaces(face);

        // 遍历相邻方块，寻找双开门的另一半
        for (BlockFace adjacentFace : adjacentFaces) {
            Block neighbourBlock = block.getRelative(adjacentFace);
            if (neighbourBlock.getBlockData() instanceof Door neighbourDoor) {
                // 检查相邻门是否为双开门的另一半，并设置其开闭状态
                if (neighbourDoor.getHinge() != hinge && neighbourDoor.getFacing() == face) {
                    neighbourDoor.setOpen(opened);
                    neighbourBlock.setBlockData(neighbourDoor);
                    logger.debug("Set door at {} to {}", neighbourBlock.getLocation(), opened ? "open" : "closed");

                    // 处理双开门的上下两部分
                    Block otherHalf = neighbourDoor.getHalf() == Bisected.Half.BOTTOM ?
                            neighbourBlock.getRelative(BlockFace.UP) : neighbourBlock.getRelative(BlockFace.DOWN);
                    if (otherHalf.getBlockData() instanceof Door otherDoor) {
                        otherDoor.setOpen(opened);
                        otherHalf.setBlockData(otherDoor);
                        logger.debug("Set door at {} to {}", otherHalf.getLocation(), opened ? "open" : "closed");
                    }
                }
            }
        }

        // 处理当前门方块的上下两部分
        Block otherHalf = door.getHalf() == Bisected.Half.BOTTOM ?
                block.getRelative(BlockFace.UP) : block.getRelative(BlockFace.DOWN);
        if (otherHalf.getBlockData() instanceof Door otherDoor) {
            otherDoor.setOpen(opened);
            otherHalf.setBlockData(otherDoor);
            logger.debug("Set door at {} to {}", otherHalf.getLocation(), opened ? "open" : "closed");
        }
    }

    /**
     * 根据门的朝向获取相邻方块的方向
     *
     * @param face 门的朝向
     * @return 相邻方块的两个方向
     */
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