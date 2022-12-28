package com.github.unchama.seichiassist.subsystems.lastquit.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.domain.actions.LastSeenNameToUuid
import scalikejdbc._

import java.util.UUID

sealed trait LastSeenNameToUuidError

object LastSeenNameToUuidError {

  /**
   * 2つ以上見つかった
   */
  case object MultipleFound extends LastSeenNameToUuidError

  /**
   * 見つからなかった
   */
  case object NotFound extends LastSeenNameToUuidError

}

class JdbcLastSeenNameToUuid[F[_]: Sync]
    extends LastSeenNameToUuid[F, LastSeenNameToUuidError] {

  override def of(playerName: String): F[Either[UUID, LastSeenNameToUuidError]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        val foundUuid = sql"SELECT uuid FROM playerdata WHERE name = $playerName"
          .map(rs => UUID.fromString(rs.string("uuid")))
          .toList()
          .apply()

        if (foundUuid.isEmpty) Right(LastSeenNameToUuidError.NotFound)
        else if (foundUuid.length >= 2) Right(LastSeenNameToUuidError.MultipleFound)
        else Left(foundUuid.head)
      }
    }

}
