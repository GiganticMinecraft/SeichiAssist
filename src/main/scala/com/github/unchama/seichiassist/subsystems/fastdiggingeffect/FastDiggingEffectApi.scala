package com.github.unchama.seichiassist.subsystems.fastdiggingeffect

import cats.data.Kleisli
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{FastDiggingEffect, FastDiggingEffectList}

import scala.concurrent.duration.FiniteDuration

trait FastDiggingEffectWriteApi[F[_], Player] {

  def addEffect(effect: FastDiggingEffect, duration: FiniteDuration): Kleisli[F, Player, Unit]

}

trait FastDiggingEffectReadApi[F[_], Player] {

  /**
   * プレーヤーに付与される採掘速度上昇効果の最新の値を保持するデータリポジトリ。
   */
  val currentEffect: KeyedDataRepository[Player, FastDiggingEffectList]

}

trait FastDiggingEffectApi[F[_], Player]
  extends FastDiggingEffectReadApi[F, Player]
    with FastDiggingEffectWriteApi[F, Player]

