package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectPersistence,
  MineStackObjectWithAmount
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID
import scala.collection.IndexedSeq.iterableFactory

class JdbcMineStackObjectPersistence[F[_]: Sync](allMineStackObjects: List[MineStackObject])
    extends MineStackObjectPersistence[F] {

  override def read(key: UUID): F[Option[List[MineStackObjectWithAmount]]] = Sync[F].delay {
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

  override def write(key: UUID, value: List[MineStackObjectWithAmount]): F[Unit] =
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

}
