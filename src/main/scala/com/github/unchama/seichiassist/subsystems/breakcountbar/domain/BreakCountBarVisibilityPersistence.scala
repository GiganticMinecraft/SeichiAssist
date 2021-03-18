package com.github.unchama.seichiassist.subsystems.breakcountbar.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait BreakCountBarVisibilityPersistence[F[_]] extends RefDict[F, UUID, BreakCountBarVisibility]
