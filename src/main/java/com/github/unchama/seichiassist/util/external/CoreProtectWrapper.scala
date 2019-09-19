package com.github.unchama.seichiassist.util.external

import net.coreprotect.CoreProtectAPI
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player

class CoreProtectWrapper(val backbone: CoreProtectAPI) {
  def queueBlockRemoval(who: Player, where: Location, rawType: Byte): Boolean {
    return backbone.logRemoval(who.name, where, where.block.type, rawType)
  }

  def queueBlockRemoval(who: Player, where: Block): Boolean {
    return backbone.logRemoval(who.name, where.location, where.type, where.data)
  }
  /*
  // For >= 1.13
  def queueBlockRemoval(who: Player, where: Location, data: BlockData): Boolean {
    return backbone.logRemoval(who.name, where, where.block.type, data)
  }
  */
}
