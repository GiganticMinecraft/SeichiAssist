package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions

import com.github.unchama.generic.Diff
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData

trait NotifyBuildAmountThreshold[F[_], Player] {

  def ofBuildAmountTo(player: Player)(diff: Diff[BuildAmountData]): F[Unit]

}

object NotifyBuildAmountThreshold {

  def apply[F[_], Player](
    implicit ev: NotifyBuildAmountThreshold[F, Player]
  ): NotifyBuildAmountThreshold[F, Player] = ev

}
