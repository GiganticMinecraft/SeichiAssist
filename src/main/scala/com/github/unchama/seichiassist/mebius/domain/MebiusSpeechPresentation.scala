package com.github.unchama.seichiassist.mebius.domain

import cats.kernel.Semigroup

/**
 * Mebiusの発話を `Effect` として提供するオブジェクトのtrait。
 *
 * NOTE:
 * Effectには Player => IO[Unit] のような型が入り、
 * Effectに Player => IO[Unit] のような型を入れて提供されることを想定している。
 *
 * IO[Unit]のような型を入れさせて、このtraitの実装がPlayerのインスタンスを知るような設計が適切ではあるのだが、
 * 現状 `PlayerDataOnMemoryRepository` は、プレーヤーに対して関連付けられる値に
 * プレーヤーがPreLoginしてからQuitするまでのライフサイクルを想定しているため、
 * 初期化処理が難しい(UUIDからプレーヤーを引いてメッセージ送信と音再生をしても良いが、適切でなさそう)。
 * TODO: Login(highest priority)からQuitまで値を持ち続けるレポジトリの実装を用意し、それにこのtraitを持たせ、設計を変更する。
 */
trait MebiusSpeechPresentation[Effect] {

  val Effect: Semigroup[Effect]

  def sendMessage(property: MebiusProperty, message: String): Effect

  def playSpeechSound(strength: MebiusSpeechStrength): Effect

  final def speak(speakingMebiusProperty: MebiusProperty, speech: MebiusSpeech): Effect =
    Effect.combine(
      sendMessage(speakingMebiusProperty, speech.content),
      playSpeechSound(speech.strength)
    )

}
