package com.github.unchama.seichiassist.concurrent

import cats.effect.{Clock, ContextShift, IO, SyncIO, Timer}
import com.github.unchama.concurrent._
import com.github.unchama.generic
import com.github.unchama.generic.tag.tag
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.Tags.LayoutPreparationContextTag
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.actions.OnBukkitServerThread
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.plugin.java.JavaPlugin

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object PluginExecutionContexts {

  implicit val pluginInstance: JavaPlugin = SeichiAssist.instance

  val cachedThreadPool: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)

  implicit val clock: Clock[SyncIO] = Clock.create

  implicit val asyncShift: NonServerThreadContextShift[IO] = {
    tag.apply[NonServerThreadContextShiftTag][ContextShift[IO]](
      IO.contextShift(cachedThreadPool)
    )
  }

  implicit val onMainThread: OnMinecraftServerThread[IO] = {
    new OnBukkitServerThread[IO]()(
      pluginInstance,
      asyncShift,
      IO.ioConcurrentEffect(asyncShift)
    )
  }

  implicit val layoutPreparationContext: LayoutPreparationContext =
    generic.tag.tag[LayoutPreparationContextTag][ExecutionContext](cachedThreadPool)

  implicit val sleepAndRoutineContext: RepeatingTaskContext =
    generic.tag.tag[RepeatingTaskContextTag][ExecutionContext](cachedThreadPool)

}
