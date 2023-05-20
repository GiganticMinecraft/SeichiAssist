package com.github.unchama.seichiassist.util

import cats.Monad
import cats.effect.IO
import com.github.unchama.minecraft.actions.GetConnectedPlayers
import com.github.unchama.minecraft.bukkit.actions.GetConnectedBukkitPlayers
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object SendMessageEffect {

  import scala.jdk.CollectionConverters._

  def sendMessageToEveryoneIgnoringPreference[T](
    content: T
  )(implicit send: PlayerSendable[T, IO]): Unit = {
    implicit val g: GetConnectedBukkitPlayers[IO] = new GetConnectedBukkitPlayers[IO]

    sendMessageToEveryoneIgnoringPreferenceM[T, IO](content).unsafeRunAsyncAndForget()
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

  def sendMessageToEveryone[T](content: T)(implicit ev: PlayerSendable[T, IO]): Unit = {
    import cats.implicits._

    Bukkit
      .getOnlinePlayers
      .asScala
      .toList
      .traverse { player =>
        for {
          playerSettings <- SeichiAssist
            .playermap(player.getUniqueId)
            .settings
            .getBroadcastMutingSettings
          _ <- IO { if (!playerSettings.shouldMuteMessages) ev.send(player, content) }
        } yield ()
      }
      .unsafeRunSync()
  }
}
