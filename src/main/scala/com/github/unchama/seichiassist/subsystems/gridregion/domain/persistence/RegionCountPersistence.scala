package com.github.unchama.seichiassist.subsystems.gridregion.domain.persistence

import com.github.unchama.generic.RefDict
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionCount

import java.util.UUID

trait RegionCountPersistence[F[_]] extends RefDict[F, UUID, RegionCount]
