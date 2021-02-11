package com.github.unchama.seichiassist.subsystems.fastdiggingeffect

import cats.data.Kleisli
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{FastDiggingEffect, FastDiggingEffectList}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState

import scala.concurrent.duration.FiniteDuration

trait FastDiggingEffectWriteApi[F[_], Player] {

  /**
   * 採掘速度上昇効果を一つ付与する作用。
   */
  def addEffect(effect: FastDiggingEffect, duration: FiniteDuration): Kleisli[F, Player, Unit]

}

trait FastDiggingEffectReadApi[F[_], Player] {

  /**
   * プレーヤーに付与される採掘速度上昇効果の最新の値を保持するデータリポジトリ。
   */
  val currentEffect: KeyedDataRepository[Player, ReadOnlyRef[F, FastDiggingEffectList]]

  /**
   * 参加しているプレーヤーの [[FastDiggingEffectList]] を1秒ごとに流すストリーム
   */
  val effectClock: fs2.Stream[F, (Player, FastDiggingEffectList)]

}

trait FastDiggingEffectApi[F[_], Player]
  extends FastDiggingEffectReadApi[F, Player]
    with FastDiggingEffectWriteApi[F, Player]

trait FastDiggingSettingsWriteApi[F[_], Player] {

  /**
   * 採掘速度上昇抑制の設定をトグルする作用。
   */
  val toggleEffectSuppression: Kleisli[F, Player, Unit]

}

trait FastDiggingSettingsReadApi[F[_], Player] {

  /**
   * プレーヤーの採掘速度上昇抑制の設定を保持するデータレポジトリ。
   */
  val currentSuppressionSettings: KeyedDataRepository[Player, ReadOnlyRef[F, FastDiggingEffectSuppressionState]]

}

trait FastDiggingSettingsApi[F[_], Player]
  extends FastDiggingSettingsReadApi[F, Player]
    with FastDiggingSettingsWriteApi[F, Player]
