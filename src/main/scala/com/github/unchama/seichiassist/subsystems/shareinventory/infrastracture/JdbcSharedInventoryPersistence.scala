package com.github.unchama.seichiassist.subsystems.shareinventory.infrastracture

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.SharedInventoryPersistence
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.bukkit.InventoryContents
import com.github.unchama.seichiassist.util.ItemListSerialization
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID
import scala.jdk.CollectionConverters._

class JdbcSharedInventoryPersistence[F[_]: Sync] extends SharedInventoryPersistence[F] {

  /**
   * [[InventoryContents]]をセーブします。
   *
   * @param inventoryContents セーブ対象の[[InventoryContents]]
   */
  override def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit] =
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

        serializedInventoryOpt match {
          case Some(serializedInventory) =>
            Some(
              InventoryContents(
                ItemListSerialization.deserializeFromBase64(serializedInventory).asScala.toList
              )
            )
          case None => None
        }
      }
    }

  /**
   * セーブされている[[InventoryContents]]を完全に削除します。
   */
  override def clear(targetUuid: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET shareinv = '' WHERE uuid = '${targetUuid.toString}'"
        .execute()
        .apply()
    }
  }
}
