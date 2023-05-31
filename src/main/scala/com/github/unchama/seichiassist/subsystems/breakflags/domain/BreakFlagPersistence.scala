package com.github.unchama.seichiassist.subsystems.breakflags.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait BreakFlagPersistence[F[_]] extends RefDict[F, UUID, List[BreakFlag]]
