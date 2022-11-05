package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.seichiassist.subsystems.minestack.domain.{
  MineStackGachaObjectId,
  MineStackObjectPersistenceByMineStackGachaData
}
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectCategory
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcMineStackObjectPersistenceByMineStackGachaData[F[_]: Sync, ItemStack](
  implicit serializeAndDeserialize: SerializeAndDeserialize[Nothing, ItemStack]
) extends MineStackObjectPersistenceByMineStackGachaData[F, ItemStack] {

  override def load: F[List[MineStackObject[ItemStack]]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT obj_name, itemstack FROM msgachadata"
        .map { rs =>
          val objName = rs.string("obj_name")
          serializeAndDeserialize
            .deserialize(rs.string("itemStack"))
            .map { itemStack =>
              MineStackObject.MineStackObjectByItemStack(
                MineStackObjectCategory.GACHA_PRIZES,
                objName,
                None,
                hasNameLore = true,
                itemStack
              )
            }
            .merge
        }
        .toList()
        .apply()
    }
  }

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
