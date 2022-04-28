package com.github.unchama.seichiassist.subsystems.gacha.infrastructure

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.ItemStackCodec
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPersistence,
  GachaPrize,
  GachaPrizeId
}
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
            val itemStack = ItemStackCodec.fromString(rs.string("itemstack"))
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
        sql"""insert into gachadata 
             |  (id,amount,probability,itemstack)
             |  values (${gachaPrize.id.id},${gachaPrize.itemStack.getAmount},
             |  ${gachaPrize.probability},${gachaPrize.itemStack},
             |  ${ItemStackCodec.fromBukkitItemStack(gachaPrize.itemStack)})
             |  on duplicate key update 
             |  amount = ${gachaPrize.itemStack.getAmount},
             |  probability = ${gachaPrize.probability},
             |  itemstack = ${ItemStackCodec.fromBukkitItemStack(gachaPrize.itemStack)}"""
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
