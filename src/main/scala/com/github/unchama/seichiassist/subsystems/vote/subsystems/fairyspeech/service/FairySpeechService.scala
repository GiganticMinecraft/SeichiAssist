package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.service

import cats.effect.Sync
import cats.implicits._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.domain.FairySpeechGateway

class FairySpeechService[F[_]: Sync](gateway: FairySpeechGateway[F]) {

  def makeSpeech(fairyMessage: Seq[FairyMessage], fairyPlaySound: Boolean): F[Unit] =
    gateway.sendMessage(fairyMessage) >> Sync[F].whenA(fairyPlaySound)(gateway.playSpeechSound)

}
