package com.github.unchama.seichiassist.data.playerdata.settings

/**
 * 全体メッセージとそれに伴う効果音の抑制をするためのプレーヤー向け設定項目の状態を表すEnum.
 */
enum class BroadcastMutingSettings {
  RECEIVE_MESSAGE_AND_SOUND, RECEIVE_MESSAGE_ONLY, MUTE_MESSAGE_AND_SOUND;

  fun nextSettingsOption(): BroadcastMutingSettings = when (this) {
    RECEIVE_MESSAGE_AND_SOUND -> RECEIVE_MESSAGE_ONLY
    RECEIVE_MESSAGE_ONLY -> MUTE_MESSAGE_AND_SOUND
    MUTE_MESSAGE_AND_SOUND -> RECEIVE_MESSAGE_AND_SOUND
  }

  fun shouldMuteMessages(): Boolean = when (this) {
    RECEIVE_MESSAGE_AND_SOUND, RECEIVE_MESSAGE_ONLY -> true
    MUTE_MESSAGE_AND_SOUND -> false
  }

  fun shouldMuteSounds(): Boolean = when (this) {
    RECEIVE_MESSAGE_AND_SOUND -> true
    RECEIVE_MESSAGE_ONLY, MUTE_MESSAGE_AND_SOUND -> false
  }

  companion object {
    fun fromBooleanSettings(displayMessages: Boolean, playSounds: Boolean): BroadcastMutingSettings =
        if (displayMessages) {
          if (playSounds) RECEIVE_MESSAGE_AND_SOUND else RECEIVE_MESSAGE_ONLY
        } else {
          MUTE_MESSAGE_AND_SOUND
        }
  }
}
