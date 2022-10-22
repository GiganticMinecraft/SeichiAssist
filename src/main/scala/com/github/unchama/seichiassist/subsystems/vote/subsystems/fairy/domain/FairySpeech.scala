package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

trait FairySpeech[F[_], Player] {

  /**
   * @return 妖精が召喚された際のメッセージを[[Player]]に送信する作用
   */
  def summonSpeech(player: Player): F[Unit]

  /**
   * @return 妖精のメッセージをランダムに[[Player]]へ送信する作用
   */
  def speechRandomly(player: Player): F[Unit]

  /**
   * @return 妖精がいつ帰るのかを[[Player]]へ送信する作用
   */
  def speechEndTime(player: Player): F[Unit]

  /**
   * @return 妖精召喚中に[[Player]]が再ログインした際のメッセージを[[Player]]に送信する作用
   */
  def welcomeBack(player: Player): F[Unit]

  /**
   * @return 妖精が帰るときのメッセージを[[Player]]に送信する作用
   */
  def bye(player: Player): F[Unit]

}
