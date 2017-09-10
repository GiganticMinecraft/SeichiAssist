package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.wimbli.WorldBorder.CoordXZ;
import com.wimbli.WorldBorder.WorldBorder;
import com.wimbli.WorldBorder.WorldFillTask;
import io.monchi.regenworld.RegenWorld;
import io.monchi.regenworld.event.RegenWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Mon_chi
 */
public class WorldRegenListener implements Listener {

    private int roadY;
    private int roadLength;
    private int spaceHeight;

    public WorldRegenListener() {
        Config config = SeichiAssist.config;
        this.roadLength = config.getRoadY();
        this.roadLength = config.getRoadLength();
        this.spaceHeight = config.getSpaceHeight();
    }

    @EventHandler
    public void onWorldRegen(RegenWorldEvent event) {
        World world = Bukkit.getWorld(event.getWorldName());
        BukkitWorld bukkitWorld = new BukkitWorld(world);

        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("showDeathMessages", "false");

        world.setSpawnLocation(8, 71, 8);
        Location spawn = world.getSpawnLocation();
        RegenWorld.getInstance().getHandler().getController().setSpawnLocation(world.getName(), spawn);

        com.wimbli.WorldBorder.Config.setBorder(world.getName(), 3000, 3000, spawn.getX(), spawn.getZ());
        com.wimbli.WorldBorder.Config.fillTask = new WorldFillTask(Bukkit.getServer(), null, world.getName(), CoordXZ.chunkToBlock(13), 1, 1, false);
        if (com.wimbli.WorldBorder.Config.fillTask.valid()) {
            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(WorldBorder.plugin, com.wimbli.WorldBorder.Config.fillTask, 1, 1);
            com.wimbli.WorldBorder.Config.fillTask.setTaskID(task);
        }

        WorldEdit worldEdit = WorldEdit.getInstance();
        EditSession session = worldEdit.getEditSessionFactory().getEditSession(bukkitWorld, 99999999);
        try {
            BaseBlock roadBlock = new BaseBlock(98, 0);
            BaseBlock spaceBlock = new BaseBlock(0);
            Vector roadBase = new Vector(9, roadY, 9);
            Vector spaceBase = new Vector(9, roadY + 1, 9);
            session.setBlocks(new CuboidRegion(bukkitWorld, new Vector(0, roadY + 1, 0), new Vector(16, 76, 16)), spaceBlock);
            session.setBlocks(new CuboidRegion(bukkitWorld, roadBase, new Vector(16 * roadLength + 16, roadY, 6)), roadBlock);
            session.setBlocks(new CuboidRegion(bukkitWorld, spaceBase, new Vector(16 * roadLength + 16, roadY + spaceHeight, 6)), spaceBlock);
            session.setBlocks(new CuboidRegion(bukkitWorld, roadBase, new Vector(-(16 * roadLength), roadY, 6)), roadBlock);
            session.setBlocks(new CuboidRegion(bukkitWorld, spaceBase, new Vector(-(16 * roadLength), roadY + spaceHeight, 6)), spaceBlock);
            session.setBlocks(new CuboidRegion(bukkitWorld, roadBase, new Vector(6, roadY, 16 * roadLength + 16)), roadBlock);
            session.setBlocks(new CuboidRegion(bukkitWorld, spaceBase, new Vector(6, roadY + spaceHeight, 16 * roadLength + 16)), spaceBlock);
            session.setBlocks(new CuboidRegion(bukkitWorld, roadBase, new Vector(6, roadY, -(16 * roadLength))), roadBlock);
            session.setBlocks(new CuboidRegion(bukkitWorld, spaceBase, new Vector(6, roadY + spaceHeight, -(16 * roadLength))), spaceBlock);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }
}
