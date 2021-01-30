package com.github.unchama.seichiassist.subsystems.breakcount.application.actions

import cats.effect.{Effect, Sync}
import com.github.unchama.generic.Diff
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{SeichiLevel, SeichiStarLevel}
import fs2.concurrent.Topic

trait NotifyLevelUp[F[_], Player] {

  import cats.implicits._

  def ofSeichiLevelTo(player: Player)(diff: Diff[SeichiLevel]): F[Unit]

  def ofSeichiStarLevelTo(player: Player)(diff: Diff[SeichiStarLevel]): F[Unit]

  def alsoNotifyTo[G[_]](topic: Topic[G, Option[(Player, Diff[SeichiLevel])]])
                        (implicit F: Sync[F], G: Effect[G]): NotifyLevelUp[F, Player] = new NotifyLevelUp[F, Player] {
    override def ofSeichiLevelTo(player: Player)(diff: Diff[SeichiLevel]): F[Unit] =
      NotifyLevelUp.this.ofSeichiLevelTo(player)(diff) *>
        EffectExtra.runAsyncAndForget[G, F, Unit](topic.publish1(Some(player, diff)).as(()))

    override def ofSeichiStarLevelTo(player: Player)(diff: Diff[SeichiStarLevel]): F[Unit] =
      NotifyLevelUp.this.ofSeichiStarLevelTo(player)(diff)
  }

}

object NotifyLevelUp {

  def apply[F[_], Player](implicit ev: NotifyLevelUp[F, Player]): NotifyLevelUp[F, Player] = ev

}
