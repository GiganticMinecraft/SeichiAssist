package com.github.unchama.buildassist

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import org.bukkit.Bukkit

object Util {

  // ワールドガードAPIを返す
  def getWorldGuard: WorldGuardPlugin = {
    Bukkit.getServer.getPluginManager.getPlugin("WorldGuard") match {
      case plugin: WorldGuardPlugin => plugin
      case _ => throw new IllegalStateException("WorldGuard is not loaded!")
    }
  }

}
