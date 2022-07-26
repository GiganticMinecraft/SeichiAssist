package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain._

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
  def updateFairyUsingState(uuid: UUID, fairyUsingState: FairyUsingState): F[Unit]

  /**
   * 妖精が回復するマナの量を変更する
   */
  def updateFairyRecoveryManaAmount(uuid: UUID, fairyRecoveryMana: FairyRecoveryMana): F[Unit]

  /**
   * 妖精が有効な時間を変更する
   */
  def updateFairyValidTimes(player: Player, fairyValidTimes: Option[FairyValidTimes]): F[Unit]

}

object FairyWriteAPI {

  def apply[F[_], Player](implicit ev: FairyWriteAPI[F, Player]): FairyWriteAPI[F, Player] = ev

}

trait FairyReadAPI[F[_], Player] {

  /**
   * 妖精にあげるりんごの開放状態を取得する
   */
  def appleOpenState(uuid: UUID): F[AppleOpenState]

  /**
   * 妖精を召喚するためのコストを取得する
   */
  def fairySummonCost(uuid: UUID): F[FairySummonCost]

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
  def fairyUsingState(uuid: UUID): F[FairyUsingState]

  /**
   * 妖精が回復するマナの量を取得する
   */
  def fairyRecoveryMana(uuid: UUID): F[FairyRecoveryMana]

  /**
   * 妖精の音を鳴らすかどうか保持するようのリポジトリ
   * ※永続化は必要ない
   */
  protected[this] val fairyPlaySoundRepository: KeyedDataRepository[
    UUID,
    Ref[F, FairyPlaySound]
  ]

  /**
   * 妖精の有効な時間を保存するリポジトリ
   * ※永続化は必要ない
   */
  protected[this] val fairyValidTimeRepository: KeyedDataRepository[Player, Ref[F, Option[
    FairyValidTimes
  ]]]

  /**
   * 妖精が有効な時間を返す
   */
  def fairyValidTimes(player: Player): F[Option[FairyValidTimes]]

}

object FairyReadAPI {

  def apply[F[_], Player](implicit ev: FairyReadAPI[F, Player]): FairyReadAPI[F, Player] = ev

}

trait FairyAPI[F[_], Player] extends FairyReadAPI[F, Player] with FairyWriteAPI[F, Player]
