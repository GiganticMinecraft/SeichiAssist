package com.github.unchama.seichiassist.subsystems.present.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import eu.timepit.refined.numeric.{NonNegative, Positive}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import org.bukkit.inventory.ItemStack
import scalikejdbc._

import java.util.UUID

/**
 * [[PresentPersistence]]のJDBC実装。この実装は[[PresentPersistence]]の制約を引き継ぐ。
 */
class JdbcBackedPresentPersistence[F[_] : Sync] extends PresentPersistence[F] {
  override type PresentID = Int
  private final val definitionTable = "present"
  private final val stateTable = "present_state"
  private final val claimingStateColumn = "claimed"
  private final val presentIdColumn = "present_id"
  private final val itemStackColumn = "itemstack"

  override def define(itemstack: ItemStack): F[PresentID] = Sync[F].delay {
    val stackAsBlob = ItemStackBlobProxy.itemStackToBlob(itemstack)
    DB.localTx { implicit session =>
      // プレゼントのIDはauto_incrementなので明示的に指定しなくて良い
      sql"""INSERT INTO $definitionTable ($itemStackColumn) VALUES ($stackAsBlob)"""
        .execute()
        .apply()

      val newPresentID = DB.readOnly { implicit session =>
        // ここで、itemstackは同じItemStackであるプレゼントが複数存在させたいケースを考慮して、UNIQUEではない。
        // 他方、present_idは主キーであり、AUTO_INCREMENTであることから単調増加なので、単純にMAXを取れば良い。
        sql"""SELECT MAX($presentIdColumn) FROM $definitionTable WHERE $itemStackColumn = $stackAsBlob"""
          .map { rs => rs.int(presentIdColumn) }
          .first()
          .apply()
          // 上でINSERTしてるんだから、必ず見つかるはず
          .get
      }

      newPresentID
    }
  }

  /**
   * 指定したPresentIDに対応するプレゼントを物理消去する。
   *
   * @param presentId プレゼントID
   */
  override def delete(presentId: PresentID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      // 制約をかけているので$stateTableの方から先に消さないと整合性エラーを吐く
      sql"""DELETE $stateTable WHERE $presentIdColumn = $presentId"""
        .execute()
        .apply()

      sql"""DELETE $definitionTable WHERE $presentIdColumn = $presentId"""
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
      sql"""DELETE FROM $stateTable WHERE $presentIdColumn = $presentID AND uuid IN ($scopeAsSQL)"""
        .execute()
        .apply()
    }
  }

  override def markAsClaimed(presentId: PresentID, player: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"""UPDATE $stateTable SET $claimingStateColumn = TRUE WHERE uuid = ${player.toString} AND $presentIdColumn = $presentId"""
        .update()
        .apply()
    }
  }

  override def mapping: F[Map[PresentID, ItemStack]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT $presentIdColumn, $itemStackColumn FROM $definitionTable"""
        .map { rs =>
          (
            rs.int(presentIdColumn),
            unwrapItemStack(rs)
          )
        }
        .list()
        .apply()
        .toMap
    }
  }

  override def fetchStateWithPagination(player: UUID, perPage: Int Refined Positive, page: Int Refined Positive): F[Map[Int, PresentClaimingState]] = {
    import cats.implicits._
    for {
      idSliceWithPagination <- idSliceWithPagination(perPage, page)
    } yield {
      // ページネーションはIDを列挙するときにすでに完了している
      val associatedEntries = DB.readOnly { implicit session =>
        sql"""
              SELECT $presentIdColumn, $claimingStateColumn
        FROM $stateTable
        WHERE uuid = ${player.toString} AND $presentIdColumn IN ($idSliceWithPagination)
"""
          .map(wrapResultForState)
          .toList()
          .apply()
          .toMap
      }

      filledEntries(associatedEntries, idSliceWithPagination)
    }
  }

  override def fetchState(player: UUID): F[Map[PresentID, PresentClaimingState]] = {
    import cats.implicits._
    for {
      validPresentMapping <- this.mapping
      validPresentIDs = validPresentMapping.keys
    } yield {
      val associatedEntries = DB.readOnly { implicit session =>
        sql"""SELECT $presentIdColumn, $claimingStateColumn FROM $stateTable WHERE uuid = ${player.toString}"""
          .map(wrapResultForState)
          .list()
          .apply()
          .toMap
      }

      filledEntries(associatedEntries, validPresentIDs)
    }
  }

  override def lookup(presentID: PresentID): F[Option[ItemStack]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT $itemStackColumn FROM $definitionTable WHERE $presentIdColumn = $presentID"""
        .map(unwrapItemStack)
        .first()
        .apply()
    }
  }

  private def idSliceWithPagination(perPage: Int Refined Positive, page: Int Refined Positive): F[Set[PresentID]] =
    Sync[F].delay {
      val offset = (page - 1) * perPage
      DB.readOnly { implicit session =>
        sql"""SELECT $presentIdColumn ORDER BY $presentIdColumn LIMIT $perPage OFFSET $offset"""
          .map { rs =>
            rs.int(presentIdColumn)
          }
          .toList()
          .apply()
          .toSet
      }
    }

  private def wrapResultForState(rs: WrappedResultSet): (Int, PresentClaimingState) = {
    val claimState = if (rs.boolean(claimingStateColumn))
      PresentClaimingState.Claimed
    else
      PresentClaimingState.NotClaimed

    (rs.int(presentIdColumn), claimState)
  }

  private def unwrapItemStack(rs: WrappedResultSet): ItemStack = {
    ItemStackBlobProxy.blobToItemStack(rs.string(itemStackColumn))
  }

  private def filledEntries(knownState: Map[PresentID, PresentClaimingState], validGlobalId: Iterable[PresentID]) = {
    val globalEntries = validGlobalId.map(id => (id, PresentClaimingState.Unavailable)).toMap
    globalEntries ++ knownState
  }
}
