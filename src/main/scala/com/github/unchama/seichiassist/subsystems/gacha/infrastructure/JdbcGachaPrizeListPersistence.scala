package com.github.unchama.seichiassist.subsystems.gacha.infrastructure

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.seichiassist.subsystems.gacha.domain._
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcGachaPrizeListPersistence[F[_]: Sync: NonServerThreadContextShift, ItemStack](
  implicit serializeAndDeserialize: SerializeAndDeserialize[Unit, ItemStack]
) extends GachaPrizeListPersistence[F, ItemStack] {

  /**
   * ガチャアイテムとして登録されているアイテムリストをGachaPrizeのVectorとして返します。
   */
  override def list: F[Vector[GachaPrize[ItemStack]]] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"select * from gachadata"
          .map { rs =>
            val probability = rs.double("probability")
            // TODO ガチャアイテムに対して記名を行うかどうかを確率に依存すべきではない
            serializeAndDeserialize
              .deserialize(rs.string("itemstack"))
              .map { itemStack =>
                GachaPrize(
                  itemStack,
                  GachaProbability(probability),
                  probability < 0.1,
                  GachaPrizeId(rs.int("id"))
                )
              }
              .toOption
              .get
          }
          .toList()
          .apply()
          .toVector
      }
    }
  }

  /**
   * ガチャリストを更新します。
   */
  override def set(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit] = {
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"truncate table gachadata".execute().apply()
        val batchParams = gachaPrizesList.map { gachaPrize =>
          Seq(
            gachaPrize.id.id,
            gachaPrize.probability.value,
            serializeAndDeserialize.serialize(gachaPrize.itemStack)
          )
        }
        sql"insert into gachadata values (?,?,?)".batch(batchParams).apply[List]()
      }
    }
  }

}
