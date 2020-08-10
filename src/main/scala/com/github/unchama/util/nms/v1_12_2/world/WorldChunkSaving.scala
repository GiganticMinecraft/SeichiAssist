package com.github.unchama.util.nms.v1_12_2.world

import java.lang.reflect.Method

import cats.effect.{Concurrent, Sync}
import org.slf4j.Logger


object WorldChunkSaving {

  private object Reflection {
    val nmsPackage_1_12_R1 = "net.minecraft.server.v1_12_R1"

    object FileIOThread {
      lazy val clazz: Class[_] = Class.forName(s"$nmsPackage_1_12_R1.FileIOThread")

      // public static FileIOThread method()
      lazy val getInstance: Unit => AnyRef = {
        val method = clazz.getDeclaredMethod("a")
        _ => method.invoke(null)
      }

      lazy val instance: AnyRef = getInstance()

      // public void method()
      lazy val relaxThrottle: AnyRef => Unit => Unit = {
        val method = clazz.getDeclaredMethod("b")
        receiver => _ => method.invoke(receiver)
      }

      // public void method()
      // originally
      // private void method()
      lazy val forceLoopThroughSavers: AnyRef => Unit => Unit = {
        val method = instance.getClass.getDeclaredMethod("c")

        method.setAccessible(true)

        receiver => _ => method.invoke(receiver)
      }
    }

  }

  import Reflection._

  /**
   * FileIOThread is a gateway object to handle chunk saves,
   * but it has an internal flag to not let chunks save in a mass.
   *
   * This action, when running, relaxes the throttle.
   * This action completes when there are no more chunks to be saved.
   */
  private def relaxFileIOThreadThrottle[F[_] : Sync]: F[Unit] = Sync[F].delay {
    FileIOThread.relaxThrottle(FileIOThread.instance)()
  }

  /**
   * FileIOThread has an internal list of `IAsyncChunkSaver`s,
   * each of which has a single method to pop its internal queue and proceed to saving a chunk.
   *
   * This action loops through the current list of `IAsyncChunkSaver`s and
   * invokes `IAsyncChunkSaver`s' pop-and-process method.
   */
  private def forceFileIOThreadLoopThroughSavers[F[_] : Sync]: F[Unit] = Sync[F].delay {
    FileIOThread.instance.synchronized {
      FileIOThread.forceLoopThroughSavers(FileIOThread.instance)()
    }
  }

  import cats.implicits._

  /**
   * In a running minecraft server, there is an internal queue which is used in controlling and limiting chunk saves.
   *
   * When chunk load happens a lot in a very short period of time,
   * default chunk unloading may be too slow, throttled by the internal queue,
   * hence there is a danger of OutOfMemoryError being thrown.
   *
   * This action is helpful in such a situation; it starts a fiber,
   * within which any unloaded unsaved chunks will be forced to be saved.
   */
  def flushChunkSaverQueue[F[_]](implicit F: Concurrent[F], logger: Logger): F[Unit] = {
    F.delay(println("Save queue flushing started..."))
  } >> {
    F.race(
      relaxFileIOThreadThrottle[F],
      F.foreverM(forceFileIOThreadLoopThroughSavers)
    ).as(())
  } >> {
    F.delay(println("Save queue flushing done!"))
  }

}
