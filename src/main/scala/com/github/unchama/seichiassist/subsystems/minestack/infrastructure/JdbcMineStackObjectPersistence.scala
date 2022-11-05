package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectPersistence,
  MineStackObjectWithAmount
}
import scalikejdbc._

import java.util.UUID

class JdbcMineStackObjectPersistence[F[_]: Sync, ItemStack, Player](
  allMineStackObjects: Vector[MineStackObject[ItemStack]]
) extends MineStackObjectPersistence[F, ItemStack] {

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
      DB.localTx { implicit session =>
        val mineStackObjectDetails = value.map { mineStackObjectWithAmount =>
          val objectName = mineStackObjectWithAmount.mineStackObject.mineStackObjectName
          val amount = mineStackObjectWithAmount.amount
          Seq("mine_stack_object_name" -> objectName, "mine_stack_object_amount" -> amount)
        }

        sql"""INSERT INTO mine_stack 
             | (player_uuid, object_name, amount) 
             | VALUES (${key.toString}, {mine_stack_object_name}, {mine_stack_object_amount})
             | ON DUPLICATE KEY UPDATE
             | amount = {mine_stack_object_amount}
             """.stripMargin.batchByName(mineStackObjectDetails: _*).apply[List]
      }
    }

}
