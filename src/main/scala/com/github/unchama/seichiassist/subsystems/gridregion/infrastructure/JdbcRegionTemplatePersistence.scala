package com.github.unchama.seichiassist.subsystems.gridregion.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gridregion.domain.persistence.RegionTemplatePersistence
import com.github.unchama.seichiassist.subsystems.gridregion.domain.regiontemplate.{RegionTemplate, RegionTemplateId}
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{RegionUnitLength, RegionUnits, SubjectiveRegionShape, regiontemplate}
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
          val regionUnits = SubjectiveRegionShape(
            RegionUnitLength(rs.int("ahead_length")),
            RegionUnitLength(rs.int("right_length")),
            RegionUnitLength(rs.int("behind_length")),
            RegionUnitLength(rs.int("left_length"))
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
             |  ${value.regionUnits.ahead.rul},
             |  ${value.regionUnits.right.rul},
             |  ${value.regionUnits.behind.rul},
             |  ${value.regionUnits.left.rul}
             | )
             | ON DUPLICATE KEY UPDATE
             |  ahead_length = ${value.regionUnits.ahead.rul}
             |  right_length = ${value.regionUnits.right.rul}
             |  behind_length = ${value.regionUnits.behind.rul}
             |  left_length = ${value.regionUnits.left.rul}
           """.stripMargin.execute().apply()
      }
    }

}
