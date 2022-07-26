package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait FairyValidTimesPersistence[F[_]] extends RefDict[F, UUID, FairyValidTimes]
