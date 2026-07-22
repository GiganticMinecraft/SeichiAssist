package com.github.unchama.seichiassist.concurrent

import cats.effect.std.Dispatcher
import cats.effect.unsafe.IORuntime
import cats.effect.{Clock, IO, Resource, SyncIO, Temporal}
import cats.syntax.all._
import com.github.unchama.concurrent.{NonServerThreadContextShift, RepeatingTaskContext}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.generic.tag.tag
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.Tags.LayoutPreparationContextTag
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.actions.OnBukkitServerThread
import com.github.unchama.seichiassist.DefaultEffectEnvironment
import org.bukkit.plugin.java.JavaPlugin

import java.util.concurrent.{Executors, TimeUnit}
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

final class PluginExecutionContexts private (
  val cachedThreadPool: ExecutionContextExecutorService,
  implicit val ioRuntime: IORuntime,
  implicit val dispatcher: Dispatcher[IO],
  implicit val effectEnvironment: EffectEnvironment[IO],
  implicit val asyncShift: NonServerThreadContextShift[IO],
  implicit val onMainThread: OnMinecraftServerThread[IO],
  implicit val layoutPreparationContext: LayoutPreparationContext,
  implicit val sleepAndRoutineContext: RepeatingTaskContext,
  implicit val timer: Temporal[IO],
  implicit val clock: Clock[SyncIO]
)

object PluginExecutionContexts {

  def resource(plugin: JavaPlugin)(
    implicit ioRuntime: IORuntime
  ): Resource[IO, PluginExecutionContexts] =
    contextsResource(plugin).flatMap(install)

  def cachedThreadPool: ExecutionContextExecutorService = current.cachedThreadPool

  implicit def ioRuntime: IORuntime = current.ioRuntime

  implicit def dispatcher: Dispatcher[IO] = current.dispatcher

  implicit def effectEnvironment: EffectEnvironment[IO] = current.effectEnvironment

  implicit def asyncShift: NonServerThreadContextShift[IO] = current.asyncShift

  implicit def onMainThread: OnMinecraftServerThread[IO] = current.onMainThread

  implicit def layoutPreparationContext: LayoutPreparationContext =
    current.layoutPreparationContext

  implicit def sleepAndRoutineContext: RepeatingTaskContext = current.sleepAndRoutineContext

  implicit def timer: Temporal[IO] = current.timer

  implicit def clock: Clock[SyncIO] = current.clock

  private val currentReference = new AtomicReference[Option[PluginExecutionContexts]](None)

  private def current: PluginExecutionContexts =
    currentReference.get().getOrElse {
      throw new IllegalStateException(
        "PluginExecutionContexts.resource must be acquired before accessing execution contexts"
      )
    }

  private def install(
    contexts: PluginExecutionContexts
  ): Resource[IO, PluginExecutionContexts] = {
    val installed = Some(contexts)

    Resource
      .make {
        IO.delay {
          if (!currentReference.compareAndSet(None, installed)) {
            throw new IllegalStateException("PluginExecutionContexts is already initialized")
          }
        }
      } { _ =>
        IO.delay {
          if (!currentReference.compareAndSet(installed, None)) {
            throw new IllegalStateException("PluginExecutionContexts ownership was replaced")
          }
        }
      }
      .as(contexts)
  }

  private def contextsResource(plugin: JavaPlugin): Resource[IO, PluginExecutionContexts] =
    executionContextResource.flatMap { executionContext =>
      Dispatcher.parallel[IO].map { dispatcher =>
        val asyncShift = NonServerThreadContextShift.fromExecutionContext[IO](executionContext)

        new PluginExecutionContexts(
          executionContext,
          ioRuntime,
          dispatcher,
          DefaultEffectEnvironment[IO](dispatcher),
          asyncShift,
          new OnBukkitServerThread[IO](plugin),
          tag[LayoutPreparationContextTag][ExecutionContext](executionContext),
          tag[com.github.unchama.concurrent.RepeatingTaskContextTag][ExecutionContext](
            executionContext
          ),
          Temporal[IO],
          Clock[SyncIO]
        )
      }
    }

  private def executionContextResource: Resource[IO, ExecutionContextExecutorService] =
    Resource.make {
      IO.delay(ExecutionContext.fromExecutorService(Executors.newCachedThreadPool()))
    } { executionContext =>
      IO.blocking {
        executionContext.shutdown()
        if (!executionContext.awaitTermination(10, TimeUnit.SECONDS)) {
          executionContext.shutdownNow(): Unit
        }
      }
    }

}
