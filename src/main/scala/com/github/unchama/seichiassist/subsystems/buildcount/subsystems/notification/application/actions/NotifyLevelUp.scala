package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions

import com.github.unchama.generic.Diff
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildLevel
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData

trait NotifyLevelUp[F[_], Player] {

  def ofBuildLevelTo(player: Player)(diff: Diff[BuildLevel]): F[Unit]
  def ofBuildAmountTo(player: Player)(diff: Diff[BuildAmountData]): F[Unit]

}

object NotifyLevelUp {

  def apply[F[_], Player](implicit ev: NotifyLevelUp[F, Player]): NotifyLevelUp[F, Player] = ev

}
