package com.github.unchama.buildassist.domain.actions

import com.github.unchama.buildassist.domain.explevel.BuildLevel
import com.github.unchama.generic.{ContextCoercion, Diff}

/**
 * 建築レベルの変化を通知するためのインターフェース。
 */
trait LevelUpNotifier[F[_], Player] {

  def notifyTo(player: Player)(diff: Diff[BuildLevel]): F[Unit]

}

object LevelUpNotifier {

  def apply[F[_], Player](implicit ev: LevelUpNotifier[F, Player]): LevelUpNotifier[F, Player] = ev

  implicit def coercion[
    F[_],
    G[_] : ContextCoercion[F, *[_]],
    Player
  ](implicit ev: LevelUpNotifier[F, Player]): LevelUpNotifier[G, Player] = new LevelUpNotifier[G, Player] {
    override def notifyTo(player: Player)(diff: Diff[BuildLevel]): G[Unit] =
      ContextCoercion(ev.notifyTo(player)(diff))
  }

}
