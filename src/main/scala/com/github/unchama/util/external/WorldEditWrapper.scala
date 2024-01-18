package com.github.unchama.util.external

import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.math.BlockVector3
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

}
