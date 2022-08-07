package com.github.unchama.minecraft.bukkit.actions

import cats.effect.Sync
import com.github.unchama.minecraft.actions.GetPlayerUUID
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class GetBukkitPlayerUUID[F[_]: Sync] extends GetPlayerUUID[F] {

  /**
   * プレイヤー名からプレイヤーの[[UUID]]を[[Option]]で返す作用
   */
  override def byPlayerName(name: String): F[Option[UUID]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT uuid FROM playerdata WHERE name = $name"
        .map(rs => UUID.fromString(rs.string("uuid")))
        .single()
        .apply()
    }
  }

}
