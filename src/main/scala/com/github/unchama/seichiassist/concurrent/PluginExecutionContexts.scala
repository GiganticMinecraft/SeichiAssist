package com.github.unchama.seichiassist.concurrent

import cats.effect.std.Dispatcher
import cats.effect.unsafe.IORuntime
import cats.effect.{Clock, IO, Resource, SyncIO, Temporal}
import com.github.unchama.concurrent.{NonServerThreadContextShift, RepeatingTaskContext}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.generic.tag.tag
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.Tags.LayoutPreparationContextTag
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.actions.OnBukkitServerThread
import com.github.unchama.seichiassist.DefaultEffectEnvironment
import org.bukkit.plugin.java.JavaPlugin

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

final class PluginExecutionContexts private[concurrent] (
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

  /**
   * プラグインの実行コンテキストを確保する。
   *
   * 確保されている間だけ、このオブジェクトが公開するアクセサ（[[timer]] など）が利用可能になる。
   *
   * `ioRuntime` は敢えて implicit パラメータにしていない。このオブジェクトのスコープ内では
   * 同名の [[ioRuntime]] アクセサがローカルなimplicitとして最優先で解決されてしまい、
   * 確保前の [[current]] を参照する実装を誤って渡してしまうため。
   */
  def resource(
    plugin: JavaPlugin,
    ioRuntime: IORuntime
  ): Resource[IO, PluginExecutionContexts] =
    PluginExecutionContextsFactory.resource(plugin, ioRuntime)

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

  private[concurrent] val currentReference =
    new AtomicReference[Option[PluginExecutionContexts]](None)

  private def current: PluginExecutionContexts =
    currentReference.get().getOrElse {
      throw new IllegalStateException(
        "PluginExecutionContexts.resource must be acquired before accessing execution contexts"
      )
    }

}

/**
 * [[PluginExecutionContexts]] の構築処理。
 *
 * この処理をコンパニオンオブジェクトの外に置いているのには理由がある。
 * [[PluginExecutionContexts]] のコンパニオンオブジェクトは、確保済みインスタンスへ委譲する
 * `implicit def`（[[PluginExecutionContexts.timer]] など）を公開している。これらは
 * コンパニオンオブジェクト内のコードから見るとローカルスコープのimplicitとなり、
 * `IO` のコンパニオンが提供するインスタンスよりも優先して解決される。
 *
 * 結果として、例えば `Resource.make` が要求する `Functor[IO]` が `timer` から供給され、
 * 確保処理そのものが確保済みインスタンスを参照するという循環が生まれる。実際にこれは
 * 「確保前に `current` を参照して `IllegalStateException`」となり、プラグインの
 * ロード自体が失敗する形で表面化していた。
 *
 * 構築処理をコンパニオンオブジェクトの外に出すことで、この循環を構造的に防いでいる。
 */
private[concurrent] object PluginExecutionContextsFactory {

  def resource(
    plugin: JavaPlugin,
    ioRuntime: IORuntime
  ): Resource[IO, PluginExecutionContexts] =
    contextsResource(plugin, ioRuntime).flatMap(install)

  private def install(
    contexts: PluginExecutionContexts
  ): Resource[IO, PluginExecutionContexts] = {
    val installed = Some(contexts)

    Resource
      .make {
        IO.delay {
          if (!PluginExecutionContexts.currentReference.compareAndSet(None, installed)) {
            throw new IllegalStateException("PluginExecutionContexts is already initialized")
          }
        }
      } { _ =>
        IO.delay {
          if (!PluginExecutionContexts.currentReference.compareAndSet(installed, None)) {
            throw new IllegalStateException("PluginExecutionContexts ownership was replaced")
          }
        }
      }
      .map(_ => contexts)
  }

  private def contextsResource(
    plugin: JavaPlugin,
    ioRuntime: IORuntime
  ): Resource[IO, PluginExecutionContexts] =
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
