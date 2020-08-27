package com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.targets

import cats.effect.Sync
import com.github.unchama.itemmigration.domain.{ItemMigrationTarget, ItemStackConversion}
import com.github.unchama.itemmigration.util.MigrationHelper
import com.github.unchama.seichiassist.util.{BukkitSerialization, ItemListSerialization}
import org.bukkit.Material
import scalikejdbc._

class SeichiAssistPersistedItems[F[_]](implicit dBSession: DBSession, F: Sync[F]) extends ItemMigrationTarget[F] {

  import scala.jdk.CollectionConverters._

  private def convertSharedInventory(persistedSharedInventory: String)(conversion: ItemStackConversion): String = {
    ItemListSerialization.serializeToBase64 {
      ItemListSerialization
        .deserializeFromBase64(persistedSharedInventory)
        .asScala
        .map { stack =>
          if (stack != null && stack.getType != Material.AIR) {
            conversion(stack)
          } else {
            stack
          }
        }
        .asJava
    }
  }

  private def convertPocketInventory(persistedPocketInventory: String)(conversion: ItemStackConversion): String = {
    BukkitSerialization.toBase64 {
      val pocketInventory = BukkitSerialization.fromBase64forPocket(persistedPocketInventory)

      MigrationHelper.convertEachStackIn(pocketInventory)(conversion)

      pocketInventory
    }
  }

  override def runMigration(conversion: ItemStackConversion): F[Unit] = F.delay {
    val triples = sql"select uuid, shareinv, inventory from seichiassist.playerdata"
      .map { rs =>
        (rs.string("uuid"), rs.stringOpt("shareinv"), rs.stringOpt("inventory"))
      }
      .list().apply()

    val batchParam: Seq[Seq[String]] = triples.map { case (uuid, shareinv, inventory) =>
      val newSharedInventory = shareinv.filter(_.nonEmpty).map(convertSharedInventory(_)(conversion))
      val newPocketInventory = inventory.filter(_.nonEmpty).map(convertPocketInventory(_)(conversion))

      Seq(newSharedInventory.getOrElse(""), newPocketInventory.getOrElse(""), uuid)
    }

    sql"""
      update seichiassist.playerdata
        set shareinv = ?, inventory = ?
        where uuid = ?
    """
      .batch(batchParam: _*)
      .apply[List]()
  }

}
