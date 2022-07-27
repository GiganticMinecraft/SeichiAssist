package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage

trait FairySpeechGateway[F[_]] {

  def sendMessage(fairyMessage: FairyMessage): F[Unit]

  def playSpeechSound: F[Unit]

}
