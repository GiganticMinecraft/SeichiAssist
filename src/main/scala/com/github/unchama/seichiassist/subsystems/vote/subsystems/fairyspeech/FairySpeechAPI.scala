package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage

import java.util.UUID
import scala.collection.immutable.Seq

trait FairySpeechAPI[F[_], Player] {

  /**
   * @return `player`に対して`messages`を妖精が喋る作用
   */
  def speech(player: Player, messages: Seq[FairyMessage]): F[Unit]

  /**
   * @return `player`に対して妖精が喋った際に音を再生するかをトグルする作用
   */
  def togglePlaySoundOnSpeech: Kleisli[F, Player, Unit]

  /**
   * @return `player`に対して妖精が喋った際に音を再生するかどうかを取得する作用
   */
  def playSoundOnSpeech(player: UUID): F[Boolean]

}

object FairySpeechAPI {

  def apply[F[_], Player](implicit ev: FairySpeechAPI[F, Player]): FairySpeechAPI[F, Player] =
    ev

}
