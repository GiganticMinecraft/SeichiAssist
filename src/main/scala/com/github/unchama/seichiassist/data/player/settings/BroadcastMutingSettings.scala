package com.github.unchama.seichiassist.data.player.settings

/**
 * 全体メッセージとそれに伴う効果音の抑制をするためのプレーヤー向け設定項目の状態を表すEnum.
 */
object BroadcastMutingSettings {

  def fromBooleanSettings(displayMessages: Boolean, playSounds: Boolean): BroadcastMutingSettings = {
    (displayMessages, playSounds) match {
      case (true, true) => ReceiveMessageAndSound
      case (true, _) => ReceiveMessageOnly
      case _ => MuteMessageAndSound
    }
  }

  case object ReceiveMessageAndSound extends BroadcastMutingSettings(ReceiveMessageOnly, false, false)

  case object ReceiveMessageOnly extends BroadcastMutingSettings(MuteMessageAndSound, false, true)

  case object MuteMessageAndSound extends BroadcastMutingSettings(ReceiveMessageAndSound, true, true)

}

sealed abstract class BroadcastMutingSettings(nextThunk: => BroadcastMutingSettings,
                                              val shouldMuteMessages: Boolean,
                                              val shouldMuteSounds: Boolean) {
  // case objectの循環参照のため
  lazy val next: BroadcastMutingSettings = nextThunk
}