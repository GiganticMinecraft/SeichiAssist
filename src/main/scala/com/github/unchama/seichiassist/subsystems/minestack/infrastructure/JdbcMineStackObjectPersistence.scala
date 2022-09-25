package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.minestack.domain.{
  MineStackObject,
  MineStackObjectPersistence,
  MineStackObjectWithAmount
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcMineStackObjectPersistence[F[_]: Sync](allMineStackObjects: List[MineStackObject])
    extends MineStackObjectPersistence[F] {

  override def read(key: UUID): F[Option[List[MineStackObjectWithAmount]]] = Sync[F].delay {
    val mineStackObjectsWithAmount = DB.localTx { implicit session =>
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

  override def write(key: UUID, value: List[MineStackObjectWithAmount]): F[Unit] = ???

}
