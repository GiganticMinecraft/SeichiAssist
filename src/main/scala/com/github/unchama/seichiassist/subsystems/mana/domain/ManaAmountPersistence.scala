package com.github.unchama.seichiassist.subsystems.mana.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait ManaAmountPersistence[F[_]] extends RefDict[F, UUID, ManaAmount]
