package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.domain.FairySpeechPersistence
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcFairySpeechPersistence[F[_]: Sync] extends FairySpeechPersistence[F] {

  override def setPlaySoundOnSpeech(player: UUID, playOnSpeech: Boolean): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE vote_fairy SET is_play_fairy_speech_sound = $playOnSpeech WHERE uuid = ${player.toString}"
          .execute()
      }
    }

  override def playSoundOnFairySpeech(player: UUID): F[Boolean] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT is_play_fairy_speech_sound FROM vote_fairy WHERE uuid=${player.toString}"
        .map(_.boolean("is_play_fairy_speech_sound"))
        .single()
        .get
    }
  }

}
