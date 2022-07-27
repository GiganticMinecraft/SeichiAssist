package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage

trait FairySpeak[F[_], Player] {

  /**
   * 単に妖精からのメッセージを発言する作用
   */
  def speak(player: Player, fairyMessage: FairyMessage)(
    implicit fairyAPI: FairyAPI[F, Player]
  ): F[Unit]

  /**
   * 妖精からのランダムなメッセージを発言する作用
   */
  def speakRandomly(player: Player)(implicit fairyAPI: FairyAPI[F, Player]): F[Unit]

  /**
   * 妖精からの召喚時メッセージを発言する作用
   */
  def speakStartMessage(player: Player)(implicit fairyAPI: FairyAPI[F, Player]): F[Unit]

}

object FairySpeak {

  def apply[F[_], Player](implicit ev: FairySpeak[F, Player]): FairySpeak[F, Player] =
    ev

}
