package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.GachaPrizeId
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackGachaObject
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.MineStackGachaObjectPersistence
import scalikejdbc._

class JdbcMineStackGachaObjectPersistence[F[_]: Sync, ItemStack, Player](
  implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player]
) extends MineStackGachaObjectPersistence[F, ItemStack] {
  import cats.implicits._

  override def getAllMineStackGachaObjects: F[Vector[MineStackGachaObject[ItemStack]]] = for {
    allGachaPrizeList <- gachaPrizeAPI.allGachaPrizeList
    mineStackGachaObjects <- Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"SELECT id, mine_stack_object_name FROM mine_stack_gacha_objects"
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
  } yield mineStackGachaObjects.toVector

}
