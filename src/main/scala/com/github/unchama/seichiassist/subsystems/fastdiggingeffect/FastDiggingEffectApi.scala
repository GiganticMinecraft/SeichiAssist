package com.github.unchama.seichiassist.subsystems.fastdiggingeffect

import cats.data.Kleisli
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{
  FastDiggingEffect,
  FastDiggingEffectList
}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats.FastDiggingEffectStatsSettings

import scala.concurrent.duration.FiniteDuration

trait FastDiggingEffectWriteApi[F[_], Player] {

  /**
   * 採掘速度上昇効果を一つ付与する作用。
   */
  def addEffect(effect: FastDiggingEffect, duration: FiniteDuration): Kleisli[F, Player, Unit]

  /**
   * サーバー内に居る全プレーヤーに採掘速度上昇効果を一括で付与する作用。
   */
  def addEffectToAllPlayers(effect: FastDiggingEffect, duration: FiniteDuration): F[Unit]

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
   * 採掘速度上昇抑制の設定をトグルする作用。 作用は結果値として変更後の設定を返す。
   */
  val toggleEffectSuppression: Kleisli[F, Player, FastDiggingEffectSuppressionState]

  /**
   * 採掘速度上昇効果の統計を受け取るかどうかの設定をトグルする作用。 作用は結果値として変更後の設定を返す。
   */
  val toggleStatsSettings: Kleisli[F, Player, FastDiggingEffectStatsSettings]

}

trait FastDiggingSettingsReadApi[F[_], Player] {

  /**
   * 採掘速度上昇抑制の設定をプレーヤーごとに保持するデータレポジトリ。
   */
  val currentSuppressionSettings: KeyedDataRepository[
    Player,
    ReadOnlyRef[F, FastDiggingEffectSuppressionState]
  ]

  /**
   * 採掘速度上昇効果の統計を受け取るかどうかの設定をプレーヤーごとに保持するデータレポジトリ。
   */
  val currentStatsSettings: KeyedDataRepository[
    Player,
    ReadOnlyRef[F, FastDiggingEffectStatsSettings]
  ]

}

trait FastDiggingSettingsApi[F[_], Player]
    extends FastDiggingSettingsReadApi[F, Player]
    with FastDiggingSettingsWriteApi[F, Player]
