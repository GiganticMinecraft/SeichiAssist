package com.github.unchama.seichiassist.subsystems.present.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.domain.actions.UuidToLastSeenName
import scalikejdbc._

import java.util.UUID

/**
 * Bukkitの[[org.bukkit.Server]]の枠組みを超えて、全てのプレイヤーの情報についてアクセスするオブジェクト
 */
class GlobalPlayerAccessor[F[_]: Sync] extends UuidToLastSeenName[F] {

  /**
   * @return
   *   全てのUUIDとそれに紐付けられた最終的な名前
   */
  override def entries: F[Map[UUID, String]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT name, uuid from seichiassist.playerdata"""
        .map { rs => (UUID.fromString(rs.string("uuid")), rs.string("name")) }
        .list()
        .apply()
        .toMap
    }
  }
}
