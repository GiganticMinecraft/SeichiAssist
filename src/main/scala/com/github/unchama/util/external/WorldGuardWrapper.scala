package com.github.unchama.util.external

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.{LocalPlayer, WorldGuard}
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.{ProtectedCuboidRegion, ProtectedRegion}
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

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
  private def wrapPlayer(player: Player): LocalPlayer = plugin.wrapPlayer(player)

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

  def getWorldMaxRegion(world: World): Int = {
    worldGuard
      .getPlatform
      .getGlobalStateManager
      .get(BukkitAdapter.adapt(world))
      .maxRegionCountPerPlayer
  }

  /**
   * WorldGuardのインスタンス
   */
  private val plugin = ExternalPlugins.getWorldGuard

  /**
   * [[RegionManager]]を返す
   */
  def getRegionManager(world: World): Option[RegionManager] = Option(
    // The expression is nullable: WorldConfiguration#useRegions is false => null
    plugin.getRegionManager(world)
  )

  /**
   * [[Player]]が[[World]]の中で持っている保護の数を返す
   *
   * @return [[Player]]が[[World]]の中で持っている保護の数。[[getRegionManager]]が[[None]]であれば0。
   */
  def getRegionCountOfPlayer(player: Player, world: World): Int =
    getRegionManager(world).map(_.getRegionCountOfPlayer(wrapPlayer(player))).getOrElse(0)

  /**
   * [[World]]における[[Player]] の最大保護可能数を取得します.
   * @return [[Player]]の[[World]] における最大保護可能数
   */
  def getMaxRegionCount(player: Player, world: World): Int =
    // TODO: migrate this to OptionalInt
    plugin.getGlobalStateManager.get(world).getMaxRegionCount(player)

  /**
   * 現在[[Player]]が[[World]]でオーナーになっている保護の数を返す。
   * @param who 誰か
   * @param where どのワールドか
   * @return オーナーになっている保護の数。どこのオーナーでもない場合は0
   */
  def getNumberOfRegions(who: Player, where: World): Int =
    // TODO: migrate this to OptionalInt
    plugin.getRegionContainer.get(where).getRegionCountOfPlayer(wrapPlayer(who))

  /**
   * 現在[[Player]]が[[Location]]の座標でOwnerになっている保護があるかどうかを返す。
   * @param player 調べる対象であるPlayer
   * @param location どの座標か
   * @return Ownerである保護が1つだけあればtrue、ないか保護が2個以上重なっていて判定できなければfalse
   */
  def isRegionOwner(player: Player, location: Location): Boolean =
    getOneRegion(location).exists(_.isOwner(wrapPlayer(player)))

  /**
   * [[Player]]が[[Location]]の座標でMemberになっている保護があるかどうかを返す。
   * NOTE: Ownerでもある場合も含まれる。
   * @param player 調べる対象であるPlayer
   * @param location どの座標か
   * @return Memberである保護が1つだけあればtrue、ないか保護が2個以上重なっていて判定できなければfalse
   */
  def isRegionMember(player: Player, location: Location): Boolean =
    getOneRegion(location).exists(_.isMember(wrapPlayer(player)))

  /**
   * [[Location]]の座標にある保護を1つだけ取得する
   * @param location どの座標か
   * @return [[ProtectedRegion]]。保護が1個もないか、2個以上ある場合は[[None]]
   */
  def getOneRegion(location: Location): Option[ProtectedRegion] = {
    val regions = getRegions(location)

    Option.when(regions.size == 1)(regions.head)
  }

  /**
   * [[Location]]の座標にある保護をすべて取得する
   * @param location どの座標か
   * @return [[ProtectedRegion]]の[[Set]]
   */
  def getRegions(location: Location): Set[ProtectedRegion] =
    getRegionManager(location.getWorld)
      .map(_.getApplicableRegions(location).getRegions.asScala.toSet)
      .getOrElse(Set.empty)
}
