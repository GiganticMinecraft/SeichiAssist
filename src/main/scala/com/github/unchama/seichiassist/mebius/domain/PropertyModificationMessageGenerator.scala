package com.github.unchama.seichiassist.mebius.domain

trait PropertyModificationMessageGenerator[Message] {

  /**
   * Mebiusのレベルアップに関するメッセージをプロパティの組から計算する
   */
  def messagesOnLevelUp(oldProperty: MebiusProperty, newProperty: MebiusProperty): List[Message]

}
