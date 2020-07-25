package com.github.unchama.seichiassist.mebius.domain.message

import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty

trait PropertyModificationMessages {

  /**
   * Mebiusのレベルアップに関するメッセージをプロパティの組から計算する
   */
  def onLevelUp(oldProperty: MebiusProperty, newProperty: MebiusProperty): List[String]

}
