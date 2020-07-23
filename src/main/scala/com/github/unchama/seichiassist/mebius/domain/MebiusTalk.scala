package com.github.unchama.seichiassist.mebius.domain

/**
 * Mebiusとプレーヤー間のやり取り。
 *
 * アイテムに書き込まれたり、レベルアップ時にMebiusとのやり取りが
 * チャットに表示されたりすることを想定している。
 */
case class MebiusTalk(mebiusMessage: String, playerMessage: String)
