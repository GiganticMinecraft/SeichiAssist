package com.github.unchama.minecraft.bukkit.actions

import cats.Monad
import cats.effect.{ConcurrentEffect, ContextShift, Sync, SyncEffect, SyncIO}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class OnBukkitServerThread[
  F[_]
](implicit hostPlugin: JavaPlugin,
  shift: ContextShift[F],
  F: ConcurrentEffect[F]) extends OnMinecraftServerThread[F] {

  import cats.implicits._

  override def runAction[G[_] : SyncEffect, A](ga: G[A]): F[A] = {
    val checkMainThread = Sync[G].delay {
      hostPlugin.getServer.isPrimaryThread
    }

    for {
      // メインスレッドにいる場合はすぐに実行できるので試行
      immediateResult <- ContextCoercion.syncEffectToSync[G, F].apply {
        checkMainThread.ifM[Option[A]](ga.map(Some.apply), Monad[G].pure(None))
      }

      result <- immediateResult match {
        // メインスレッドですでに実行ができた場合実行結果を
        case Some(value) => Monad[F].pure(value)

        // 実行結果が得られていない場合、メインスレッドに飛んで実行結果を戻す
        // メインスレッドに飛ぶアクション自体をcancellableにする
        case None => F.cancelable[A] { cb =>
          val run: Runnable = () => {
            cb(Right(SyncEffect[G].runSync[SyncIO, A](ga).unsafeRunSync()))
          }

          F.delay {
            Bukkit.getScheduler.runTask(hostPlugin, run)
          } >>= { task =>
            F.delay(task.cancel())
          }
        }
      }

      // 継続の実行がメインスレッドから外れるよう促す
      _ <- shift.shift
    } yield result
  }

}
