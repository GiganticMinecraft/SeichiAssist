package com.github.unchama.seichiassist.mebius.domain.speech

import cats.effect.Sync
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty

/**
 * Mebiusからの発話を仲介するオブジェクトのクラス。
 */
abstract class MebiusSpeechGateway[F[_] : Sync] {

  def sendMessage(property: MebiusProperty, message: String): F[Unit]

  def playSpeechSound(strength: MebiusSpeechStrength): F[Unit]

}
