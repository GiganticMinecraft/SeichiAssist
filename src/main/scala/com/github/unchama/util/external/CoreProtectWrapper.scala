package com.github.unchama.util.external

import net.coreprotect.CoreProtectAPI
import org.bukkit.block.Block
import org.bukkit.entity.Player

class CoreProtectWrapper(val backbone: CoreProtectAPI) {

  def queueBlockRemoval(who: Player, where: Block): Boolean = {
    backbone.logRemoval(who.getName, where.getLocation, where.getType, where.getBlockData)
  }

}
