package com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata

import com.github.unchama.generic.RefDict

import java.util.UUID

/**
 * [[BuildAmountData]]の永続化を担うオブジェクトのtrait。
 */
trait BuildAmountDataPersistence[F[_]] extends RefDict[F, UUID, BuildAmountData]
