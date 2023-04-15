package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage

trait FairySpeechGateway[F[_]] {

  /**
   * @return 妖精からメッセージを送信する作用
   */
  def sendMessage(fairyMessages: Seq[FairyMessage]): F[Unit]

  /**
   * @return 妖精がメッセージを送信した時の音を再生する作用
   */
  def playSpeechSound: F[Unit]

}
