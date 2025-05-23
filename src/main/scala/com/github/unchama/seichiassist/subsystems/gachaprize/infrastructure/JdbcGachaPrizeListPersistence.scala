package com.github.unchama.seichiassist.subsystems.gachaprize.infrastructure

import cats.effect.Sync
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.seichiassist.subsystems.gachaprize.domain._
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventName
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrizeTableEntry
import scalikejdbc._
import com.github.unchama.generic.Cloneable
import com.github.unchama.seichiassist.subsystems.gachaprize.domain

class JdbcGachaPrizeListPersistence[F[_]: Sync, ItemStack: Cloneable](
  implicit serializeAndDeserialize: SerializeAndDeserialize[Nothing, ItemStack]
) extends GachaPrizeListPersistence[F, ItemStack] {

  override def list: F[Vector[GachaPrizeTableEntry[ItemStack]]] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT gachadata.id AS gacha_prize_id, probability, itemstack, event_name, event_start_time, event_end_time FROM gachadata
             | LEFT OUTER JOIN gacha_events
             | ON gachadata.event_id = gacha_events.id"""
          .stripMargin
          .map { rs =>
            // TODO ガチャアイテムに対して記名を行うかどうかを確率に依存すべきではない
            val probability = rs.double("probability")
            val gachaEvent = rs.stringOpt("event_name").map { eventName =>
              GachaEvent(
                GachaEventName(eventName),
                rs.localDate("event_start_time"),
                rs.localDate("event_end_time")
              )
            }

            serializeAndDeserialize
              .deserialize(rs.string("itemstack"))
              .map { itemStack =>
                domain.GachaPrizeTableEntry(
                  itemStack,
                  GachaProbability(probability),
                  probability < 0.1,
                  GachaPrizeId(rs.int("gacha_prize_id")),
                  gachaEvent
                )
              }
              .merge
          }
          .toList()
          .toVector
      }
    }
  }

  override def upsertGachaPrize(gachaPrize: GachaPrizeTableEntry[ItemStack]): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        val eventId = gachaPrize.gachaEvent.flatMap { gachaEvent =>
          sql"SELECT id FROM gacha_events WHERE event_name = ${gachaEvent.eventName.name}"
            .map(_.int("id"))
            .single()
        }

        val serializedItemStack = serializeAndDeserialize.serialize(gachaPrize.itemStack)

        sql"""INSERT INTO gachadata (id, probability, itemstack, event_id)
             | VALUES (
             |   ${gachaPrize.id.id},
             |   ${gachaPrize.probability.value},
             |   $serializedItemStack,
             |   $eventId
             | )
             | ON DUPLICATE KEY UPDATE
             |   probability = ${gachaPrize.probability.value},
             |   itemstack = $serializedItemStack
         """.stripMargin.execute()
      }
    }

  override def removeGachaPrize(gachaPrizeId: GachaPrizeId): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"DELETE FROM gachadata WHERE id = ${gachaPrizeId.id}".execute()
    }
  }

  override def duplicateDefaultGachaPrizes(gachaEvent: GachaEvent): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      val eventName = gachaEvent.eventName.name

      sql"""INSERT INTO gachadata (probability, itemstack, event_id)
           | (SELECT probability, itemstack, (SELECT id FROM gacha_events WHERE event_name = $eventName) FROM gachadata
           | WHERE event_id IS NULL)
         """.stripMargin.execute()
    }
  }

}
