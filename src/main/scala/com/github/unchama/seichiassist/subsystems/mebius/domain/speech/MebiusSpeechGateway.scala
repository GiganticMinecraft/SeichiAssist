package com.github.unchama.seichiassist.subsystems.mebius.domain.speech

import com.github.unchama.seichiassist.subsystems.mebius.domain.property.MebiusProperty

/**
 * Mebiusからの発話を仲介するオブジェクトのtrait。
 */
trait MebiusSpeechGateway[F[_]] {

  def sendMessage(property: MebiusProperty, message: String): F[Unit]

  def playSpeechSound(strength: MebiusSpeechStrength): F[Unit]

}
