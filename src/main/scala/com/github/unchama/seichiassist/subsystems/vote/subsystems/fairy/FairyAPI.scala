package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service.FairySpeechService

import java.util.UUID

trait FairyWriteAPI[F[_], Player] {

  /**
   * 妖精にあげるりんごの開放状態を変更する
   *  ※妖精にあげるりんごの数を変更する
   */
  def updateAppleOpenState(uuid: UUID, appleOpenState: AppleOpenState): F[Unit]

  /**
   * 妖精を召喚するためのコストを変更します。
   */
  def updateFairySummonCost(uuid: UUID, fairySummonCost: FairySummonCost): F[Unit]

  /**
   * fairyPlaySoundRepositoryの音を鳴らすかどうかの設定を切り替える
   */
  def fairyPlaySoundToggle(uuid: UUID): F[Unit]

  /**
   * 妖精を使っているかどうかを切り替える
   */
  def updateFairyUsingState(player: Player, fairyUsingState: FairyUsingState): F[Unit]

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

  def apply[F[_], Player](implicit ev: FairyWriteAPI[F, Player]): FairyWriteAPI[F, Player] = ev

}

trait FairyReadAPI[F[_], G[_], Player] {

  /**
   * 妖精にあげるりんごの開放状態を取得する
   */
  def appleOpenState(uuid: UUID): F[AppleOpenState]

  /**
   * 妖精を召喚するためのコストを取得する
   */
  def fairySummonCost(player: Player): F[FairySummonCost]

  /**
   * `FairyLoreTable`からLoreを取得する
   */
  def getFairyLore(uuid: UUID): F[FairyLore]

  /**
   * fairyPlaySoundRepositoryから音を鳴らすかどうかを取得する
   */
  def fairyPlaySound(uuid: UUID): F[FairyPlaySound]

  /**
   * 妖精を使っているかを取得する
   */
  def fairyUsingState(player: Player): F[FairyUsingState]

  /**
   * 妖精が回復するマナの量を取得する
   */
  def fairyRecoveryMana(uuid: UUID): F[FairyRecoveryMana]

  /**
   * 妖精が食べたりんごの量を取得する
   */
  def appleAteByFairy(uuid: UUID): F[AppleAmount]

  /**
   * 妖精の音を鳴らすかどうか保持するようのリポジトリ
   * ※永続化は必要ない
   */
  protected[this] val fairyPlaySoundRepository: KeyedDataRepository[
    UUID,
    Ref[F, FairyPlaySound]
  ]

  val fairySpeechServiceRepository: PlayerDataRepository[FairySpeechService[G]]

  /**
   * 妖精が有効な時間を返す
   */
  def fairyEndTime(player: Player): F[Option[FairyEndTime]]

  /**
   * 自分の妖精に食べさせたりんごの量の順位を返す
   */
  def appleAteByFairyMyRanking(player: Player): F[AppleAteByFairyRank]

  /**
   * 妖精に食べさせたりんごの量の順位上位4件を返す
   */
  def appleAteByFairyRankingTopFour(player: Player): F[AppleAteByFairyRankTopFour]

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

trait FairyAPI[F[_], G[_], Player]
    extends FairyReadAPI[F, G, Player]
    with FairyWriteAPI[F, Player]
