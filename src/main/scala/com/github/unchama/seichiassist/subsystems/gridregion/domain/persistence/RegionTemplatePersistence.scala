package com.github.unchama.seichiassist.subsystems.gridregion.domain.persistence

import com.github.unchama.generic.RefDict
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionTemplate

import java.util.UUID

trait RegionTemplatePersistence[F[_]] extends RefDict[F, UUID, Vector[RegionTemplate]]
