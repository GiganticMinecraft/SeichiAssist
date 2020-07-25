package com.github.unchama.seichiassist.mebius.domain.message

/**
 * Mebiusの、戦闘中のプレーヤーに向けるメッセージ。
 *
 * `rawMessage` はプレーヤー名のプレースホルダとして"[str1]"を、敵の名前のプレースホルダとして"[str2]"を含んでよい。
 */
case class MebiusCombatMessage(rawMessage: String) {

  def interpolate(ownerNickname: String, enemyName: String): String =
    rawMessage
      .replace("[str1]", ownerNickname)
      .replace("[str2]", enemyName)

}
