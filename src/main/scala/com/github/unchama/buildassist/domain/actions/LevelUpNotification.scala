package com.github.unchama.buildassist.domain.actions

import com.github.unchama.buildassist.domain.explevel.BuildLevel
import com.github.unchama.util.Diff

/**
 * 建築レベルの変化を通知するためのインターフェース。
 */
trait LevelUpNotification[F[_], Player] {

  def notifyTo(player: Player)(diff: Diff[BuildLevel]): F[Unit]

}

object LevelUpNotification {

  def apply[F[_], Player](implicit ev: LevelUpNotification[F, Player]): LevelUpNotification[F, Player] = ev

}
