package com.github.unchama.seichiassist.subsystems.mebius.domain.message

/**
 * Mebiusからプレーヤーに向けるメッセージ。
 *
 * `rawMessage` はプレーヤー名のプレースホルダとして"[str1]"を含んでよい。
 */
case class MebiusPlayerMessage(rawMessage: String) {

  /**
   * `ownerNickname` を `rawMessage` に補完してメッセージを生成する。
   */
  def interpolate(ownerNickname: String): String =
    rawMessage
      .replace("[str1]", ownerNickname)

}
