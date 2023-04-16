package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech

import cats.effect.Sync
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.bukkit.BukkitFairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.domain.{
  FairySpeechGateway,
  FairySpeechPersistence
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.infrastructure.JdbcFairySpeechPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.service.FairySpeechService
import org.bukkit.entity.Player

import java.util.UUID

trait System[F[_], Player] extends Subsystem[F] {

  val api: FairySpeechAPI[F, Player]

}

object System {

  import cats.implicits._

  def wired[F[_]: Sync]: System[F, Player] = {
    val speechGateway: Player => FairySpeechGateway[F] = player =>
      new BukkitFairySpeechGateway[F](player)
    val speechService: Player => FairySpeechService[F] = player =>
      new FairySpeechService[F](speechGateway(player))
    val persistence: FairySpeechPersistence[F] = new JdbcFairySpeechPersistence[F]

    new System[F, Player] {
      override val api: FairySpeechAPI[F, Player] = new FairySpeechAPI[F, Player] {

        override def speech(player: Player, messages: Seq[FairyMessage]): F[Unit] = for {
          fairyPlaySound <- persistence.playSoundOnFairySpeech(player.getUniqueId)
          _ <- speechService(player).makeSpeech(messages, fairyPlaySound)
        } yield ()

        override def setPlaySoundOnSpeech(player: UUID, playOnSpeech: Boolean): F[Unit] =
          persistence.setPlaySoundOnSpeech(player, playOnSpeech)

        override def playSoundOnSpeech(player: UUID): F[Boolean] =
          persistence.playSoundOnFairySpeech(player)
      }
    }
  }

}
