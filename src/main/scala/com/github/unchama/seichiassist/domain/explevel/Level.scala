package com.github.unchama.seichiassist.domain.explevel

import simulacrum.typeclass

/**
 * レベルを表す型の型クラス
 */
@typeclass trait Level[T] {
  /**
   * レベルの生の値を `T` に包む。
   *
   * @param rawLevel 正のレベル値
   */
  def wrapPositive(rawLevel: Int): T
}
