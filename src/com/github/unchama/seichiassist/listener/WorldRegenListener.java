package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
    private BaseBlock roadBlock;
    private BaseBlock spaceBlock;

    private WorldEdit worldEdit;
    private WorldGuardPlugin worldGuard;

    public WorldRegenListener() {
        Config config = SeichiAssist.config;
        this.roadY = config.getRoadY();
        this.roadLength = config.getRoadLength();
        this.spaceHeight = config.getSpaceHeight();
        this.roadBlock = new BaseBlock(config.getRoadBlockID(), config.getRoadBlockDamage());
        this.spaceBlock = new BaseBlock(0);

        this.worldEdit = WorldEdit.getInstance();
        this.worldGuard = WorldGuardPlugin.inst();
    }

    @EventHandler
    public void onWorldRegen(RegenWorldEvent event) {
        World world = Bukkit.getWorld(event.getWorldName());
        BukkitWorld bukkitWorld = new BukkitWorld(world);

        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("showDeathMessages", "false");

        world.setSpawnLocation(8, 71, 8);
        Location spawn = world.getSpawnLocation();
        RegenWorld.getInstance().getController().setSpawnLocation(world.getName(), spawn);

        com.wimbli.WorldBorder.Config.setBorder(world.getName(), 3000, 3000, spawn.getX(), spawn.getZ());
        com.wimbli.WorldBorder.Config.fillTask = new WorldFillTask(Bukkit.getServer(), null, world.getName(), CoordXZ.chunkToBlock(13), 1, 1, false);
        if (com.wimbli.WorldBorder.Config.fillTask.valid()) {
            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(WorldBorder.plugin, com.wimbli.WorldBorder.Config.fillTask, 1, 1);
            com.wimbli.WorldBorder.Config.fillTask.setTaskID(task);
        }

        RegionManager regionManager = worldGuard.getRegionManager(world);
        regionManager.getRegions().keySet().stream()
                .filter(region -> region.equalsIgnoreCase("__global__"))
                .forEach(regionManager::removeRegion);

        EditSession session = worldEdit.getEditSessionFactory().getEditSession(bukkitWorld, 99999999);
        try {
            setupRoad(session, world, "spawn", new BlockVector(0, roadY, 0), new BlockVector(15, roadY, 15));
            setupRoad(session, world, "road1", new BlockVector(16, roadY, 0), new BlockVector(15 + 16 * roadLength, roadY, 15));
            setupRoad(session, world, "road2", new BlockVector(-1, roadY, 0), new BlockVector(-(16 * roadLength), roadY, 15));
            setupRoad(session, world, "road3", new BlockVector(0, roadY, 16), new BlockVector(15, roadY, 15 + 16 * roadLength));
            setupRoad(session, world, "road4", new BlockVector(0, roadY, -1), new BlockVector(15, roadY, -(16 * roadLength)));
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    private void setupRoad(EditSession session, World world, String protName, BlockVector pos1, BlockVector pos2) throws MaxChangedBlocksException {
        BukkitWorld bukkitWorld = new BukkitWorld(world);
        session.setBlocks(new CuboidRegion(bukkitWorld, pos1, pos2), roadBlock);
        session.setBlocks(new CuboidRegion(bukkitWorld, pos1.add(0, 1, 0), pos2.add(0, 1 + spaceHeight, 0)), spaceBlock);
        ProtectedRegion region = new ProtectedCuboidRegion(protName, new BlockVector(pos1.getX(), 0, pos1.getZ()), new BlockVector(pos2.getX(), 255, pos2.getZ()));
        WorldGuardPlugin.inst().getRegionManager(world).addRegion(region);
    }
}
