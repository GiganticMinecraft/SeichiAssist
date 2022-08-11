package com.github.unchama.seichiassist.subsystems.gacha

import cats.Functor
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.{GachaPrize, GachaPrizeId}

trait GachaLotteryAPI[F[_], ItemStack] {

  /**
   * ガチャ景品を抽選してその結果を返す作用
   */
  def runLottery(amount: Int): F[Vector[GachaPrize[ItemStack]]]

}

object GachaLotteryAPI {

  def apply[F[_], ItemStack](
    implicit ev: GachaLotteryAPI[F, ItemStack]
  ): GachaLotteryAPI[F, ItemStack] =
    ev

}

trait GachaReadAPI[F[_], ItemStack] {

  import cats.implicits._

  protected implicit val _FFunctor: Functor[F]

  /**
   * ガチャの景品リストを返す
   */
  def list: F[Vector[GachaPrize[ItemStack]]]

  /**
   * [[GachaPrizeId]]に対応する[[GachaPrize]]を取得する
   */
  final def gachaPrize(gachaPrizeId: GachaPrizeId): F[Option[GachaPrize[ItemStack]]] = for {
    prizes <- list
  } yield prizes.find(_.id == gachaPrizeId)

  /**
   * 指定された[[GachaPrizeId]]の[[GachaPrize]]が存在するか確認する
   */
  final def existsGachaPrize(gachaPrizeId: GachaPrizeId): F[Boolean] = for {
    prizes <- list
  } yield prizes.exists(_.id == gachaPrizeId)

  val grantGachaPrize: GrantGachaPrize[F, ItemStack]

}

object GachaReadAPI {

  def apply[F[_], ItemStack](
    implicit ev: GachaReadAPI[F, ItemStack]
  ): GachaReadAPI[F, ItemStack] = ev

}

trait GachaWriteAPI[F[_], ItemStack] {

  /**
   * ガチャの景品リストを何らかの方法でロードする
   */
  def load: F[Unit]

  /**
   * ガチャの景品リストを、与えたGachaPrizesListに置き換えを行う
   */
  def replace(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit]

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
  def addGachaPrize(gachaPrize: GachaPrizeId => GachaPrize[ItemStack]): F[Unit]

}

object GachaWriteAPI {

  def apply[F[_], ItemStack](
    implicit ev: GachaWriteAPI[F, ItemStack]
  ): GachaWriteAPI[F, ItemStack] = ev

}

trait GachaAPI[F[_], ItemStack]
    extends GachaReadAPI[F, ItemStack]
    with GachaWriteAPI[F, ItemStack]
    with GachaLotteryAPI[F, ItemStack]
