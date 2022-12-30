package com.github.unchama.seichiassist.subsystems.gridregion.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait RegionTemplatePersistence[F[_]]
    extends RefDict[F, UUID, Map[RegionTemplateId, RegionUnits]]
