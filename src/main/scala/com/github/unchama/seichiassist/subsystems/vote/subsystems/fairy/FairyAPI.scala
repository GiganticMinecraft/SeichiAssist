package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpawnRequestErrorOrSpawn
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._

import java.util.UUID

trait FairyWriteAPI[F[_], G[_], Player] {

  /**
   * 妖精にあげるりんごの開放状態を変更する
   */
  def updateAppleOpenState(uuid: UUID, appleConsumeStrategy: FairyAppleConsumeStrategy): F[Unit]

  /**
   * 妖精を召喚するためのコストを変更します。
   */
  def updateFairySummonCost(uuid: UUID, fairySummonCost: FairySummonCost): F[Unit]

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

trait FairySummonAPI[F[_], Player] {

  /**
   * 妖精の召喚をリクエストする
   * 召喚に失敗した場合はエラーを返す
   * 成功した場合は召喚する作用を返す
   */
  def fairySummonRequest(player: Player): F[FairySpawnRequestErrorOrSpawn[F]]
}

object FairySummonAPI {

  def apply[F[_], Player](implicit ev: FairySummonAPI[F, Player]): FairySummonAPI[F, Player] =
    ev

}

trait FairyAPI[F[_], G[_], Player]
    extends FairyReadAPI[F, G, Player]
    with FairyWriteAPI[F, G, Player]
    with FairySpeechAPI[F, Player]
    with FairySummonAPI[F, Player]
