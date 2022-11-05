package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.minestack.domain.{
  MineStackGachaObjectId,
  MineStackObjectPersistenceByMineStackGachaData
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcMineStackObjectPersistenceByMineStackGachaData[F[_]: Sync, ItemStack]
    extends MineStackObjectPersistenceByMineStackGachaData[F, ItemStack] {

  override def addMineStackGachaObject(
    id: MineStackGachaObjectId,
    objectName: String
  ): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"INSERT INTO mine_stack_gacha_objects (id, mine_stack_object_name) VALUES (${id.value}, ${objectName})"
        .execute()
        .apply()
    }
  }

  override def deleteMineStackGachaObject(id: MineStackGachaObjectId): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"DELETE FROM mine_stack_gacha_objects WHERE id = ${id.value}".execute().apply()
    }
  }
}
