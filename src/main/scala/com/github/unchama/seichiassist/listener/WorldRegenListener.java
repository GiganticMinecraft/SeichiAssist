package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wimbli.WorldBorder.CoordXZ;
import com.wimbli.WorldBorder.WorldBorder;
import com.wimbli.WorldBorder.WorldFillTask;
import io.monchi.regenworld.RegenWorld;
import io.monchi.regenworld.event.RegenWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Mon_chi
 */
public class WorldRegenListener implements Listener {

    private final int roadY;
    private final int roadLength;
    private final int spaceHeight;
    private final int worldSize;

    private final WorldEdit worldEdit;
    private final WorldGuardPlugin worldGuard;


    public WorldRegenListener() {
        Config config = SeichiAssist.seichiAssistConfig();
        this.roadY = config.getRoadY();
        this.roadLength = config.getRoadLength();
        this.spaceHeight = config.getSpaceHeight();
        this.worldSize = config.getWorldSize();

        this.worldEdit = WorldEdit.getInstance();
        this.worldGuard = WorldGuardPlugin.inst();
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onWorldRegen(RegenWorldEvent event) {
        World world = Bukkit.getWorld(event.getWorldName());
        BukkitWorld bukkitWorld = new BukkitWorld(world);

        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("showDeathMessages", "false");

        world.setSpawnLocation(8, 71, 8);
        Location spawn = world.getSpawnLocation();
        RegenWorld.getInstance().getController().setSpawnLocation(world.getName(), spawn);

        com.wimbli.WorldBorder.Config.setBorder(world.getName(), worldSize, worldSize, spawn.getX(), spawn.getZ());
        com.wimbli.WorldBorder.Config.fillTask = new WorldFillTask(Bukkit.getServer(), null, world.getName(), CoordXZ.chunkToBlock(13), 1, 1, false);
        if (com.wimbli.WorldBorder.Config.fillTask.valid()) {
            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(WorldBorder.plugin, com.wimbli.WorldBorder.Config.fillTask, 1, 1);
            com.wimbli.WorldBorder.Config.fillTask.setTaskID(task);
        }

        Collection<ProtectedRegion> regions = Objects.requireNonNull(WorldGuard.getInstance().getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(world)))
                .getRegions()
                .values();

        regions.forEach(region -> {
            Objects.requireNonNull(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world))).removeRegion(region.getId());
        });


        EditSession session = worldEdit.getEditSessionFactory().getEditSession(bukkitWorld, 99999999);
        try {
            // spawnの地形造成
            setupRoadWithWorldGuard(session, world, "spawn", BlockVector3.at(0, roadY, 0), BlockVector3.at(15, roadY, 15));
            // 東西南北へ続くroadの地形造成
            setupRoad(session, world, BlockVector3.at(16, roadY, 0), BlockVector3.at(15 + 16 * roadLength, roadY, 15));
            setupRoad(session, world, BlockVector3.at(-1, roadY, 0), BlockVector3.at(-(16 * roadLength), roadY, 15));
            setupRoad(session, world, BlockVector3.at(0, roadY, 16), BlockVector3.at(15, roadY, 15 + 16 * roadLength));
            setupRoad(session, world, BlockVector3.at(0, roadY, -1), BlockVector3.at(15, roadY, -(16 * roadLength)));
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    /**
     * 地形造成を行う
     *
     * @throws MaxChangedBlocksException
     */
    private void setupRoad(EditSession session, World world, BlockVector3 pos1, BlockVector3 pos2) throws MaxChangedBlocksException {
        BukkitWorld bukkitWorld = new BukkitWorld(world);
        session.setBlocks(new CuboidRegion(bukkitWorld, pos1, pos2), BukkitAdapter.adapt(Material.BEDROCK.createBlockData()));
        session.setBlocks(new CuboidRegion(bukkitWorld, pos1.add(0, 1, 0), pos2.add(0, 1 + spaceHeight, 0)), BukkitAdapter.adapt(Material.AIR.createBlockData()));
    }

    /**
     * 地形造成と、地形造成を行った場所にWorldGuardRegionも設定する
     *
     * @throws MaxChangedBlocksException
     */
    private void setupRoadWithWorldGuard(EditSession session, World world, String protName, BlockVector3 pos1, BlockVector3 pos2) throws MaxChangedBlocksException {
        BukkitWorld bukkitWorld = new BukkitWorld(world);
        session.setBlocks(new CuboidRegion(bukkitWorld, pos1, pos2), BukkitAdapter.adapt(Material.BEDROCK.createBlockData()));
        session.setBlocks(new CuboidRegion(bukkitWorld, pos1.add(0, 1, 0), pos2.add(0, 1 + spaceHeight, 0)), BukkitAdapter.adapt(Material.AIR.createBlockData()));
        ProtectedRegion region = new ProtectedCuboidRegion(protName, BlockVector3.at(pos1.getX(), 0, pos1.getZ()), BlockVector3.at(pos2.getX(), 255, pos2.getZ()));
        WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world)).addRegion(region);
    }
}
