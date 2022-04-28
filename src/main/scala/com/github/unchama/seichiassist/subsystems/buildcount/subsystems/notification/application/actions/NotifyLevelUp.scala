package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions

import com.github.unchama.generic.Diff
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildLevel

trait NotifyLevelUp[F[_], Player] {

  def ofBuildLevelTo(player: Player)(diff: Diff[BuildLevel]): F[Unit]

}

object NotifyLevelUp {

  def apply[F[_], Player](implicit ev: NotifyLevelUp[F, Player]): NotifyLevelUp[F, Player] = ev

}
