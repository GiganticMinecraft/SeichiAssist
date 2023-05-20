package com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel

/**
 * レベルの進行度を表す値。
 *
 * @param progress
 *   現在のレベルから次のレベルまでへの進行度。0以上1未満の[[Double]]値である。
 * @param expAmountToNextLevel
 *   次のレベルに到達するのに必要な整地経験値量
 */
case class BuildLevelProgress private (progress: Double, expAmountToNextLevel: BuildExpAmount) {
  require(0.0 <= progress && progress < 1.0, "レベル進捗は[0.0, 1.0)の要素である必要があります")
}

object BuildLevelProgress {

  import com.github.unchama.generic.algebra.typeclasses.OrderedMonus._

  /**
   * 必須経験値量と達成済み経験値量から [[BuildLevelProgress]] を得る。 必要経験値量は達成済み経験値量未満である必要がある。
   */
  def fromRequiredAndAchievedPair(
    required: BuildExpAmount,
    achieved: BuildExpAmount
  ): BuildLevelProgress = {
    BuildLevelProgress(
      achieved.amount.toDouble / required.amount.toDouble,
      required |-| achieved
    )
  }
}
