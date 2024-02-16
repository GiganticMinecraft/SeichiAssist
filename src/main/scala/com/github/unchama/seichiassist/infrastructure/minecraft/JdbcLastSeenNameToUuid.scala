package com.github.unchama.seichiassist.infrastructure.minecraft

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

  override def of(playerName: String): F[Either[LastSeenNameToUuidError, UUID]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        val foundUuid = sql"SELECT uuid FROM playerdata WHERE name = $playerName"
          .map(rs => UUID.fromString(rs.string("uuid")))
          .toList()

        if (foundUuid.isEmpty) Left(LastSeenNameToUuidError.NotFound)
        else if (foundUuid.length >= 2) Left(LastSeenNameToUuidError.MultipleFound)
        else Right(foundUuid.head)
      }
    }

}
