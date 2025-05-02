package com.github.unchama.util.external

import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import org.bukkit.entity.Player

object WorldEditWrapper {

  /**
   * WorldEditのインスタンス
   */
  private val plugin: WorldEditPlugin = ExternalPlugins.getWorldEdit

  /**
   * @return `player`が選択している範囲
   */
  def getSelection(player: Player): BlockVector3 =
    plugin.getSession(player).getPlacementPosition(plugin.wrapPlayer(player))

  /**
   * @return `player` が範囲を選択していれば Region、そうでなければ None
   */
  def getSelectedRegion(player: Player): Option[Region] = {
    try {
      Some(plugin.getSession(player).getSelection)
    } catch {
      case _: Exception => None
    }
  }

}
