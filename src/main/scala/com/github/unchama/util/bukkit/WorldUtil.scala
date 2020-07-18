package com.github.unchama.util.bukkit

import org.bukkit.World
import org.bukkit.World.Environment

object WorldUtil {
  def getAbsoluteWorldFolder(world: World): String = {
    val base = world.getWorldFolder.getAbsolutePath
    world.getEnvironment match {
      case Environment.NORMAL => base
      case Environment.NETHER => s"$base/DIM-1"
      case Environment.THE_END => s"$base/DIM1"
    }
  }
}
