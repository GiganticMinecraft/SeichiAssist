package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  AppleAmount,
  AppleConsumeAmountRank,
  FairyAppleConsumeStrategy,
  FairyEndTime,
  FairyRecoveryMana,
  FairySummonCost
}

import java.util.UUID

trait FairyPersistence[F[_]] {

  /**
   * プレイヤーデータを作成する。このメソッドの実装が返す作用は、冪等でなければならない。
   * @return プレイヤーデータを作成するべき等な作用
   */
  def initializePlayerData(player: UUID): F[Unit]

  /**
   * @return 妖精に開放するりんごの状態を変更する作用
   */
  def updateAppleConsumeStrategy(player: UUID, openState: FairyAppleConsumeStrategy): F[Unit]

  /**
   *  @return 妖精に開放するりんごの状態を取得する作用
   */
  def appleConsumeStrategy(player: UUID): F[FairyAppleConsumeStrategy]

  /**
   * @return 妖精が召喚するためのコストを変更する作用
   */
  def updateFairySummonCost(player: UUID, fairySummonCost: FairySummonCost): F[Unit]

  /**
   * @return 妖精を召喚するためのコストを取得する作用
   */
  def fairySummonCost(player: UUID): F[FairySummonCost]

  /**
   * @return 妖精が召喚されているかを更新する作用
   */
  def updateIsFairyUsing(player: UUID, isFairyUsing: Boolean): F[Unit]

  /**
   * @return 妖精が召喚されているかを取得する作用
   */
  def isFairyUsing(player: UUID): F[Boolean]

  /**
   * @return 妖精が回復するマナの量を変更する作用
   */
  def updateFairyRecoveryMana(player: UUID, fairyRecoveryMana: FairyRecoveryMana): F[Unit]

  /**
   * @return 妖精が回復するマナの量を取得する作用
   */
  def fairyRecoveryMana(player: UUID): F[FairyRecoveryMana]

  /**
   * @return 妖精の効果が終了する時刻を変更する作用
   */
  def updateFairyEndTime(player: UUID, fairyEndTime: FairyEndTime): F[Unit]

  /**
   * @return 妖精の効果が終了する時刻を取得する作用
   */
  def fairyEndTime(player: UUID): F[Option[FairyEndTime]]

  /**
   * @return 妖精が指定プレイヤーから食べたりんごの量を増加させる作用
   */
  def increaseConsumedAppleAmountByFairy(player: UUID, appleAmount: AppleAmount): F[Unit]

  /**
   * @return 妖精が食べたりんごの量を取得する作用
   */
  def consumedAppleAmountByFairy(player: UUID): F[Option[AppleAmount]]

  /**
   * @return 自分の妖精に食べさせたりんごの量の順位を返す作用
   */
  def rankByConsumedAppleAmountByFairy(player: UUID): F[Option[AppleConsumeAmountRank]]

  /**
   * 妖精に食べさせたりんごの量が多いプレイヤーを上位とし、そのランキングの上から指定した件数を返す
   * ただし、要素数が`top`件あることは保証しない。
   *
   * @param top 最上位から何番目まで取得するか件数を指定する。0以下であってはならない。
   * @return 指定した件数が要素数となり、その並びが消費量の降順になっているような順序つきのコレクションを返す作用。
   */
  def fetchMostConsumedApplePlayersByFairy(top: Int): F[Vector[AppleConsumeAmountRank]]

  /**
   * @return 妖精が今まで食べたりんごの合計数を返す作用
   */
  def totalConsumedAppleAmount: F[AppleAmount]

  /**
   * @return 妖精が喋るときに音をだすかをトグルする作用
   */
  def setPlaySoundOnSpeech(player: UUID, playOnSpeech: Boolean): F[Unit]

  /**
   * @return 妖精が喋ったときに音を再生するか取得する作用
   */
  def playSoundOnFairySpeech(player: UUID): F[Boolean]

}
