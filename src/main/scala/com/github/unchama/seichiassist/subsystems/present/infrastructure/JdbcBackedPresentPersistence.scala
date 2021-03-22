package com.github.unchama.seichiassist.subsystems.present.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scalikejdbc._

import java.util.UUID

class JdbcBackedPresentPersistence[F[_] : Sync] extends PresentPersistence[F] {
  private final val definitionTable = "presents"
  private final val stateTable = "present_state"
  private final val claimingStateColumn = "claimed"
  override def defineNewPresent(itemstack: ItemStack): F[Option[PresentID]] = Sync[F].delay {
    val stackAsBlob = ItemStackBlobProxy.itemStackToBlob(itemstack)
    DB.localTx { implicit session =>
      // プレゼントのIDはauto_incrementなので0で良い
      sql"""INSERT INTO $definitionTable VALUES (0, $stackAsBlob)"""
        .execute()
        .apply()

      val value = DB.readOnly { implicit session =>
        // ここで、itemstackは同じItemStackであるプレゼントをスコープにする場合を考慮して、UNIQUEではない。
        // 他方、present_idは主キーであり、AUTO_INCREMENTであることから単調増加なので、単純にMAXを取れば良い。
        sql"""SELECT MAX(present_id) FROM $definitionTable WHERE itemstack = $stackAsBlob"""
          .map { rs => rs.int("present_id") }
          .first()
          .apply()
          // 上でINSERTしてるんだから、必ず見つかるはず
          .get
      }

      Some(value)
    }
  }

  override def addScope(presentID: PresentID, players: Set[UUID]): F[Unit] = Sync[F].delay {
    val initialValues = players.map { uuid => (presentID, s"'${uuid.toString}'", false) }
      .map { case (_1, _2, _3) => s"($_1, $_2, $_3)" }
      .mkString(",")

    DB.localTx { implicit session =>
      sql"""INSERT INTO $stateTable VALUES ($initialValues)"""
        .execute()
        .apply()
    }
  }

  override def removeScope(presentID: PresentID, players: Set[UUID]): F[Unit] = Sync[F].delay {
    val scopeAsSQL = players.map(_.toString).map(x => s"'$x'").mkString(",")

    DB.localTx { implicit session =>
      sql"""DELETE FROM $stateTable WHERE present_id = $presentID AND uuid IN $scopeAsSQL"""
        .execute()
        .apply()
    }
  }

  override def claimPresent(player: Player, presentId: PresentID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"""UPDATE $stateTable SET $claimingStateColumn = TRUE WHERE uuid = '${player.getUniqueId}' AND present_id = $presentId;"""
        .update()
        .apply()
    }
  }

  override def getPresentMapping: F[Map[PresentID, ItemStack]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT present_id, itemstack FROM $definitionTable;"""
        .map { rs => (
          rs.int("present_id"),
          ItemStackBlobProxy.blobToItemStack(rs.string("itemstack"))
        )}
        .list()
        .apply()
        .toMap
    }
  }

  override def fetchPresentsState(player: Player): F[Map[PresentID, PresentClaimingState]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT present_id, $claimingStateColumn FROM $stateTable WHERE uuid = '${player.getUniqueId}'"""
        .map { rs =>
          val claimState = if (rs.boolean(claimingStateColumn))
            PresentClaimingState.Claimed
          else
            PresentClaimingState.NotClaimed
          (rs.int("present_id"), claimState)
        }
        .list()
        .apply()
        .toMap
        .withDefault(_ => PresentClaimingState.Unavailable)
    }
  }

  override def lookupPresent(presentID: PresentID): F[Option[ItemStack]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT itemstack FROM $definitionTable WHERE present_id = $presentID"""
        .map { rs => ItemStackBlobProxy.blobToItemStack(rs.string("itemstack")) }
        .first()
        .apply()
    }
  }
}

