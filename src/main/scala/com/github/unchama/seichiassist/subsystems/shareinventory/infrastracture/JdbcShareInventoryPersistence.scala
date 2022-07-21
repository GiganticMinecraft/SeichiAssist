package com.github.unchama.seichiassist.subsystems.shareinventory.infrastracture

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.{
  InventoryContents,
  ShareInventoryPersistence,
  ShareInventoryResult
}
import com.github.unchama.seichiassist.util.ItemListSerialization
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID
import scala.jdk.CollectionConverters._

class JdbcShareInventoryPersistence[F[_]: Sync] extends ShareInventoryPersistence[F] {

  /**
   * [[InventoryContents]]をセーブします。
   *
   * @param inventoryContents セーブ対象の[[InventoryContents]]
   */
  override def saveSerializedShareInventory(
    targetUuid: UUID,
    inventoryContents: InventoryContents
  ): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        val serializedInventory =
          ItemListSerialization.serializeToBase64(inventoryContents.inventoryContents.asJava)
        sql"UPDATE playerdata SET shareinv = '$serializedInventory' WHERE uuid = '${targetUuid.toString}'"
          .execute()
          .apply()
      }
    }

  /**
   * セーブされている[[InventoryContents]]をロードします。
   */
  override def loadSerializedShareInventory(targetUuid: UUID): F[InventoryContents] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        val serializedInventory =
          sql"SELECT shareinv FROM playerdata WHERE uuid = '${targetUuid.toString}'"
            .map(rs => rs.string("shareinv"))
            .toList()
            .apply()
            .head
        InventoryContents(
          ItemListSerialization.deserializeFromBase64(serializedInventory).asScala.toList
        )
      }
    }
}
