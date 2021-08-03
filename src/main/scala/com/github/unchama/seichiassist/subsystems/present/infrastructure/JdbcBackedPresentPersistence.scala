package com.github.unchama.seichiassist.subsystems.present.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.present.domain.OperationResult.DeleteResult
import com.github.unchama.seichiassist.subsystems.present.domain.{PresentClaimingState, PresentPersistence}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import org.bukkit.inventory.ItemStack
import scalikejdbc._

import java.util.UUID

/**
 * [[PresentPersistence]]のJDBC実装。この実装は[[PresentPersistence]]の制約を引き継ぐ。
 */
class JdbcBackedPresentPersistence[F[_] : Sync] extends PresentPersistence[F, ItemStack] {
  override def define(itemstack: ItemStack): F[PresentID] = Sync[F].delay {
    val stackAsBlob = ItemStackBlobProxy.itemStackToBlob(itemstack)
    DB.localTx { implicit session =>
      // プレゼントのIDはauto_incrementなので明示的に指定しなくて良い
      sql"""INSERT INTO present (itemstack) VALUES ($stackAsBlob)"""
        .updateAndReturnGeneratedKey
        .apply()
    }
  }

  /**
   * 指定したPresentIDに対応するプレゼントを物理消去する。
   *
   * @param presentId プレゼントID
   */
  override def delete(presentId: PresentID): F[DeleteResult] = Sync[F].delay {
    DB.localTx { implicit session =>
      // 制約をかけているのでpresent_stateの方から先に消さないと整合性エラーを吐く
      sql"""DELETE FROM present_state WHERE present_id = $presentId"""
        .execute()
        .apply()

      val deletedRows =
        sql"""DELETE FROM present WHERE present_id = $presentId"""
          .update()
          .apply()

      if (deletedRows == 1) DeleteResult.Done else DeleteResult.NotFount
    }
  }

  override def grant(presentID: PresentID, players: Set[UUID]): F[Unit] = Sync[F].delay {
    import scala.collection.Seq.iterableFactory

    val initialValues = players
      .map { uuid => Seq(presentID, uuid.toString, false) }
      .toSeq

    DB.localTx { implicit session =>
      sql"""INSERT INTO present_state VALUES (?, ?, ?)"""
        .batch(initialValues: _*)
        .apply()
    }
  }

  override def revoke(presentID: PresentID, players: Set[UUID]): F[Unit] = {
    val existence = DB.readOnly { implicit session =>
      sql"""SELECT present_state FROM present_state WHERE present_id = $presentID"""
        .map(_ => ()) // avoid NoExtractor
        .first()
        .apply()
    }

    import cats.Applicative
    existence.fold(Applicative[F].pure(())) { _ =>
      Sync[F].delay {
        val scopeAsSQL = players.map(_.toString)

        DB.localTx { implicit session =>
          // https://discord.com/channels/237758724121427969/565935041574731807/824107651985834004
          sql"""DELETE FROM present_state WHERE present_id = $presentID AND uuid IN ($scopeAsSQL)"""
            .execute()
            .apply()
        }
      }
    }
  }

  override def markAsClaimed(presentId: PresentID, player: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"""UPDATE present_state SET claimed = TRUE WHERE uuid = ${player.toString} AND present_id = $presentId"""
        .update()
        .apply()
    }
  }

  override def mapping: F[Map[PresentID, ItemStack]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT present_id, itemstack FROM present"""
        .map { rs =>
          (
            rs.long("present_id"),
            unwrapItemStack(rs)
          )
        }
        .list()
        .apply()
        .toMap
    }
  }

  override def fetchStateWithPagination(player: UUID, perPage: Int Refined Positive, page: Int Refined Positive): F[Map[PresentID, PresentClaimingState]] = {
    import cats.implicits._
    for {
      idSliceWithPagination <- idSliceWithPagination(perPage, page)
    } yield {
      if (idSliceWithPagination.isEmpty) {
        for {
          entries <- fetchState(player)
        } yield {
          Left(PaginationRejectReason.TooLargePage(Math.ceil(entries.size.toDouble / perPage).toInt))
        }
      } else {
        // ページネーションはIDを列挙するときにすでに完了している
        val associatedEntries = DB.readOnly { implicit session =>
          sql"""
               |SELECT present_id, claimed
               |FROM present_state
               |WHERE uuid = ${player.toString} AND present_id IN ($idSliceWithPagination)
               |ORDER BY present_id
        """
          .stripMargin
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
        sql"""SELECT present_id, claimed FROM present_state WHERE uuid = ${player.toString}"""
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
      sql"""SELECT itemstack FROM present WHERE present_id = $presentID"""
        .map(unwrapItemStack)
        .first()
        .apply()
    }
  }

  private def idSliceWithPagination(perPage: Int Refined Positive, page: Int Refined Positive): F[Set[PresentID]] =
    Sync[F].delay {
      val offset = (page - 1) * perPage
      DB.readOnly { implicit session =>
        sql"""SELECT present_id FROM present ORDER BY present_id LIMIT ${perPage.value} OFFSET $offset"""
          .map { _.long("present_id") }
          .toList()
          .apply()
          .toSet
      }
    }

  private def wrapResultForState(rs: WrappedResultSet): (Long, PresentClaimingState) = {
    val claimState = if (rs.boolean("claimed"))
      PresentClaimingState.Claimed
    else
      PresentClaimingState.NotClaimed

    (rs.long("present_id"), claimState)
  }

  private def unwrapItemStack(rs: WrappedResultSet): ItemStack = {
    ItemStackBlobProxy.blobToItemStack(rs.string("itemstack"))
  }

  private def filledEntries(knownState: Map[PresentID, PresentClaimingState], validGlobalId: Iterable[PresentID]) = {
    val globalEntries = validGlobalId.map(id => (id, PresentClaimingState.Unavailable)).toMap
    globalEntries ++ knownState
  }
}
