package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyManaRecoveryState

trait FairySpeech[F[_], Player] {

  /**
   * @return 妖精が召喚された際のメッセージを[[Player]]に送信する作用
   */
  def summonSpeech(player: Player): F[Unit]

  /**
   * @return [[FairyManaRecoveryState]]に応じた
   *         妖精のメッセージをランダムに[[Player]]へ送信する作用
   */
  def speechRandomly(player: Player, fairyManaRecoveryState: FairyManaRecoveryState): F[Unit]

  /**
   * @return 妖精がいつ帰るのかを[[Player]]へ知らせる作用
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
