package com.github.unchama.util.external

import net.coreprotect.CoreProtectAPI
import org.bukkit.block.Block
import org.bukkit.entity.Player

class CoreProtectWrapper(val backbone: CoreProtectAPI) {

  def queueBlockRemoval(who: Player, where: Block): Boolean = {
    // where.getDataが非推奨になっていますが、現在これ以外の方法でByteデータを取得する方法がないようです。
    backbone.logRemoval(who.getName, where.getLocation, where.getType, where.getData)
  }

  /*
  // For >= 1.13
  def queueBlockRemoval(who: Player, where: Location, data: BlockData): Boolean = {
    return backbone.logRemoval(who.getName, where, where.getBlock.getType, data)
  }
   */
}
