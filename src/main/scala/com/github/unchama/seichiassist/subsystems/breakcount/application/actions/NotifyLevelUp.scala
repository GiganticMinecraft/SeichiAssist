package com.github.unchama.seichiassist.subsystems.breakcount.application.actions

import cats.Applicative
import com.github.unchama.generic.Diff
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{SeichiLevel, SeichiStarLevel}

trait NotifyLevelUp[F[_], Player] {

  def ofSeichiLevelTo(player: Player)(diff: Diff[SeichiLevel]): F[Unit]

  def ofSeichiStarLevelTo(player: Player)(diff: Diff[SeichiStarLevel]): F[Unit]

  def andThenOnSeichiLevelup(action: Player => Diff[SeichiLevel] => F[Unit])
                            (implicit F: Applicative[F]): NotifyLevelUp[F, Player] = new NotifyLevelUp[F, Player] {
    override def ofSeichiLevelTo(player: Player)(diff: Diff[SeichiLevel]): F[Unit] =
      F.*>(NotifyLevelUp.this.ofSeichiLevelTo(player)(diff))(action(player)(diff))

    override def ofSeichiStarLevelTo(player: Player)(diff: Diff[SeichiStarLevel]): F[Unit] =
      NotifyLevelUp.this.ofSeichiStarLevelTo(player)(diff)
  }

}

object NotifyLevelUp {

  def apply[F[_], Player](implicit ev: NotifyLevelUp[F, Player]): NotifyLevelUp[F, Player] = ev

}
