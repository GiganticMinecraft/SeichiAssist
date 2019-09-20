package com.github.unchama.seichiassist.data.player.settings

/**
 * 全体メッセージとそれに伴う効果音の抑制をするためのプレーヤー向け設定項目の状態を表すEnum.
 */
enum class BroadcastMutingSettings {
  RECEIVE_MESSAGE_AND_SOUND, RECEIVE_MESSAGE_ONLY, MUTE_MESSAGE_AND_SOUND;

  def nextSettingsOption(): BroadcastMutingSettings = when (this) {
    RECEIVE_MESSAGE_AND_SOUND -> RECEIVE_MESSAGE_ONLY
    RECEIVE_MESSAGE_ONLY -> MUTE_MESSAGE_AND_SOUND
    MUTE_MESSAGE_AND_SOUND -> RECEIVE_MESSAGE_AND_SOUND
  }

  def shouldMuteMessages(): Boolean = when (this) {
    RECEIVE_MESSAGE_AND_SOUND, RECEIVE_MESSAGE_ONLY -> true
    MUTE_MESSAGE_AND_SOUND -> false
  }

  def shouldMuteSounds(): Boolean = when (this) {
    RECEIVE_MESSAGE_AND_SOUND -> true
    RECEIVE_MESSAGE_ONLY, MUTE_MESSAGE_AND_SOUND -> false
  }
}

object BroadcastMutingSettings {
  def fromBooleanSettings(displayMessages: Boolean, playSounds: Boolean): BroadcastMutingSettings =
    if (displayMessages) {
      if (playSounds) RECEIVE_MESSAGE_AND_SOUND else RECEIVE_MESSAGE_ONLY
    } else {
      MUTE_MESSAGE_AND_SOUND
    }
}