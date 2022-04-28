package com.github.unchama.seichiassist.subsystems.gacha.infrastructure

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPersistence,
  GachaPrize,
  GachaPrizeId
}
import com.github.unchama.seichiassist.util.BukkitSerialization
import org.bukkit.Bukkit
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcGachaPersistence[F[_]: Sync: NonServerThreadContextShift]
    extends GachaPersistence[F] {

  import cats.implicits._

  /**
   * ガチャアイテムとして登録されているアイテムリストをGachaPrizeのVectorとして返します。
   */
  override def list: F[Vector[GachaPrize]] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        sql"select * from gachadata"
          .map { rs =>
            val itemStack = BukkitSerialization.fromBase64(rs.string("itemstack")).getItem(0)
            val probability = rs.double("probability")
            itemStack.setAmount(rs.int("amount"))
            // TODO ガチャアイテムに対して記名を行うかどうかを確率に依存すべきではない
            GachaPrize(itemStack, probability, probability < 0.1, GachaPrizeId(rs.int("id")))
          }
          .toList()
          .apply()
          .toVector
      }
    }
  }

  /**
   * ガチャアイテムを追加します。
   */
  override def upsert(gachaPrize: GachaPrize): F[Unit] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay[Unit] {
      DB.localTx { implicit session =>
        val inventory = Bukkit.getServer.createInventory(null, 9)
        inventory.setItem(0, gachaPrize.itemStack)

        sql"""insert into gachadata 
             |  (id,amount,probability,itemstack)
             |  values (${gachaPrize.id.id},${gachaPrize.itemStack.getAmount},
             |  ${gachaPrize.probability},${gachaPrize.itemStack},
             |  ${BukkitSerialization.toBase64(inventory)})
             |  on duplicate key update 
             |  amount = ${gachaPrize.itemStack.getAmount},
             |  probability = ${gachaPrize.probability},
             |  itemstack = ${BukkitSerialization.toBase64(inventory)}"""
          .stripMargin
          .execute()
          .apply()
      }
    }
  }

  /**
   * ガチャアイテムを削除します。
   */
  override def remove(id: GachaPrizeId): F[Boolean] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session => sql"delete from gachadata id = $id".execute().apply() }
    }
  }
}
