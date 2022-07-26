package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

/**
 * 妖精が有効な時間を管理するクラス
 */
final class FairyValidTimesState[F[_]: Sync] {

  private val validTimesState: Ref[F, Option[FairyValidTimes]] =
    Ref.unsafe[F, Option[FairyValidTimes]](None)

  def updateState(validTimes: FairyValidTimes): F[Unit] =
    validTimesState.set(Some(validTimes))

  def setNoneState(): F[Unit] = validTimesState.set(None)

  def fairyValidTimes: Ref[F, Option[FairyValidTimes]] = validTimesState

}
