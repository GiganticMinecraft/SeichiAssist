package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  AppleOpenState,
  FairyLore,
  FairyPlaySound,
  FairySummonCost
}

import java.util.UUID

trait FairyWriteAPI[F[_]] {

  /**
   * 妖精にあげるりんごの開放状態を変更する
   *  ※妖精にあげるりんごの数を変更する
   */
  def updateAppleOpenState(uuid: UUID, appleOpenState: AppleOpenState): F[Unit]

  /**
   * 妖精を召喚するコストを変更します。
   */
  def updateFairySummonCost(uuid: UUID, fairySummonCost: FairySummonCost): F[Unit]

  /**
   * fairyPlaySoundRepositoryから音を鳴らすかどうかを取得する
   */
  def fairyPlaySound(uuid: UUID): F[FairyPlaySound]

  /**
   * fairyPlaySoundRepositoryの音を鳴らすかどうかの設定を切り替える
   */
  def fairyPlaySoundToggle(uuid: UUID): F[Unit]

}

object FairyWriteAPI {

  def apply[F[_]](implicit ev: FairyWriteAPI[F]): FairyWriteAPI[F] = ev

}

trait FairyReadAPI[F[_]] {

  /**
   * 妖精にあげるりんごの開放状態を取得する
   */
  def appleOpenState(uuid: UUID): F[AppleOpenState]

  /**
   * 妖精を召喚するコストを取得します
   */
  def fairySummonCost(uuid: UUID): F[FairySummonCost]

  /**
   * `FairyLoreTable`からLoreを取得する
   */
  def getFairyLore(uuid: UUID): F[FairyLore]

  /**
   * 妖精の音を鳴らすかどうか保持するようのリポジトリ
   * ※永続化は必要ない
   */
  protected[this] val fairyPlaySoundRepository: KeyedDataRepository[
    UUID,
    Ref[F, FairyPlaySound]
  ]

}

object FairyReadAPI {

  def apply[F[_]](implicit ev: FairyReadAPI[F]): FairyReadAPI[F] = ev

}

trait FairyAPI[F[_]] extends FairyReadAPI[F] with FairyWriteAPI[F]
