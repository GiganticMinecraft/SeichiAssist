package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyMessage

trait FairySpeak[F[_], Player] {

  def speak(player: Player, fairyMessage: FairyMessage)(implicit fairyAPI: FairyAPI[F]): F[Unit]

  def speakRandomly(player: Player)(implicit fairyAPI: FairyAPI[F]): F[Unit]

}

object FairySpeak {

  def apply[F[_], Player](implicit ev: FairySpeak[F, Player]): FairySpeak[F, Player] =
    ev

}
