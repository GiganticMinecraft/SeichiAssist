package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class BreakSkillTargetConfigRepository[F[_]: Sync] {

  import cats.implicits._

  private val breakSkillTargetConfigs: Ref[F, Set[BreakSkillTargetConfig]] =
    Ref.unsafe(Set.empty)

  /**
   * @return `configKey`の破壊フラグをトグルする作用
   */
  def toggleBreakSkillTargetConfig(configKey: BreakSkillTargetConfigKey): F[Unit] = for {
    _ <- breakSkillTargetConfigs.getAndUpdate { breakFlags =>
      val currentTargetBreakConfig =
        breakFlags.find(_.configKey == configKey).exists(_.includes)

      breakFlags.filterNot(_.configKey == configKey) + BreakSkillTargetConfig(
        configKey,
        includes = !currentTargetBreakConfig
      )
    }
  } yield ()

  /**
   * @return 現在の破壊フラグを取得する作用
   */
  def breakSkillTargetConfig(configKey: BreakSkillTargetConfigKey): F[Boolean] = {
    for {
      config <- breakSkillTargetConfigs.get
    } yield config.find(_.configKey == configKey).exists(_.includes)
  }

  /**
   * この関数は永続化層で利用されることを想定しています。
   * @return 現在の[[BreakSkillTargetConfig]]をすべて取得する作用
   */
  def getConfigAll: F[Set[BreakSkillTargetConfig]] = breakSkillTargetConfigs.get

  /**
   * この関数は永続化層で利用することを想定しています。
   * @return 現在の設定を`config`で上書きする作用
   */
  def setConfig(config: Set[BreakSkillTargetConfig]): F[Unit] =
    breakSkillTargetConfigs.set(config)

}
