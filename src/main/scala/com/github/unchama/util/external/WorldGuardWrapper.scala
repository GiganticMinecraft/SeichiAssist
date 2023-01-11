package com.github.unchama.util.external

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedRegion
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

  /**
   * WorldGuardのインスタンス
   */
  private val plugin = ExternalPlugins.getWorldGuard

  /**
   * [[RegionManager]]を返す
   *
   * The expression is nullable: WorldConfiguration#useRegions is false => null
   */
  def getRegionManager(world: World): Option[RegionManager] = Option(
    plugin.getRegionManager(world)
  )

  /**
   * 与えられた [[World]] の [[Player]] の最大保護可能数を取得します.
   *
   * @param player 最大保護可能数を取得したい [[Player]] (`null`は許容されない)
   * @param world  最大保護可能数を取得したい [[World]] (`null`は許容されない)
   * @return [[Player]] の [[World]] における最大保護可能数
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
    WorldGuardPlugin
      .inst()
      .getRegionContainer
      .get(where)
      .getRegionCountOfPlayer(WorldGuardPlugin.inst().wrapPlayer(who))

  /**
   * 現在[[Player]]が[[Location]]の座標でOwnerになっている保護があるかどうかを返す。
   * @param player 調べる対象であるPlayer
   * @param location どの座標か
   * @return Ownerである保護が1つだけあればtrue、ないか保護が2個以上重なっていて判定できなければfalse
   */
  def isRegionOwner(player: Player, location: Location): Boolean =
    getOneRegion(location).exists(_.isOwner(plugin.wrapPlayer(player)))

  /**
   * 現在[[Player]]が[[Location]]の座標でMemberになっている保護があるかどうかを返す。
   * ※Ownerでもある場合も含まれる。
   * @param player 調べる対象であるPlayer
   * @param location どの座標か
   * @return Memberである保護が1つだけあればtrue、ないか保護が2個以上重なっていて判定できなければfalse
   */
  def isRegionMember(player: Player, location: Location): Boolean =
    getOneRegion(location).exists(_.isMember(plugin.wrapPlayer(player)))

  /**
   * [[Location]]の座標にある保護を1つだけ取得する
   * @param location どの座標か
   * @return [[ProtectedRegion]]。保護が1個もないか、2個以上ある場合は[[None]]
   */
  def getOneRegion(location: Location): Option[ProtectedRegion] = {
    val regions = getRegions(location)

    regions.headOption.filter(_ => regions.size == 1)
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
