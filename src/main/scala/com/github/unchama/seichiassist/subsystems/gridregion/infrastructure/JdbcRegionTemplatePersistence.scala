package com.github.unchama.seichiassist.subsystems.gridregion.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gridregion.domain.persistence.RegionTemplatePersistence
import com.github.unchama.seichiassist.subsystems.gridregion.domain.regiontemplate.{
  RegionTemplate,
  RegionTemplateId
}
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  RegionUnit,
  RegionUnits,
  regiontemplate
}
import scalikejdbc._

import java.util.UUID

class JdbcRegionTemplatePersistence[F[_]: Sync] extends RegionTemplatePersistence[F] {

  override def regionTemplates(uuid: UUID): F[Vector[RegionTemplate]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT ahead_length, right_length, behind_length, left_length
           | FROM grid_template
           | WHERE designer_uuid = ${uuid.toString}
           | ORDER BY id DESC"""
        .stripMargin
        .map { rs =>
          val id = RegionTemplateId(rs.int("id"))
          val regionUnits = RegionUnits(
            RegionUnit(rs.int("ahead_length")),
            RegionUnit(rs.int("right_length")),
            RegionUnit(rs.int("behind_length")),
            RegionUnit(rs.int("left_length"))
          )

          regiontemplate.RegionTemplate(id, regionUnits)
        }
        .toList()
        .apply()
        .toVector
    }
  }

  override def saveRegionTemplate(uuid: UUID, value: RegionTemplate): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"""INSERT INTO grid_template
             | (id, designer_uuid, ahead_length, right_length, behind_length, left_length)
             | VALUES (
             |  ${value.templateId},
             |  ${uuid.toString},
             |  ${value.regionUnits.ahead.units},
             |  ${value.regionUnits.right.units},
             |  ${value.regionUnits.behind.units},
             |  ${value.regionUnits.left.units}
             | )
             | ON DUPLICATE KEY UPDATE
             |  ahead_length = ${value.regionUnits.ahead.units}
             |  right_length = ${value.regionUnits.right.units}
             |  behind_length = ${value.regionUnits.behind.units}
             |  left_length = ${value.regionUnits.left.units}
           """.stripMargin.execute().apply()
      }
    }

}
