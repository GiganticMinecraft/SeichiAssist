package com.github.unchama.seichiassist.subsystems.breakcount.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait SeichiAmountDataPersistence[F[_]] extends RefDict[F, UUID, SeichiAmountData]
