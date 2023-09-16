package com.github.unchama.util.external

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.{ProtectedCuboidRegion, ProtectedRegion}
import com.sk89q.worldguard.{LocalPlayer, WorldGuard}
import org.bukkit.entity.Player
import org.bukkit.{Location, World}

import scala.jdk.CollectionConverters._

/**
 * WorldGuardの各種関数を集めたクラスです.
 *
 * @author karayuu
 */
object WorldGuardWrapper {

  private val worldGuard = WorldGuard.getInstance()

  /**
   * [[LocalPlayer]]を返す
   */
  private def wrapPlayer(player: Player): LocalPlayer =
    worldGuard.checkPlayer(BukkitAdapter.adapt(player))

  def getRegionManager(world: World): RegionManager =
    worldGuard.getPlatform.getRegionContainer.get(BukkitAdapter.adapt(world))

  def getRegion(loc: Location): List[ProtectedRegion] = {
    val container =
      worldGuard.getPlatform.getRegionContainer.get(BukkitAdapter.adapt(loc.getWorld))
    container
      .getApplicableRegions(BukkitAdapter.adapt(loc).toVector.toBlockPoint)
      .getRegions
      .asScala
      .toList
  }


  def getRegions(world: World): List[ProtectedRegion] = {
    worldGuard
      .getPlatform
      .getRegionContainer
      .get(BukkitAdapter.adapt(world))
      .getRegions
      .values()
      .asScala
      .toList
  }

  def isNotOverlapping(world: World, region: ProtectedCuboidRegion): Boolean = {
    val regions = worldGuard
      .getPlatform
      .getRegionContainer
      .get(BukkitAdapter.adapt(world))
      .getRegions
      .values()
    region.getIntersectingRegions(regions).size() <= 0
  }

  def canBuild(p: Player, loc: Location): Boolean = {
    getRegion(loc).exists { region =>
      val player = wrapPlayer(p)
      region.isOwner(player) || region.isMember(player)
    }
  }

  def findByRegionName(name: String): Option[RegionManager] =
    worldGuard.getPlatform.getRegionContainer.getLoaded.asScala.find(_.getName == name)

  def removeByProtectedRegionRegion(world: World, region: ProtectedRegion): Unit = {
    worldGuard
      .getPlatform
      .getRegionContainer
      .get(BukkitAdapter.adapt(world))
      .removeRegion(region.getId)
  }

  def getMaxRegion(player: Player, world: World): Int = {
    worldGuard
      .getPlatform
      .getGlobalStateManager
      .get(BukkitAdapter.adapt(world))
      .getMaxRegionCount(wrapPlayer(player))
  }

  def getNumberOfRegions(player: Player, world: World): Int =
    worldGuard
      .getPlatform
      .getRegionContainer
      .get(BukkitAdapter.adapt(world))
      .getRegionCountOfPlayer(wrapPlayer(player))

  def getWorldMaxRegion(world: World): Int = {
    worldGuard
      .getPlatform
      .getGlobalStateManager
      .get(BukkitAdapter.adapt(world))
      .maxRegionCountPerPlayer
  }

  def isRegionMember(player: Player, location: Location): Boolean =
    getRegion(location).exists(_.isMember(wrapPlayer(player)))

  def canProtectionWorld(world: World): Boolean =
    worldGuard.getPlatform.getGlobalStateManager.get(BukkitAdapter.adapt(world)).useRegions

}
