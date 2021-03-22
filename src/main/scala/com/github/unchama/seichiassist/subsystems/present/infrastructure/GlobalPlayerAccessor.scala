package com.github.unchama.seichiassist.subsystems.present.infrastructure

import cats.effect.Sync
import scalikejdbc._

import java.util.UUID

/**
 * Bukkitの[[org.bukkit.Server]]の枠組みを超えて、全てのプレイヤーの情報についてアクセスするオブジェクト
 */
private[present] object GlobalPlayerAccessor {
  /**
   *
   * @tparam F 文脈
   * @return 全てのUUIDとそれに紐付けられた最終的な名前
   */
  def getUUIDsAndName[F[_] : Sync]: F[Map[UUID, String]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT name, uuid from seichiassist.playerdata"""
        .map { rs =>
          (UUID.fromString(rs.string("uuid")), rs.string("name"))
        }
        .list()
        .apply()
        .toMap
    }
  }
}
