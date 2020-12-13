package com.github.unchama.util.external

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import org.bukkit.{Location, World}
import org.bukkit.entity.Player

/**
 * [[TownyAPI]]とのプロキシ
 * see:
 * - https://github.com/TownyAdvanced/Towny/wiki/TownyAPI
 */
object TownyAPIWrapper {
  private val api = TownyAPI.getInstance()
  val instance: this.type = this

  /**
   * Townyが有効化されているか調べる
   * @param world 対象のワールド
   * @return 有効化されていればtrue
   */
  def isApplicable(world: World): Boolean =
    api.isTownyWorld(world)

  def canModify(player: Player, location: Location): Boolean = {
    if (!isApplicable(location.getWorld)) return true
    val town = api.getTown(location)
    val existsTown = town ne null
    if (existsTown) {
      town.hasResident(wrapIntoResident(player))
    } else {
      true
    }
  }


  /**
   * org.bukkit.entity.Player -> com.palmergames.bukkit.towny.`object`.Resident
   * @param player プレイヤー
   * @return `player`と同一のエンティティを指す[[Resident]]
   */
  private def wrapIntoResident(player: Player) =
    api.getDataSource.getResident(player.getName)
}
