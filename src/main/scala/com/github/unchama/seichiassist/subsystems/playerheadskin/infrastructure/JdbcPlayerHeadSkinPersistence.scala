package com.github.unchama.seichiassist.subsystems.playerheadskin.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.playerheadskin.domain.PlayerHeadSkinPersistence
import scalikejdbc._

import java.util.UUID

class JdbcPlayerHeadSkinPersistence[F[_]: Sync] extends PlayerHeadSkinPersistence[F] {

  override def fetchLastSeenPlayerName(player: UUID): F[Option[String]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT name FROM playerdata WHERE uuid = ${player.toString}"
        .map(_.string("name"))
        .first
        .apply()
    }
  }

}
