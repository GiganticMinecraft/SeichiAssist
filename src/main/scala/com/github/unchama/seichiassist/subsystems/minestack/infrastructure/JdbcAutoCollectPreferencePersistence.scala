package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.minestack.domain.AutoCollectPreference
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.AutoCollectPreferencePersistence
import scalikejdbc._

import java.util.UUID

class JdbcAutoCollectPreferencePersistence[F[_]: Sync]
    extends AutoCollectPreferencePersistence[F] {
  override def read(key: UUID): F[Option[AutoCollectPreference]] = Sync[F].delay {
    DB.localTx { implicit session =>
      val fetchedPreference =
        sql"SELECT minestackflag FROM playerdata WHERE uuid = ${key.toString}"
          .map(_.boolean("minestackflag"))
          .single()
          .getOrElse(false)

      Some(AutoCollectPreference(fetchedPreference))
    }
  }

  override def write(key: UUID, value: AutoCollectPreference): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET minestackflag = ${value.isEnabled} WHERE uuid = ${key.toString}"
        .execute()
    }
  }
}
