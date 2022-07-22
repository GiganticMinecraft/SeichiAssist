package com.github.unchama.seichiassist.subsystems.sharedinventory.infrastracture

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.{
  SharedFlag,
  SharedInventoryPersistence
}
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents
import com.github.unchama.seichiassist.util.ItemListSerialization
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID
import scala.jdk.CollectionConverters._

class JdbcSharedInventoryPersistence[F[_]: Sync] extends SharedInventoryPersistence[F] {

  /**
   * セーブされている[[InventoryContents]]を完全に削除します。
   */
  override def clear(targetUuid: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET shareinv = NULL WHERE uuid = '${targetUuid.toString}'"
        .execute()
        .apply()
    }
  }

  /**
   * [[InventoryContents]]をセーブします。
   *
   * @param inventoryContents セーブ対象の[[InventoryContents]]
   */
  def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        val serializedInventory =
          ItemListSerialization.serializeToBase64(inventoryContents.inventoryContents.asJava)
        sql"UPDATE playerdata SET shareinv = $serializedInventory WHERE uuid = '${targetUuid.toString}'"
          .execute()
          .apply()
      }
    }

  /**
   * セーブされている[[InventoryContents]]をロードします。
   */
  override def load(targetUuid: UUID): F[Option[InventoryContents]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        val serializedInventoryOpt =
          sql"SELECT shareinv FROM playerdata WHERE uuid = '${targetUuid.toString}'"
            .map(rs => rs.string("shareinv"))
            .single()
            .apply()

        serializedInventoryOpt.map(serializedInventory =>
          InventoryContents.ofNonEmpty(
            ItemListSerialization.deserializeFromBase64(serializedInventory).asScala.toList
          )
        )
      }
    }

//  import cats.implicits._
//
//  override def read(uuid: UUID): F[Option[SharedFlag]] = for {
//    loadedContents <- load(uuid)
//  } yield {
//    loadedContents match {
//      case Some(_) => Some(SharedFlag.Sharing)
//      case None    => Some(SharedFlag.NotSharing)
//    }
//  }
//
//  override def write(key: UUID, value: SharedFlag): F[Unit] = Sync[F].pure(())
}
