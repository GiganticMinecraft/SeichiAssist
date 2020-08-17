package com.github.unchama.seichiassist.domain.explevel

import simulacrum.typeclass

/**
 * レベルを表す型の型クラス
 */
@typeclass trait Level[T] {
  def wrap(rawLevel: Int): T
}
