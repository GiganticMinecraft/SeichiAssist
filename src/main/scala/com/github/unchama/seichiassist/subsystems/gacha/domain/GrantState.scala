package com.github.unchama.seichiassist.subsystems.gacha.domain

sealed trait GrantState

object GrantState {

  /**
   * ガチャ景品をMineStackに付与した
   */
  case object GrantedMineStack extends GrantState

  /**
   * ガチャ景品をインベントリに付与した
   * ドロップしたことは考慮しない
   */
  case object GrantedInventory extends GrantState

}
