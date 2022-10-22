package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._

import java.util.UUID

trait FairyWriteAPI[F[_], G[_], Player] {

  /**
   * プレイヤーデータを作成する
   */
  def createPlayerData(uuid: UUID): F[Unit]

  /**
   * 妖精にあげるりんごの開放状態を変更する
   */
  def updateAppleOpenState(uuid: UUID, appleOpenState: FairyAppleConsumeStrategy): F[Unit]

  /**
   * 妖精を召喚するためのコストを変更します。
   */
  def updateFairySummonCost(uuid: UUID, fairySummonCost: FairySummonCost): F[Unit]

  /**
   * 妖精を使っているかどうかを切り替える
   */
  def updateIsFairyUsing(player: Player, isFairyUsing: Boolean): F[Unit]

  /**
   * 妖精が回復するマナの量を変更する
   */
  def updateFairyRecoveryManaAmount(uuid: UUID, fairyRecoveryMana: FairyRecoveryMana): F[Unit]

  /**
   * 妖精の効果が終了する時間を変更する
   */
  def updateFairyEndTime(player: Player, fairyEndTime: FairyEndTime): F[Unit]

  /**
   * 妖精が食べたりんごの数を増加させる
   */
  def increaseAppleAteByFairy(uuid: UUID, appleAmount: AppleAmount): F[Unit]

}

object FairyWriteAPI {

  def apply[F[_], G[_], Player](
    implicit ev: FairyWriteAPI[F, G, Player]
  ): FairyWriteAPI[F, G, Player] = ev

}

trait FairyReadAPI[F[_], G[_], Player] {

  /**
   * 妖精にあげるりんごの開放状態を取得する
   */
  def appleOpenState(uuid: UUID): F[FairyAppleConsumeStrategy]

  /**
   * 妖精を召喚するためのコストを取得する
   */
  def fairySummonCost(player: Player): F[FairySummonCost]

  /**
   * `FairyLoreTable`からLoreを取得する
   */
  def getFairyLore(uuid: UUID): F[FairyLore]

  /**
   * 妖精を使っているかを取得する
   */
  def isFairyUsing(player: Player): F[Boolean]

  /**
   * 妖精が回復するマナの量を取得する
   */
  def fairyRecoveryMana(uuid: UUID): F[FairyRecoveryMana]

  /**
   * 妖精が食べたりんごの量を取得する
   */
  def appleAteByFairy(uuid: UUID): F[Option[AppleAmount]]

  /**
   * 妖精が有効な時間を返す
   */
  def fairyEndTime(player: Player): F[Option[FairyEndTime]]

  /**
   * 自分の妖精に食べさせたりんごの量の順位を返す
   */
  def appleAteByFairyMyRanking(player: Player): F[Option[AppleAteByFairyRank]]

  /**
   * 妖精に食べさせたりんごの量の順位上`number`件を返す
   */
  def appleAteByFairyRanking(number: Int): F[Vector[Option[AppleAteByFairyRank]]]

  /**
   * 妖精が食べたりんごの合計数を返す
   */
  def allEatenAppleAmount: F[AppleAmount]

}

object FairyReadAPI {

  def apply[F[_], G[_], Player](
    implicit ev: FairyReadAPI[F, G, Player]
  ): FairyReadAPI[F, G, Player] = ev

}

trait FairySpeechAPI[F[_], Player] {

  /**
   * 妖精が喋るときに音をだすかをトグルする
   */
  def toggleFairySpeechSound(uuid: UUID): F[Unit]

  /**
   * 妖精が喋ったときに音を再生するか取得する
   */
  def fairySpeechSound(uuid: UUID): F[Boolean]

  /**
   * @return 妖精がいつ帰るのかを[[Player]]に送信する作用
   */
  def speechEndTime(player: Player): F[Unit]

}

object FairySpeechAPI {

  def apply[F[_], Player](implicit ev: FairySpeechAPI[F, Player]): FairySpeechAPI[F, Player] =
    ev

}

trait FairyAPI[F[_], G[_], Player]
    extends FairyReadAPI[F, G, Player]
    with FairyWriteAPI[F, G, Player]
    with FairySpeechAPI[F, Player]
