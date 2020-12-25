package com.github.unchama.buildassist.domain.actions

import com.github.unchama.buildassist.domain.explevel.BuildLevel
import com.github.unchama.generic.Diff

/**
 * 建築レベルの変化を通知するためのインターフェース。
 */
trait LevelUpNotifier[F[_], Player] {

  def notifyTo(player: Player)(diff: Diff[BuildLevel]): F[Unit]

}

object LevelUpNotifier {

  def apply[F[_], Player](implicit ev: LevelUpNotifier[F, Player]): LevelUpNotifier[F, Player] = ev

}
