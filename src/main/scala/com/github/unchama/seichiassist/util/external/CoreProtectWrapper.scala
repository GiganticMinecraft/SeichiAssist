package com.github.unchama.seichiassist.util.external

import net.coreprotect.CoreProtectAPI
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player

class CoreProtectWrapper(val backbone: CoreProtectAPI) {
  def queueBlockRemoval(who: Player, where: Location, rawType: Byte): Boolean = {
    backbone.logRemoval(who.getName, where, where.getBlock.getType, rawType)
  }

  def queueBlockRemoval(who: Player, where: Block): Boolean = {
    backbone.logRemoval(who.getName, where.getLocation, where.getType, where.getData)
  }

  /*
  // For >= 1.13
  def queueBlockRemoval(who: Player, where: Location, data: BlockData): Boolean = {
    return backbone.logRemoval(who.getName, where, where.getBlock.getType, data)
  }
  */
}
