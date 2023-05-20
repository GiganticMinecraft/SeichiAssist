package com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.application.actions

import com.github.unchama.generic.Diff
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{
  SeichiLevel,
  SeichiStarLevel
}

trait NotifyLevelUp[F[_], Player] {

  def ofSeichiAmountTo(player: Player)(diff: Diff[SeichiAmountData]): F[Unit]

  def ofSeichiLevelTo(player: Player)(diff: Diff[SeichiLevel]): F[Unit]

  def ofSeichiStarLevelTo(player: Player)(diff: Diff[SeichiStarLevel]): F[Unit]

}

object NotifyLevelUp {

  def apply[F[_], Player](implicit ev: NotifyLevelUp[F, Player]): NotifyLevelUp[F, Player] = ev

}
