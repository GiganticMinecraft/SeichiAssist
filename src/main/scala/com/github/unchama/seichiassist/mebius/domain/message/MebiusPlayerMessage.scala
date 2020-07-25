package com.github.unchama.seichiassist.mebius.domain.message

/**
 * Mebiusからプレーヤーに向けるメッセージ。
 *
 * `rawMessage` はプレーヤー名のプレースホルダとして"[str1]"を含んでよい。
 */
case class MebiusPlayerMessage(rawMessage: String) {

  def interpolate(ownerNickname: String): String =
    rawMessage
      .replace("[str1]", ownerNickname)

}
