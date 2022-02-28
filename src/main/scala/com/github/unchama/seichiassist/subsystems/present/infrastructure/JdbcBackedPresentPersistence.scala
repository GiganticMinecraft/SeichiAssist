package com.github.unchama.seichiassist.subsystems.present.infrastructure

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.generic.MapExtra
import com.github.unchama.seichiassist.subsystems.present.domain.OperationResult.DeleteResult
import com.github.unchama.seichiassist.subsystems.present.domain._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import org.bukkit.inventory.ItemStack
import scalikejdbc._

import java.util.UUID

/**
 * [[PresentPersistence]]のJDBC実装。この実装は[[PresentPersistence]]の制約を引き継ぐ。
 */
class JdbcBackedPresentPersistence[F[_]: Sync] extends PresentPersistence[F, ItemStack] {
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
   * @param presentId
   *   プレゼントID
   */
  override def delete(presentId: PresentID): F[DeleteResult] = Sync[F].delay {
    DB.localTx { implicit session =>
      // 制約をかけているのでpresent_stateの方から先に消さないと整合性エラーを吐く
      sql"""DELETE FROM present_state WHERE present_id = $presentId""".execute().apply()

      val deletedRows =
        sql"""DELETE FROM present WHERE present_id = $presentId""".update().apply()

      if (deletedRows == 1) DeleteResult.Done else DeleteResult.NotFound
    }
  }

  override def grant(presentID: PresentID, players: Set[UUID]): F[Option[GrantRejectReason]] = {
    import cats.implicits._
    val program = for {
      exists <- Sync[F].delay {
        DB.readOnly { implicit session =>
          sql"""SELECT present_id FROM present""".map(x => x.long("present_id")).list().apply()
        }.contains(presentID)
      }
    } yield {
      if (exists) {
        Sync[F].delay {
          import scala.collection.Seq.iterableFactory

          val initialValues = players.map { uuid => Seq(presentID, uuid.toString, false) }.toSeq

          DB.localTx { implicit session =>
            // upsert - これによってfilterなしで整合性違反を起こすことはなくなる
            sql"""
                  INSERT INTO present_state VALUES (?, ?, ?)
                  ON DUPLICATE KEY UPDATE present_id=present_id, uuid=uuid
                 """.batch(initialValues: _*).apply()
          }

          // 型推論
          None: Option[GrantRejectReason]
        }
      } else {
        // 型推論
        Applicative[F].pure(
          Some(GrantRejectReason.NoSuchPresentID: GrantRejectReason): Option[GrantRejectReason]
        )
      }
    }

    program.flatten
  }

  override def revoke(presentID: PresentID, players: Set[UUID]): F[Option[RevokeWarning]] = {
    if (players.isEmpty) {
      Applicative[F].pure(Some(RevokeWarning.NoPlayers))
    } else {
      Sync[F].delay {
        val scopeAsSQL = players.map(_.toString)

        val deleteCount = DB.localTx { implicit session =>
          // https://discord.com/channels/237758724121427969/565935041574731807/824107651985834004
          sql"""DELETE FROM present_state WHERE present_id = $presentID AND uuid IN ($scopeAsSQL)"""
            .update()
            .apply()
        }

        Option.when(deleteCount == 0) { RevokeWarning.NoSuchPresentID }
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
        .map { rs => (rs.long("present_id"), unwrapItemStack(rs)) }
        .list()
        .apply()
        .toMap
    }
  }

  override def fetchStateWithPagination(
    player: UUID,
    perPage: Int Refined Positive,
    page: Int Refined Positive
  ): F[Either[PaginationRejectReason, List[(PresentID, PresentClaimingState)]]] = {
    import cats.implicits._
    for {
      idSliceWithPagination <- idSliceWithPagination(perPage, page)
      count <- computeValidPresentCount
    } yield {
      if (count == 0) {
        Left(PaginationRejectReason.Empty)
      } else if (idSliceWithPagination.isEmpty) {
        Left(PaginationRejectReason.TooLargePage(Math.ceil(count.toDouble / perPage).toLong))
      } else {
        // ページネーションはIDを列挙するときにすでに完了している
        val associatedEntries = DB.readOnly { implicit session =>
          sql"""
               |SELECT present_id, claimed
               |FROM present_state
               |WHERE uuid = ${player.toString} AND present_id IN ($idSliceWithPagination)
               |ORDER BY present_id
        """.stripMargin.map(wrapResultForState).toList().apply()
        }

        Right(
          MapExtra
            .fillOnBaseSet(
              associatedEntries.toMap,
              idSliceWithPagination,
              PresentClaimingState.Unavailable
            )
            .toList
        )
      }
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
      }

      MapExtra.fillOnBaseSet(
        associatedEntries.toMap,
        validPresentIDs.toSet,
        PresentClaimingState.Unavailable
      )
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

  private def idSliceWithPagination(
    perPage: Int Refined Positive,
    page: Int Refined Positive
  ): F[Set[PresentID]] =
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
    val claimState =
      if (rs.boolean("claimed"))
        PresentClaimingState.Claimed
      else
        PresentClaimingState.NotClaimed

    (rs.long("present_id"), claimState)
  }

  private def unwrapItemStack(rs: WrappedResultSet): ItemStack = {
    ItemStackBlobProxy.blobToItemStack(rs.string("itemstack"))
  }

  private def computeValidPresentCount: F[Long] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT COUNT(*) AS c FROM present""".map(_.long("c")).first().apply().get // safe
    }
  }
}
