package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizeId
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize

trait GachaLotteryAPI[F[_]] {

  /**
   * ガチャ景品を抽選してその結果を返す作用
   */
  def lottery(amount: Int): F[Vector[GachaPrize]]

}

object GachaLotteryAPI {

  def apply[F[_]](implicit ev: GachaLotteryAPI[F]): GachaLotteryAPI[F] =
    ev

}

trait GachaReadAPI[F[_]] {

  import cats.implicits._

  protected implicit val _FSync: Sync[F]

  /**
   * ガチャの景品リスト用のリポジトリ
   */
  protected val gachaPrizesListRepository: Ref[F, Vector[GachaPrize]] =
    Ref.unsafe[F, Vector[GachaPrize]](Vector.empty)

  /**
   * ガチャの景品リストを返す
   */
  final def list: F[Vector[GachaPrize]] = gachaPrizesListRepository.get

  /**
   * [[GachaPrizeId]]に対応する[[GachaPrize]]を取得する
   */
  final def gachaPrize(gachaPrizeId: GachaPrizeId): F[Option[GachaPrize]] = for {
    prizes <- list
  } yield prizes.find(_.id == gachaPrizeId)

  /**
   * 指定された[[GachaPrizeId]]の[[GachaPrize]]が存在するか確認する
   */
  final def existsGachaPrize(gachaPrizeId: GachaPrizeId): F[Boolean] = for {
    prizes <- list
  } yield prizes.exists(_.id == gachaPrizeId)

}

object GachaReadAPI {

  def apply[F[_]](implicit ev: GachaReadAPI[F]): GachaReadAPI[F] = ev

}

trait GachaWriteAPI[F[_]] {

  /**
   * ガチャの景品リストを何らかの方法でロードする
   */
  def load: F[Unit]

  /**
   * ガチャの景品リストを、与えたGachaPrizesListに置き換えを行う
   */
  def replace(gachaPrizesList: Vector[GachaPrize]): F[Unit]

  /**
   * ガチャ景品リストを空にする
   */
  final def clear: F[Unit] = replace(Vector.empty)

  /**
   * ガチャ景品リストから指定された[[GachaPrizeId]]の[[GachaPrize]]を削除する
   */
  def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Unit]

  /**
   * ガチャ景品リストにGachaPrizeを追加する
   */
  def addGachaPrize(gachaPrize: GachaPrizeId => GachaPrize): F[Unit]

}

object GachaWriteAPI {

  def apply[F[_]](implicit ev: GachaWriteAPI[F]): GachaWriteAPI[F] = ev

}

trait GachaAPI[F[_]] extends GachaReadAPI[F] with GachaWriteAPI[F] with GachaLotteryAPI[F]
