package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.PocketInventoryPersistence
import com.github.unchama.seichiassist.util.BukkitSerialization
import org.bukkit.inventory.Inventory
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBukkitPocketInventoryPersistence[F[_]: Sync]
    extends PocketInventoryPersistence[F, Inventory] {

  // TODO BukkitSerializationのロジックをこっちに持ってくる

  override def read(key: UUID): F[Option[Inventory]] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"select inventory from playerdata where uuid = ${key.toString}"
        .map { rs => BukkitSerialization.fromBase64forPocket(rs.string("inventory")) }
        .headOption()
    }
  }

  override def write(key: UUID, value: Inventory): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      val encoded = BukkitSerialization.toBase64(value)

      sql"update playerdata set inventory = $encoded where uuid = ${key.toString}".update()
    }
  }

}
