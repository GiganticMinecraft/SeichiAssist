package com.github.unchama.buildassist.domain.playerdata

import com.github.unchama.util.RefDict

import java.util.UUID

/**
 * [[BuildAmountData]]の永続化を担うオブジェクトのtrait。
 */
trait BuildAmountDataPersistence[F[_]] extends RefDict[F, UUID, BuildAmountData]
