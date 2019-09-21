package com.github.unchama.seichiassist.data.player.settings

/**
 * 全体メッセージとそれに伴う効果音の抑制をするためのプレーヤー向け設定項目の状態を表すEnum.
 */
object BroadcastMutingSettings {

  case object ReceiveMessageAndSound extends BroadcastMutingSettings(ReceiveMessageOnly, false, false)

  case object ReceiveMessageOnly extends BroadcastMutingSettings(MuteMessageAndSound, false, true)

  case object MuteMessageAndSound extends BroadcastMutingSettings(ReceiveMessageAndSound, true, true)

  // TODO: ここパターンマッチっぽくない
  def fromBooleanSettings(displayMessages: Boolean, playSounds: Boolean): BroadcastMutingSettings = {
    case _ if displayMessages && playSounds => ReceiveMessageAndSound
    case _ if displayMessages => ReceiveMessageOnly
    case _ => MuteMessageAndSound
  }

}

sealed abstract class BroadcastMutingSettings(val next: BroadcastMutingSettings,
                                              val shouldMuteMessages: Boolean,
                                              val shouldMuteSounds: Boolean)