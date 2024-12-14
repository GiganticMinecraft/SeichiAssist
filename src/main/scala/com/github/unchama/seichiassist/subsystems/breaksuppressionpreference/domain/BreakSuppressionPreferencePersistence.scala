package com.github.unchama.seichiassist.subsystems.breaksuppressionpreference.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait BreakSuppressionPreferenceConfigPersistence[F[_]] extends RefDict[F, UUID, BreakSuppressionPreference]
