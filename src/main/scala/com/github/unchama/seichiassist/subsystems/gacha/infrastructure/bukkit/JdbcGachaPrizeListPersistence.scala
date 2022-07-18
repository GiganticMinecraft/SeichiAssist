package com.github.unchama.seichiassist.subsystems.gacha.infrastructure.bukkit

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.codec.ItemStackCodec
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrizeListPersistence,
  GachaPrizeId,
  GachaProbability,
  bukkit
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcGachaPrizeListPersistence[F[_]: Sync: NonServerThreadContextShift]
    extends GachaPrizeListPersistence[F] {

  /**
   * ガチャアイテムとして登録されているアイテムリストをGachaPrizeのVectorとして返します。
   */
  override def list: F[Vector[GachaPrize]] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"select * from gachadata"
          .map { rs =>
            val probability = rs.double("probability")
            // TODO ガチャアイテムに対して記名を行うかどうかを確率に依存すべきではない
            val itemStack = ItemStackCodec.fromString(rs.string("itemstack"))
            itemStack.setAmount(rs.int("amount"))
            bukkit.GachaPrize(
              itemStack,
              GachaProbability(probability),
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
   * ガチャリストを更新します。
   */
  override def set(gachaPrizesList: Vector[GachaPrize]): F[Unit] = {
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"truncate table gachadata".execute().apply()
        gachaPrizesList.foreach { gachaPrize =>
          val itemStackString = ItemStackCodec.toString(gachaPrize.itemStack)
          val amount = gachaPrize.itemStack.getAmount
          sql"insert into gachadata values (${gachaPrize.id.id},$amount,${gachaPrize.probability.value},$itemStackString)"
            .execute()
            .apply()
        }
      }
    }
  }

}
