package com.github.unchama.seichiassist.mebius.domain.speech

import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty

/**
 * Mebiusからの発話を仲介するオブジェクトのtrait。
 */
trait MebiusSpeechGateway[F[_]] {

  def sendMessage(property: MebiusProperty, message: String): F[Unit]

  def playSpeechSound(strength: MebiusSpeechStrength): F[Unit]

}
