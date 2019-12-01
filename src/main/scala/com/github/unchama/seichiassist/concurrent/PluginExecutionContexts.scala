package com.github.unchama.seichiassist.concurrent

import java.util.concurrent.Executors

import com.github.unchama.concurrent.BukkitSyncExecutionContext
import com.github.unchama.generic
import com.github.unchama.menuinventory.Tags.LayoutPreparationContextTag
import com.github.unchama.menuinventory.Types.LayoutPreparationContext
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.plugin.java.JavaPlugin

import scala.concurrent.ExecutionContext

object PluginExecutionContexts {

  implicit val pluginInstance: JavaPlugin = SeichiAssist.instance

  val sync: ExecutionContext = new BukkitSyncExecutionContext()

  val cachedThreadPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  implicit val layoutPreparationContext: LayoutPreparationContext =
    generic.tag.tag[LayoutPreparationContextTag][ExecutionContext](cachedThreadPool)

}
