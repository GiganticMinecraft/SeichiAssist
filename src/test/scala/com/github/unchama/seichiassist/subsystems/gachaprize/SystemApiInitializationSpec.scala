package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.github.unchama.generic.Cloneable
import com.github.unchama.generic.effect.concurrent.CachedRef
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventName,
  GachaEventPersistence
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrizeId,
  GachaPrizeListPersistence,
  GachaPrizeTableEntry,
  StaticGachaPrizeFactory
}
import com.github.unchama.seichiassist.subsystems.gachaprize.usecase.GachaPrizeUseCase
import org.bukkit.inventory.ItemStack
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

/**
 * gachaprizeサブシステムのAPI構築の回帰テスト。
 *
 * Scala 3移行時、匿名クラス内の `override protected implicit val F = implicitly` が
 * 定義中の `this.F` 自身を解決してしまい、未初期化の null が格納される問題が発生した。
 * このテストは、API構築直後にMonadインスタンスが正しく初期化されており、
 * `F` を利用するメソッドが実行できることを固定する。
 */
class SystemApiInitializationSpec extends AnyWordSpec {

  private val emptyPrizesRef: CachedRef[IO, Vector[GachaPrizeTableEntry[ItemStack]]] =
    new CachedRef[IO, Vector[GachaPrizeTableEntry[ItemStack]]] {
      override val initial: Ref[IO, Vector[GachaPrizeTableEntry[ItemStack]]] =
        Ref.unsafe(Vector.empty)
      override val updateInterval: IO[FiniteDuration] = IO.pure(1.minute)
    }

  private implicit val prizeListPersistence: GachaPrizeListPersistence[IO, ItemStack] =
    new GachaPrizeListPersistence[IO, ItemStack] {
      override def list: IO[Vector[GachaPrizeTableEntry[ItemStack]]] = IO.pure(Vector.empty)
      override def upsertGachaPrize(gachaPrize: GachaPrizeTableEntry[ItemStack]): IO[Unit] =
        IO.unit
      override def removeGachaPrize(gachaPrizeId: GachaPrizeId): IO[Unit] = IO.unit
      override def duplicateDefaultGachaPrizes(gachaEvent: GachaEvent): IO[Unit] = IO.unit
    }

  private implicit val eventPersistence: GachaEventPersistence[IO] =
    new GachaEventPersistence[IO] {
      override def createGachaEvent(gachaEvent: GachaEvent): IO[Unit] = IO.unit
      override def deleteGachaEvent(eventName: GachaEventName): IO[Unit] = IO.unit
      override def gachaEvents: IO[Vector[GachaEvent]] = IO.pure(Vector.empty)
    }

  // 本テストで検証する経路（gachaPrizesWhenGachaEventsIsNotHolding）では
  // ItemStackの値そのものは一切参照されないため、nullで代用している
  private implicit val staticFactory: StaticGachaPrizeFactory[ItemStack] =
    new StaticGachaPrizeFactory[ItemStack] {
      override val gachaRingo: ItemStack = null
      override val expBottle: ItemStack = null
      override val mineHeadItem: ItemStack = null
    }

  private implicit val cloneable: Cloneable[ItemStack] = (x: ItemStack) => x

  private implicit val cachedRef: CachedRef[IO, Vector[GachaPrizeTableEntry[ItemStack]]] =
    emptyPrizesRef

  private val signable: CanBeSignedAsGachaPrize[ItemStack] =
    new CanBeSignedAsGachaPrize[ItemStack] {
      override def signWith(ownerName: String): GachaPrizeTableEntry[ItemStack] => ItemStack =
        _ => throw new NotImplementedError("本テストの経路では使用しない")
    }

  "System.createApi" should {
    "構築直後にMonadインスタンスが初期化されており、Fを利用するメソッドが実行できる" in {
      val useCase = new GachaPrizeUseCase[IO, ItemStack]

      val api = System.createApi[IO](
        emptyPrizesRef,
        useCase,
        prizeListPersistence,
        eventPersistence,
        staticFactory,
        signable
      )

      // Fがnullの場合、gachaPrizesWhenGachaEventsIsNotHoldingはmapの適用時に
      // NullPointerExceptionを投げる
      assert(api.gachaPrizesWhenGachaEventsIsNotHolding.unsafeRunSync() == Vector.empty)
      assert(api.allGachaPrizeList.unsafeRunSync() == Vector.empty)
      assert(api.existsGachaPrize(GachaPrizeId(1)).unsafeRunSync() == false)
    }
  }
}
