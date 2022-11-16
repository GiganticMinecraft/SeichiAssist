package com.github.unchama.seichiassist.subsystems.gridregion.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait RegionNumberPersistence[F[_]] extends RefDict[F, UUID, RegionNumber]
