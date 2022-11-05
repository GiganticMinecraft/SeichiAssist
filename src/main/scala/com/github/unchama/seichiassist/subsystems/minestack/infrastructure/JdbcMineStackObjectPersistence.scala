package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.GachaPrizeId
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackGachaObject
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectPersistence,
  MineStackObjectWithAmount
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID
import scala.collection.IndexedSeq.iterableFactory

class JdbcMineStackObjectPersistence[F[_]: Sync, ItemStack, Player](
  allMineStackObjects: Vector[MineStackObject[ItemStack]],
  gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player]
) extends MineStackObjectPersistence[F, ItemStack] {

  import cats.implicits._

  override def read(key: UUID): F[Option[List[MineStackObjectWithAmount[ItemStack]]]] =
    Sync[F].delay {
      val mineStackObjectsWithAmount = DB.readOnly { implicit session =>
        sql"SELECT object_name, amount FROM mine_stack WHERE player_uuid = ${key.toString}"
          .map { rs =>
            val objectName = rs.string("object_name")
            val amount = rs.long("amount")
            allMineStackObjects.find(_.mineStackObjectName == objectName) match {
              case Some(mineStackObject) =>
                Some(MineStackObjectWithAmount(mineStackObject, amount))
              case None =>
                None
            }
          }
          .toList()
          .apply()
          .filterNot(_.isEmpty)
          .map(_.get)
      }

      Some(mineStackObjectsWithAmount)
    }

  override def write(key: UUID, value: List[MineStackObjectWithAmount[ItemStack]]): F[Unit] =
    Sync[F].delay {
      val mineStackObjectDetails = value.map { mineStackObjectWithAmount =>
        val objectName = mineStackObjectWithAmount.mineStackObject.mineStackObjectName
        val amount = mineStackObjectWithAmount.amount
        Seq("object_name" -> objectName, "amount" -> amount)
      }
      DB.localTx { implicit session =>
        sql"""INSERT INTO mine_stack 
             | (player_uuid, object_name, amount) 
             | VALUES (${key.toString}, {object_name}, {amount})
             | ON DUPLICATE KEY UPDATE
             | amount = {amount}
             """.stripMargin.batchByName(mineStackObjectDetails: _*).apply()
      }
    }

  override def getAllMineStackGachaObjects: F[Vector[MineStackGachaObject[ItemStack]]] = for {
    allGachaPrizeList <- gachaPrizeAPI.allGachaPrizeList
    mineStackGachaObjects <- Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"SELECT id, mine_stack_object_name FROM mine_stack_gacha_objects"
          .toList()
          .map { rs =>
            val id = rs.int("id")
            allGachaPrizeList
              .find(_.id == GachaPrizeId(id))
              .map(MineStackGachaObject(rs.string("mine_stack_object_name"), _))
          }
          .toList()
          .apply()
          .collect { case Some(value) => value }
      }
    }
  } yield mineStackGachaObjects

}
