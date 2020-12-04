package com.github.unchama.util.external

import com.onarandombox.MultiverseCore.MultiverseCore
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import net.coreprotect.{CoreProtect, CoreProtectAPI}
import org.bukkit.Bukkit

object ExternalPlugins {
  private def getCoreProtect: Option[CoreProtectAPI] = {
    val plugin = Bukkit.getPluginManager.getPlugin("CoreProtect")
    if (!plugin.isInstanceOf[CoreProtect]) return None
    val coreProtectAPI = plugin.asInstanceOf[CoreProtect].getAPI
    if (!coreProtectAPI.isEnabled) return None
    if (coreProtectAPI.APIVersion() < 4) return None
    Some(coreProtectAPI)
  }

  def getCoreProtectWrapper: Option[CoreProtectWrapper] = {
    val coreProtectAPI = getCoreProtect
    coreProtectAPI match {
      case Some(coreProtectAPI: CoreProtectAPI) => Some(new CoreProtectWrapper(coreProtectAPI))
      case None => None
    }
  }

  def getWorldGuard: WorldGuardPlugin = {
    val plugin = Bukkit.getPluginManager.getPlugin("WorldGuard")
    if (!plugin.isInstanceOf[WorldGuardPlugin]) throw new IllegalStateException("WorldGuardPluginが見つかりませんでした。")
    plugin.asInstanceOf[WorldGuardPlugin]
  }

  def getMultiverseCore: MultiverseCore = {
    val plugin = Bukkit.getPluginManager.getPlugin("Multiverse-Core")
    if (!plugin.isInstanceOf[MultiverseCore]) throw new IllegalStateException("Multiverse-Coreが見つかりませんでした。")
    plugin.asInstanceOf[MultiverseCore]
  }

  def getWorldEdit: Option[WorldEditPlugin] = {
    val plugin = Bukkit.getPluginManager.getPlugin("WorldEdit")
    plugin match {
      case worldEditPlugin: WorldEditPlugin => Some(worldEditPlugin)
      case _ => None
    }
  }
}
