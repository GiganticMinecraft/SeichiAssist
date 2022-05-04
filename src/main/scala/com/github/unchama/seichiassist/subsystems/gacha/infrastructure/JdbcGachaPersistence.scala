package com.github.unchama.seichiassist.subsystems.gacha.infrastructure

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.Wrapper.ItemStackStringWrapper
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
            val probability = rs.double("probability")
            // TODO ガチャアイテムに対して記名を行うかどうかを確率に依存すべきではない
            GachaPrize(
              ItemStackStringWrapper(rs.string("itemstack"), rs.int("amount")),
              probability,
              probability < 0.1,
              GachaPrizeId(rs.int("id"))
            )
          }
          .toList()
          .apply()
          .toVector
      }
    }
  }

  /**
   * ガチャアイテムを追加します。
   * idが同じだった場合は置き換えられます
   */
  override def upsert(gachaPrize: GachaPrize): F[Unit] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay[Unit] {
      DB.localTx { implicit session =>
        sql"""insert into gachadata 
             |  (id,amount,probability,itemstack)
             |  values (${gachaPrize.id.id},${gachaPrize.itemStack.amount},
             |  ${gachaPrize.probability},${gachaPrize.itemStack},
             |  ${gachaPrize.itemStack.itemStack})
             |  on duplicate key update 
             |  amount = ${gachaPrize.itemStack.amount},
             |  probability = ${gachaPrize.probability},
             |  itemstack = ${gachaPrize.itemStack}""".stripMargin.execute().apply()
      }
    }
  }

  /**
   * ガチャアイテムを削除します。
   */
  override def remove(id: GachaPrizeId): F[Boolean] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        sql"delete from gachadata where id = $id".execute().apply()
      }
    }
  }
}
