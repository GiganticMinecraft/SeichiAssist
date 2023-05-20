package com.github.unchama.seichiassist.subsystems.idletime.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class PlayerIdleMinuteRepository[F[_]: Sync] {

  private val idleMinuteRepository: Ref[F, IdleMinute] =
    Ref.unsafe[F, IdleMinute](IdleMinute.initial)

  /**
   * @return 現在のリポジトリの値を返す作用
   */
  def currentIdleMinute: F[IdleMinute] = idleMinuteRepository.get

  /**
   * @return リポジトリの値を1増加させる作用
   */
  def addOneMinute: F[Unit] = idleMinuteRepository.update(_.increment)

  /**
   * @return リポジトリの値を初期値に戻す作用
   */
  def reset: F[Unit] = idleMinuteRepository.set(IdleMinute.initial)

}
