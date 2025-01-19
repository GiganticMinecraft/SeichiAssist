package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectWithAmount
}
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.MineStackObjectPersistence
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
          .filterNot(_.isEmpty)
          .map(_.get)
      }

      Some(mineStackObjectsWithAmount)
    }

  override def write(key: UUID, value: List[MineStackObjectWithAmount[ItemStack]]): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        val batchParams = value.map { mineStackObjectWithAmount =>
          val objectName = mineStackObjectWithAmount.mineStackObject.mineStackObjectName
          val amount = mineStackObjectWithAmount.amount
          Seq(key.toString, objectName, amount)
        }

        sql"""INSERT INTO mine_stack 
             | (player_uuid, object_name, amount) 
             | VALUES (?, ?, ?)
             | ON DUPLICATE KEY UPDATE
             | amount = VALUES(amount)
             """.stripMargin.batch(batchParams: _*).apply[List]()
      }
    }

}
