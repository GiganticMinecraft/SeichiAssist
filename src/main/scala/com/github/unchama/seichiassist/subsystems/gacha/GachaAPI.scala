package com.github.unchama.seichiassist.subsystems.gacha

import cats.Monad
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaevent.GachaEventName
import com.github.unchama.seichiassist.subsystems.gacha.domain.{GachaPrize, GachaPrizeId}

trait GachaEventAPI[F[_]] {

  /**
   * @return 指定された名前のガチャイベントが存在するか確認する
   */
  def isExistsGachaEvent(gachaEventName: GachaEventName): Boolean

}

trait GachaDrawAPI[F[_], Player] {

  /**
   * @return ガチャを実行する作用
   */
  def drawGacha(player: Player, draws: Int): F[Unit]

}

object GachaLotteryAPI {

  def apply[F[_], Player](implicit ev: GachaDrawAPI[F, Player]): GachaDrawAPI[F, Player] =
    ev

}

trait GachaReadAPI[F[_], ItemStack] {

  import cats.implicits._

  protected implicit val F: Monad[F]

  /**
   * @return ガチャの景品リストをすべて取得する
   */
  def list: F[Vector[GachaPrize[ItemStack]]]

  /**
   * @return 特定のガチャイベントに左右されないガチャ景品リストを取得する
   */
  def alwaysDischargeGachaPrizes: F[Vector[GachaPrize[ItemStack]]]

  /**
   * @return 指定されたイベント名で排出されるガチャ景品のみを取得する
   */
  def getOnlyGachaEventDischargeGachaPrizes(
    gachaEventName: GachaEventName
  ): F[Vector[GachaPrize[ItemStack]]]

  /**
   * @return イベントで排出されるガチャ景品と常に排出されるガチャ景品の合成リストを取得する
   */
  final def getGachaEventDischargeGachaPrizes(
    gachaEventName: GachaEventName
  ): F[Vector[GachaPrize[ItemStack]]] = for {
    alwaysGachaPrizes <- alwaysDischargeGachaPrizes
    onlyGachaEvent <- getOnlyGachaEventDischargeGachaPrizes(gachaEventName)
  } yield alwaysGachaPrizes ++ onlyGachaEvent

  /**
   * @return [[GachaPrizeId]]に対応する[[GachaPrize]]
   */
  final def fetch(gachaPrizeId: GachaPrizeId): F[Option[GachaPrize[ItemStack]]] = for {
    prizes <- list
  } yield prizes.find(_.id == gachaPrizeId)

  /**
   * 指定された[[GachaPrizeId]]に対応する[[GachaPrize]]が存在するか確認する
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

  final type GachaPrizeByGachaPrizeId = GachaPrizeId => GachaPrize[ItemStack]

  /**
   * ガチャ景品リストにGachaPrizeを追加する
   */
  def addGachaPrize(gachaPrize: GachaPrizeByGachaPrizeId): F[Unit]

}

object GachaWriteAPI {

  def apply[F[_], ItemStack](
    implicit ev: GachaWriteAPI[F, ItemStack]
  ): GachaWriteAPI[F, ItemStack] = ev

}

trait GachaAPI[F[_], ItemStack, Player]
    extends GachaReadAPI[F, ItemStack]
    with GachaWriteAPI[F, ItemStack]
    with GachaDrawAPI[F, Player]
