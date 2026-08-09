package com.github.unchama.minecraft.bukkit.actions

import cats.effect.{Async, SyncIO}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import org.bukkit.plugin.java.JavaPlugin

class OnBukkitServerThread[F[_]](hostPlugin: JavaPlugin)(implicit F: Async[F])
    extends OnMinecraftServerThread[F] {

  import cats.syntax.all._

  override def runAction[A](action: SyncIO[A]): F[A] =
    F.defer {
      F.delay(hostPlugin.getServer.isPrimaryThread).flatMap {
        case true  => action.to[F]
        case false =>
          F.async[A] { callback =>
            F.delay {
              val run: Runnable = () => callback(action.attempt.unsafeRunSync())
              val task = hostPlugin.getServer.getScheduler.runTask(hostPlugin, run)

              Some(F.delay(task.cancel()))
            }
          }
      }
    }

}
