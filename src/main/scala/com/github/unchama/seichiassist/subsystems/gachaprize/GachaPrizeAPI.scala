package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.Monad
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventName
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrizeTableEntry,
  GachaPrizeId,
  StaticGachaPrizeFactory
}

trait GachaEventReadAPI[F[_]] {

  import cats.implicits._

  protected implicit val F: Monad[F]

  /**
   * @return 現在作成されているガチャイベントの一覧を取得する作用
   */
  def createdGachaEvents: F[Vector[GachaEvent]]

  /**
   * @return 指定された`gachaEventName`のイベントが存在すれば返す作用
   */
  private def findGachaEvent(gachaEventName: GachaEventName): F[Option[GachaEvent]] = for {
    gachaEvents <- createdGachaEvents
  } yield gachaEvents.find(_.eventName == gachaEventName)

  /**
   * 指定された[[GachaEventName]]のイベントが存在するか調べる
   * @return 存在すれば`true`、存在しないなら`false`を返す作用
   */
  final def existsGachaEvent(gachaEventName: GachaEventName): F[Boolean] = for {
    foundGachaEvent <- findGachaEvent(gachaEventName)
  } yield foundGachaEvent.nonEmpty

}

object GachaEventReadAPI {

  def apply[F[_]](implicit ev: GachaEventReadAPI[F]): GachaEventReadAPI[F] = ev

}

trait GachaEventWriteAPI[F[_]] {

  /**
   * ガチャイベントを作成します。作成時、常時排出されるアイテムが自動的にコピーされます。
   * @return ガチャイベントを作成する作用
   */
  def createGachaEvent(gachaEvent: GachaEvent): F[Unit]

  /**
   * @return ガチャイベントを削除する作用
   */
  def deleteGachaEvent(gachaEventName: GachaEventName): F[Unit]

}

object GachaEventWriteAPI {

  def apply[F[_]](implicit ev: GachaEventWriteAPI[F]): GachaEventWriteAPI[F] = ev

}

trait GachaPrizeReadAPI[F[_], ItemStack] {

  import cats.implicits._

  protected implicit val F: Monad[F]

  /**
   * @return 現在排出対象となっているガチャ景品リストを取得する作用
   */
  def listOfNow: F[Vector[GachaPrizeTableEntry[ItemStack]]]

  /**
   * @return すべてのガチャ景品を取得する作用
   */
  def allGachaPrizeList: F[Vector[GachaPrizeTableEntry[ItemStack]]]

  /**
   * @return イベント開催中ではないときに排出されるガチャ景品を取得する作用
   */
  final def gachaPrizesWhenGachaEventsIsNotHolding: F[Vector[GachaPrizeTableEntry[ItemStack]]] =
    for {
      gachaPrizes <- allGachaPrizeList
    } yield gachaPrizes.filter(_.nonGachaEventItem)

  /**
   * @return `gachaPrizeId`に対応する[[GachaPrizeTableEntry]]を返す作用
   */
  final def fetch(gachaPrizeId: GachaPrizeId): F[Option[GachaPrizeTableEntry[ItemStack]]] =
    for {
      prizes <- allGachaPrizeList
    } yield prizes.find(_.id == gachaPrizeId)

  /**
   * @return [[StaticGachaPrizeFactory]]を返す
   */
  def staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack]

  /**
   * @return [[CanBeSignedAsGachaPrize]]を返す
   */
  def canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]

}

object GachaPrizeReadAPI {

  def apply[F[_], ItemStack](
    implicit ev: GachaPrizeReadAPI[F, ItemStack]
  ): GachaPrizeReadAPI[F, ItemStack] = ev

}

trait GachaPrizeWriteAPI[F[_], ItemStack] {

  /**
   * @return ガチャ景品リストから指定された`gachaPrizeId`に紐づく[[GachaPrizeTableEntry]]を削除し、その結果を返す作用
   */
  def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Boolean]

  final type GachaPrizeByGachaPrizeId = GachaPrizeId => GachaPrizeTableEntry[ItemStack]

  /**
   * @return ガチャ景品リストに`gachaPrizeByGachaPrizeId`から取り出される[[GachaPrizeTableEntry]]を追加する作用
   */
  def addGachaPrize(gachaPrizeByGachaPrizeId: GachaPrizeByGachaPrizeId): F[Unit]

  /**
   * @return `gachaPrize`と同じガチャIDのガチャ景品が存在すれば`gachaPrize`に更新し、
   *         なければ`gachaPrize`を追加します
   */
  def upsertGachaPrize(gachaPrize: GachaPrizeTableEntry[ItemStack]): F[Unit]

}

object GachaPrizeWriteAPI {

  def apply[F[_], ItemStack](
    implicit ev: GachaPrizeWriteAPI[F, ItemStack]
  ): GachaPrizeWriteAPI[F, ItemStack] = ev

}

trait GachaPrizeAPI[F[_], ItemStack, Player]
    extends GachaPrizeReadAPI[F, ItemStack]
    with GachaPrizeWriteAPI[F, ItemStack]
    with GachaEventReadAPI[F]
    with GachaEventWriteAPI[F]
