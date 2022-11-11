package com.github.unchama.seichiassist.subsystems.gridregion.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait RegionUnitsPersistence[F[_]] extends RefDict[F, UUID, RegionUnits]
