package com.github.unchama.seichiassist.concurrent

import cats.effect.{ContextShift, IO, Timer}
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

  given pluginInstance: JavaPlugin = SeichiAssist.instance

  val cachedThreadPool: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  given timer: Timer[IO] = IO.timer(cachedThreadPool)

  given asyncShift: NonServerThreadContextShift[IO] = {
    tag.apply[NonServerThreadContextShiftTag][ContextShift[IO]](
      IO.contextShift(cachedThreadPool)
    )
  }

  given onMainThread: OnMinecraftServerThread[IO] = {
    new OnBukkitServerThread[IO]()(
      pluginInstance,
      asyncShift,
      IO.ioConcurrentEffect(asyncShift)
    )
  }

  given layoutPreparationContext: LayoutPreparationContext =
    generic.tag.tag[LayoutPreparationContextTag][ExecutionContext](cachedThreadPool)

  given sleepAndRoutineContext: RepeatingTaskContext =
    generic.tag.tag[RepeatingTaskContextTag][ExecutionContext](cachedThreadPool)

}
