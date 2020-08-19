package com.github.unchama.seichiassist.concurrent

import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO, Timer}
import com.github.unchama.concurrent.{BukkitSyncIOShift, RepeatingTaskContext, RepeatingTaskContextTag}
import com.github.unchama.generic
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.Tags.LayoutPreparationContextTag
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.plugin.java.JavaPlugin

import scala.concurrent.ExecutionContext

object PluginExecutionContexts {

  implicit val pluginInstance: JavaPlugin = SeichiAssist.instance

  implicit val syncShift: BukkitSyncIOShift = BukkitSyncIOShift()

  val cachedThreadPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)

  // syncShiftの方がspecificな型であるという理由から、
  // syncShiftとasyncShiftが同時にスコープにある場合は
  // syncShiftの方が優先されてしまう模様。これは望ましくなく、エラーになるべきなので、
  // TODO ContextShift[IO]よりも具体的な型にする
  implicit val asyncShift: ContextShift[IO] = IO.contextShift(cachedThreadPool)

  implicit val layoutPreparationContext: LayoutPreparationContext =
    generic.tag.tag[LayoutPreparationContextTag][ExecutionContext](cachedThreadPool)

  implicit val sleepAndRoutineContext: RepeatingTaskContext =
    generic.tag.tag[RepeatingTaskContextTag][ExecutionContext](cachedThreadPool)

}
