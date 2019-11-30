package com.github.unchama.seichiassist.data.player.settings

/**
 * 全体メッセージとそれに伴う効果音の抑制をするためのプレーヤー向け設定項目の状態を表すEnum.
 */
object BroadcastMutingSettings {

  def fromBooleanSettings(shouldMuteMessage: Boolean, shouldMuteSounds: Boolean): BroadcastMutingSettings = {
    (shouldMuteMessage, shouldMuteSounds) match {
      case (true, true) => MuteMessageAndSound
      case (_, true) => ReceiveMessageOnly
      case _ => ReceiveMessageAndSound
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
