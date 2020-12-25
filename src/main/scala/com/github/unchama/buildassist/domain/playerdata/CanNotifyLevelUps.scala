package com.github.unchama.buildassist.domain.playerdata

import com.github.unchama.buildassist.domain.explevel.BuildLevel
import com.github.unchama.util.Diff

/**
 * 建築レベルの変化を通知するためのインターフェース。
 */
trait CanNotifyLevelUps[F[_], Player] {

  def notifyTo(player: Player)(diff: Diff[BuildLevel]): F[Unit]

}

object CanNotifyLevelUps {

  def apply[F[_], Player](implicit ev: CanNotifyLevelUps[F, Player]): CanNotifyLevelUps[F, Player] = ev

}
