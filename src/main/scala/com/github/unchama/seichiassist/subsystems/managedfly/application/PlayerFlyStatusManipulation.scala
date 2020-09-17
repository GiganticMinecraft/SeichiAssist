package com.github.unchama.seichiassist.subsystems.managedfly.application

import com.github.unchama.seichiassist.subsystems.managedfly.domain.{IdleStatus, PlayerFlyStatus}
import simulacrum.typeclass

/**
 * プレーヤーの飛行状態に`F`の文脈で干渉する手段を与える型クラスインスタンスのtrait。
 *
 * `F` は `Kleisli[G, Player, *]` の形をしていることを想定している。
 */
@typeclass trait PlayerFlyStatusManipulation[F[_]] extends AnyRef {
  /**
   * 飛行に必要な経験値をプレーヤーが持っていることを保証するアクション。
   * このアクションは [[PlayerExpNotEnough]] を `raiseError` してよい。
   */
  val ensurePlayerExp: F[Unit]

  /**
   * 飛行に必要な経験値をプレーヤーに消費させるアクション。
   * このアクションは [[PlayerExpNotEnough]] を `raiseError` してよい。
   */
  val consumePlayerExp: F[Unit]

  /**
   * プレーヤーがアイドル状態であるかを判定するアクション。
   */
  val isPlayerIdle: F[IdleStatus]

  /**
   * プレーヤーの飛行状態を[[PlayerFlyStatus]]に基づいてセットするアクション。
   */
  val synchronizeFlyStatus: PlayerFlyStatus => F[Unit]

  /**
   * [[InternalInterruption]] に対応して、プレーヤーへセッションが終了することを通知するアクション。
   */
  val sendNotificationsOnInterruption: InternalInterruption => F[Unit]
}
