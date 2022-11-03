package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.Monad
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventName
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.{
  GachaPrize,
  GachaPrizeId
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  StaticGachaPrizeFactory
}

trait GachaEventReadAPI[F[_]] {

  import cats.implicits._

  protected implicit val F: Monad[F]

  /**
   * @return 現在作成されているガチャイベントの一覧を取得する
   */
  def createdGachaEvents: F[Vector[GachaEvent]]

  /**
   * @return 指定された[[GachaEventName]]のイベントが存在すれば返す。
   */
  final def findGachaEvent(gachaEventName: GachaEventName): F[Option[GachaEvent]] = for {
    gachaEvents <- createdGachaEvents
  } yield gachaEvents.find(_.eventName == gachaEventName)

  /**
   * @return 指定された名前のガチャイベントが存在するか確認する
   */
  final def isExistsGachaEvent(gachaEventName: GachaEventName): F[Boolean] = for {
    foundGachaEvent <- findGachaEvent(gachaEventName)
  } yield foundGachaEvent.nonEmpty

}

object GachaEventReadAPI {

  def apply[F[_]](implicit ev: GachaEventReadAPI[F]): GachaEventReadAPI[F] = ev

}

trait GachaEventWriteAPI[F[_]] {

  /**
   * @return ガチャイベントを作成する作用
   *         ガチャイベントを作成すると、常時排出アイテムが自動コピーされます。
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
   * @return ガチャの景品リストをすべて取得する
   */
  def list: F[Vector[GachaPrize[ItemStack]]]

  /**
   * @return [[GachaPrizeId]]に対応する[[GachaPrize]]
   */
  final def fetch(gachaPrizeId: GachaPrizeId): F[Option[GachaPrize[ItemStack]]] = for {
    prizes <- list
  } yield prizes.find(_.id == gachaPrizeId)

  /**
   * 指定された[[GachaPrizeId]]に対応する[[GachaPrize]]が存在するか確認する
   */
  final def existsGachaPrize(gachaPrizeId: GachaPrizeId): F[Boolean] =
    F.map(fetch(gachaPrizeId))(_.nonEmpty)

  /**
   * @return [[StaticGachaPrizeFactory]]を返す
   */
  def staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack]

  /**
   * @return [[ItemStack]]から[[GachaPrize]]を取得する
   */
  def findByItemStack(itemStack: ItemStack): F[Option[GachaPrize[ItemStack]]]

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
   * `gachaEventName`がNoneの場合は常時排出アイテムとして扱う。
   */
  def addGachaPrize(gachaPrize: GachaPrizeByGachaPrizeId): F[Unit]

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
