package com.github.unchama.seichiassist.subsystems.present.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scalikejdbc._

import java.util.UUID

class JdbcBackedPresentRepository[F[_] : Sync] {
  def getUUIDs: F[Set[UUID]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT uuid from seichiassist.playerdata;"
        // mapがないとキレる
        .map { rs =>
          UUID.fromString(rs.string("uuid"))
        }
        .list()
        .apply()
        .toSet
    }
  }

  /**
   *
   * @param itemstack 追加するアイテム
   * @param players 配るプレイヤー
   * @return 成功した場合新たに取得した`F[Some[Int]]`、失敗した場合`F[None]`
   */
  def performAddPresent(itemstack: ItemStack, players: Seq[UUID]): F[Option[Int]] = {
    Sync[F].delay {
      val next = DB.readOnly { implicit session =>
        sql"""SELECT MAX(present_id) as max FROM presents"""
          .map { _.int("max") }
          .first()
          .apply()
      }.map { _ + 1 }.getOrElse(1)
      val wasSuccessful = DB.localTx { implicit session =>
        sql"""INSERT INTO present VALUES ($next, '${ItemStackBlobProxy.itemStackToBlob(itemstack)}')"""
          .execute()
          .apply()

        val initQuery =
          players.map { uuid => (uuid.toString, next, false) }.map { t =>
            s"('${t._1}', ${t._2}, ${t._3})"
          }.mkString(",\n")
        sql"""INSERT INTO present_state VALUES $initQuery"""
          .execute()
          .apply()
      }
      Option.when(wasSuccessful) {
        next
      }
    }
  }

  def claimPresent(player: Player, presentId: Int): F[Unit] = {
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"""UPDATE present_state SET claimed = TRUE WHERE uuid = '${player.getUniqueId}' AND present_id = $presentId;"""
          .update()
          .apply()
      }
    }
  }

  def getAllPresent: F[Map[Int, ItemStack]] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT present_id, itemstack FROM presents;"""
          .map { rs => (
            rs.int("present_id"),
            ItemStackBlobProxy.blobToItemStack(rs.string("itemstack"))
          )}
          .list()
          .apply()
          .toMap
      }
    }
  }

  def fetchPresentsState(player: Player): F[Map[Int, PresentClaimingState]] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT present_id, claimed FROM present_state WHERE uuid = '${player.getUniqueId}'"""
          .map { x =>
            val claimState = if (x.boolean("claimed"))
              PresentClaimingState.Claimed
            else
              PresentClaimingState.NotClaimed
            (x.int("present_id"), claimState)
          }
          .list()
          .apply()
          .toMap
          .withDefault(_ => PresentClaimingState.Unavailable)
      }
    }
  }

  def getAllPresentId: F[Set[Int]] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT present_id FROM presents;"""
          .map { rs => rs.int("present_id") }
          .list()
          .apply()
          .toSet
      }
    }
  }
}

