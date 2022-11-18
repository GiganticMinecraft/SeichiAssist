package com.github.unchama.seichiassist.subsystems.gridregion.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  RegionTemplateId,
  RegionTemplatePersistence,
  RegionUnit,
  RegionUnits
}
import scalikejdbc._

import java.util.UUID

class JdbcRegionTemplatePersistence[F[_]: Sync] extends RegionTemplatePersistence[F] {

  override def read(uuid: UUID): F[Option[Map[RegionTemplateId, RegionUnits]]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val regionUnits =
        sql"""SELECT ahead_length, right_length, behind_length, left_length
             | FROM grid_template WHERE designer_uuid = ${uuid.toString}
             | ORDER BY id DESC"""
          .stripMargin
          .map(rs =>
            RegionTemplateId(rs.int("id")) -> RegionUnits(
              RegionUnit(rs.int("ahead_length")),
              RegionUnit(rs.int("right_length")),
              RegionUnit(rs.int("behind_length")),
              RegionUnit(rs.int("left_length"))
            )
          )
          .toList()
          .apply()
          .toMap

      Some(regionUnits)
    }
  }

  override def write(uuid: UUID, value: Map[RegionTemplateId, RegionUnits]): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        val batchParams = value.map {
          case (id, regionUnits) =>
            Seq(
              regionUnits.ahead.units,
              regionUnits.right.units,
              regionUnits.behind.units,
              regionUnits.left.units,
              id.value
            )
        }.toSeq

        sql"""UPDATE grid_template SET ahead_length = ?, right_length = ?, behind_length = ?, left_length = ?
             | WHERE designer_uuid = ${uuid.toString} AND id = ?"""
          .stripMargin
          .batch(batchParams: _*)
          .apply[List]()
      }
    }

}
