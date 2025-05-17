package com.github.unchama.seichiassist.subsystems.gridregion.domain.persistence

import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionTemplate
import java.util.UUID

trait RegionTemplatePersistence[F[_]] {

  def regionTemplates(uuid: UUID): F[Vector[RegionTemplate]]

  def saveRegionTemplate(uuid: UUID, regionTemplate: RegionTemplate): F[Unit]

}
