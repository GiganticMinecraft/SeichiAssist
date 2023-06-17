package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpawnRequestErrorOrSpawn
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._

import java.util.UUID

trait FairyWriteAPI[F[_], G[_], Player] {

  /**
   * @return 妖精にあげるりんごの開放状態を変更する作用
   */
  def updateAppleOpenState(
    appleConsumeStrategy: FairyAppleConsumeStrategy
  ): Kleisli[F, Player, Unit]

  /**
   * @return 妖精を召喚するためのコストを変更する作用
   */
  def updateFairySummonCost(fairySummonCost: FairySummonCost): Kleisli[F, Player, Unit]

}

object FairyWriteAPI {

  def apply[F[_], G[_], Player](
    implicit ev: FairyWriteAPI[F, G, Player]
  ): FairyWriteAPI[F, G, Player] = ev

}

trait FairyReadAPI[F[_], G[_], Player] {

  /**
   * @return 妖精にあげるりんごの開放状態を取得する作用
   */
  def consumeStrategy(uuid: UUID): F[FairyAppleConsumeStrategy]

  /**
   * @return 妖精を召喚するためのコストを取得する作用
   */
  def fairySummonCost(player: Player): F[FairySummonCost]

  /**
   * @return 妖精を使っているかを取得する作用
   */
  def isFairyAppearing(player: Player): F[Boolean]

  /**
   * @return 自分の妖精に食べさせたりんごの量の順位を返す作用
   */
  def rankByMostConsumedApple(player: Player): F[Option[AppleConsumeAmountRank]]

  /**
   * @return 妖精に食べさせたりんごの量の上位`top`件を返す作用
   */
  def rankingByMostConsumedApple(top: Int): F[Vector[AppleConsumeAmountRank]]

  /**
   * @return 妖精が食べたりんごの合計数を返す作用
   */
  def totalConsumedApple: F[AppleAmount]

}

object FairyReadAPI {

  def apply[F[_], G[_], Player](
    implicit ev: FairyReadAPI[F, G, Player]
  ): FairyReadAPI[F, G, Player] = ev

}

trait FairySpeechAPI[F[_], Player] {

  /**
   * @return 妖精がいつ帰るのかを送信する作用
   */
  def sendDisappearTimeToChat: Kleisli[F, Player, Unit]

}

object FairySpeechAPI {

  def apply[F[_], Player](implicit ev: FairySpeechAPI[F, Player]): FairySpeechAPI[F, Player] =
    ev

}

trait FairySummonAPI[F[_], Player] {

  /**
   * 召喚に失敗した場合はエラーを返す
   * 成功した場合は召喚する作用を返す
   */
  def fairySummonRequest: Kleisli[F, Player, FairySpawnRequestErrorOrSpawn[F]]
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
