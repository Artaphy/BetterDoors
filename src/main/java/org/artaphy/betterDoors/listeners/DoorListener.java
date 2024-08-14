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

/**
 * 监听器类，用于处理门的相关交互事件
 */
public class DoorListener implements Listener {
    // 敲门和开门的声音音量和音调常量
    private static final float KNOCK_SOUND_VOLUME = 0.8f;
    private static final float KNOCK_SOUND_PITCH = 1.0f;
    private static final float DOOR_TOGGLE_SOUND_VOLUME = 1.0f;
    private static final float DOOR_TOGGLE_SOUND_PITCH = 1.0f;

    // 配置选项：是否允许用手打开铁门
    private final boolean openingIronDoorsWithHands;
    // 配置选项：是否启用敲门功能
    private final boolean knocking;
    // 配置选项：敲门时是否需要空手
    private final boolean knockingRequireEmptyHand;
    // 配置选项：敲门时是否需要蹲伏
    private final boolean knockingRequireSneaking;

    /**
     * 构造函数，根据插件的配置初始化DoorListener
     * @param plugin JavaPlugin实例，用于获取配置
     */
    public DoorListener(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        this.openingIronDoorsWithHands = config.getBoolean("opening-irondoors-with-hands", true);
        this.knocking = config.getBoolean("knocking", true);
        this.knockingRequireEmptyHand = config.getBoolean("knocking-require-empty-hand", true);
        this.knockingRequireSneaking = config.getBoolean("knocking-require-sneaking", true);
    }

    /**
     * 当玩家与环境交互时触发的事件处理函数
     * 主要处理玩家敲门和用手开关铁门的逻辑
     * @param event 交互事件
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        // 敲门逻辑
        if (knocking && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (!knockingRequireEmptyHand || event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
                if (!knockingRequireSneaking || event.getPlayer().isSneaking()) {
                    playKnockSound(block, event.getPlayer());
                    event.setCancelled(true);
                }
            }
        }

        // 用手开关铁门逻辑
        if (block.getType() != Material.IRON_DOOR) return;
        if (!openingIronDoorsWithHands) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
            toggleIronDoor(block, event.getPlayer());
            event.setCancelled(true);
        }
    }

    /**
     * 播放敲门声音
     * @param block 被敲击的方块
     * @param player 敲门的玩家
     */
    private void playKnockSound(Block block, Player player) {
        Sound knockSound = block.getType() == Material.IRON_DOOR
                ? Sound.BLOCK_IRON_DOOR_OPEN
                : Sound.ITEM_SHIELD_BLOCK;
        player.playSound(block.getLocation(), knockSound, KNOCK_SOUND_VOLUME, KNOCK_SOUND_PITCH);
    }

    /**
     * 切换铁门的开闭状态
     * @param block 铁门方块
     * @param player 操作的玩家
     */
    private void toggleIronDoor(Block block, Player player) {
        Door door = getDoorFromBlock(block);
        if (door == null) return;

        boolean wasOpen = door.isOpen();
        door.setOpen(!wasOpen);
        block.setBlockData(door);

        toggleOtherHalf(block, door, !wasOpen);

        playDoorToggleSound(player, wasOpen);
    }

    /**
     * 切换铁门的另一半的开闭状态
     * @param block 当前操作的铁门方块
     * @param door 当前操作的铁门数据
     * @param isOpen 目标开闭状态
     */
    private void toggleOtherHalf(Block block, Door door, boolean isOpen) {
        Bisected.Half half = door.getHalf();
        if (half == null) return;

        Block otherHalf = half == Bisected.Half.BOTTOM ? block.getRelative(BlockFace.UP) : block.getRelative(BlockFace.DOWN);
        Door otherDoor = getDoorFromBlock(otherHalf);
        if (otherDoor != null) {
            otherDoor.setOpen(isOpen);
            otherHalf.setBlockData(otherDoor);
        }
    }

    /**
     * 播放开门或关门声音
     * @param player 操作的玩家
     * @param wasOpen 门之前的开闭状态
     */
    private void playDoorToggleSound(Player player, boolean wasOpen) {
        Sound sound = wasOpen ? Sound.BLOCK_IRON_DOOR_CLOSE : Sound.BLOCK_IRON_DOOR_OPEN;
        player.playSound(player.getLocation(), sound, DOOR_TOGGLE_SOUND_VOLUME, DOOR_TOGGLE_SOUND_PITCH);
    }

    /**
     * 从方块数据中获取门信息
     * @param block 方块
     * @return Door门数据，如果方块数据不是门类型则返回null
     */
    private Door getDoorFromBlock(Block block) {
        return (Door) block.getBlockData();
    }
}