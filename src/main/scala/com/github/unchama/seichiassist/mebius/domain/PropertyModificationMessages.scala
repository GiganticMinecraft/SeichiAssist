package com.github.unchama.seichiassist.mebius.domain

trait PropertyModificationMessages {

  /**
   * Mebiusのレベルアップに関するメッセージをプロパティの組から計算する
   */
  def onLevelUp(oldProperty: MebiusProperty, newProperty: MebiusProperty): List[String]

}
