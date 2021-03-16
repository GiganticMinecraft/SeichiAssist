package com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint

import com.github.unchama.generic.RefDict

import java.util.UUID

trait GachaPointPersistence[F[_]] extends RefDict[F, UUID, GachaPoint]
