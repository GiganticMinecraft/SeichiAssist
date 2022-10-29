package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackObjectPersistenceByMineStackGachaData
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectCategory
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcMineStackObjectPersistenceByMineStackGachaData[F[_]: Sync, ItemStack <: Cloneable](
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

}
