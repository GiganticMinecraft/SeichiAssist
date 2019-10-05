package com.github.unchama.seichiassist

import com.github.unchama.concurrent.BukkitSyncExecutionContext
import org.bukkit.plugin.java.JavaPlugin

import scala.concurrent.ExecutionContext

object PluginExecutionContexts {

  implicit val pluginInstance: JavaPlugin = SeichiAssist.instance

  val sync: ExecutionContext = new BukkitSyncExecutionContext()

}
