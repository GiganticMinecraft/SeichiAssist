package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage

import scala.collection.immutable.Seq

trait FairySpeechAPI[F[_], Player] {

  /**
   * @return `player`に対して`messages`を妖精が喋る作用
   */
  def speech(player: Player, messages: Seq[FairyMessage]): F[Unit]

}

object FairySpeechAPI {

  def apply[F[_], Player](implicit ev: FairySpeechAPI[F, Player]): FairySpeechAPI[F, Player] =
    ev

}
