package com.github.unchama.seichiassist.util

import cats.Monad
import cats.effect.{IO, LiftIO}
import com.github.unchama.minecraft.actions.GetConnectedPlayers
import com.github.unchama.minecraft.bukkit.actions.GetConnectedBukkitPlayers
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import org.bukkit.entity.Player

object SendMessageEffect {

  def sendMessageToEveryoneIgnoringPreferenceIO[T: PlayerSendable[*, IO]](
    content: T
  ): IO[Unit] = {
    implicit val g: GetConnectedBukkitPlayers[IO] = new GetConnectedBukkitPlayers[IO]
    sendMessageToEveryoneIgnoringPreferenceM[T, IO](content)
  }

  def sendMessageToEveryoneIgnoringPreferenceM[T, F[_]: Monad: GetConnectedPlayers[*[
    _
  ], Player]](content: T)(implicit ev: PlayerSendable[T, F]): F[Unit] = {
    import cats.implicits._

    for {
      players <- GetConnectedPlayers[F, Player].now
      _ <- players.traverse(ev.send(_, content))
    } yield ()
  }

  def sendMessageToEveryone[T, F[_]: Monad: LiftIO: GetConnectedPlayers[*[_], Player]](
    content: T
  )(implicit ev: PlayerSendable[T, F]): F[Unit] = {
    import cats.implicits._

    for {
      players <- GetConnectedPlayers[F, Player].now
      _ <- players.traverse { player =>
        for {
          playerSettings <- SeichiAssist
            .playermap(player.getUniqueId)
            .settings
            .getBroadcastMutingSettings
            .to[F]
          _ <- ev.send(player, content).unlessA(playerSettings.shouldMuteMessages)
        } yield ()
      }
    } yield ()
  }
}
