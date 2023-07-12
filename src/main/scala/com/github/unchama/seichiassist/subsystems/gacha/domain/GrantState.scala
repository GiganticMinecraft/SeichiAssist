package com.github.unchama.seichiassist.subsystems.gacha.domain

sealed trait GrantState

/**
 * ガチャ景品をどこに付与したのかを表すenum
 * MineStackに付与したか、プレイヤー(インベントリ)に直接付与したかを表す。
 */
object GrantState {

  /**
   * ガチャ景品をMineStackに付与した
   */
  case object GrantedMineStack extends GrantState

  /**
   * ガチャ景品をインベントリに直接付与、または地面にドロップした
   */
  case object GrantedInventoryOrDrop extends GrantState

}
