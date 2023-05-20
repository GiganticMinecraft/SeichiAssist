package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.domain

import java.util.UUID

trait FairySpeechPersistence[F[_]] {

  /**
   * @return 妖精が喋るときに音を再生するかを`playOnSpeech`に変更する作用
   */
  def setPlaySoundOnSpeech(player: UUID, playOnSpeech: Boolean): F[Unit]

  /**
   * @return 妖精が喋ったときに音を再生するか取得する作用
   */
  def playSoundOnFairySpeech(player: UUID): F[Boolean]

}
