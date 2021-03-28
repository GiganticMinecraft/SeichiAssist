package com.github.unchama.seichiassist.subsystems.present.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scalikejdbc._

import java.util.UUID

/**
 * [[PresentPersistence]]のJDBC実装。この実装は[[PresentPersistence]]の制約を引き継ぐ。
 */
class JdbcBackedPresentPersistence[F[_] : Sync] extends PresentPersistence[F] {
  private final val definitionTable = "present"
  private final val stateTable = "present_state"
  private final val claimingStateColumn = "claimed"
  override def define(itemstack: ItemStack): F[Option[PresentID]] = Sync[F].delay {
    val stackAsBlob = ItemStackBlobProxy.itemStackToBlob(itemstack)
    DB.localTx { implicit session =>
      // プレゼントのIDはauto_incrementなので0で良い
      sql"""INSERT INTO $definitionTable (itemstack) VALUES ($stackAsBlob)"""
        .execute()
        .apply()

      val value = DB.readOnly { implicit session =>
        // ここで、itemstackは同じItemStackであるプレゼントが複数存在させたいケースを考慮して、UNIQUEではない。
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

  /**
   * 指定したPresentIDに対応するプレゼントを物理消去する。
   * @param presentId プレゼントID
   */
  override def delete(presentId: PresentID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      // 制約をかけているので$stateTableの方から先に消さないと整合性エラーを吐く
      sql"""DELETE $stateTable WHERE present_id = $presentId"""
        .execute()
        .apply()

      sql"""DELETE $definitionTable WHERE present_id = $presentId"""
        .execute()
        .apply()
    }
  }

  override def grant(presentID: PresentID, players: Set[UUID]): F[Unit] = Sync[F].delay {
    import scala.collection.Seq.iterableFactory

    val initialValues = players
      .map { uuid => Seq(presentID, uuid.toString, false) }
      .toSeq

    DB.localTx { implicit session =>
      sql"""INSERT INTO $stateTable VALUES (?, ?, ?)"""
        .batch(initialValues: _*)
        .apply()
    }
  }

  override def revoke(presentID: PresentID, players: Set[UUID]): F[Unit] = Sync[F].delay {
    val scopeAsSQL = players.map(_.toString)

    DB.localTx { implicit session =>
      // https://discord.com/channels/237758724121427969/565935041574731807/824107651985834004
      sql"""DELETE FROM $stateTable WHERE present_id = $presentID AND uuid IN ($scopeAsSQL)"""
        .execute()
        .apply()
    }
  }

  override def markAsClaimed(player: Player, presentId: PresentID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"""UPDATE $stateTable SET $claimingStateColumn = TRUE WHERE uuid = ${player.getUniqueId} AND present_id = $presentId"""
        .update()
        .apply()
    }
  }

  override def mapping: F[Map[PresentID, ItemStack]] = Sync[F].delay {
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

  override def fetchState(player: Player): F[Map[PresentID, PresentClaimingState]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT present_id, $claimingStateColumn FROM $stateTable WHERE uuid = ${player.getUniqueId.toString}"""
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

  override def lookup(presentID: PresentID): F[Option[ItemStack]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT itemstack FROM $definitionTable WHERE present_id = $presentID"""
        .map { rs => ItemStackBlobProxy.blobToItemStack(rs.string("itemstack")) }
        .first()
        .apply()
    }
  }
}
