package com.github.unchama.concurrent

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

import scala.concurrent.ExecutionContext

/**
 * Class of `ExecutionContext`s which delegates
 * all execution to the Bukkit's synchronous task scheduler.
 *
 * @param plugin `JavaPlugin` from which the runnable originates
 */
class BukkitSyncExecutionContext(implicit val plugin: JavaPlugin) extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = Bukkit.getScheduler.runTask(plugin, runnable)

  override def reportFailure(cause: Throwable): Unit = {
    plugin.getLogger.severe("Caught unhandled exception while executing a Runnable on main thread.")
    cause.printStackTrace()
  }
}
