package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.Monad
import cats.effect.Async
import com.github.unchama.generic.effect.concurrent.CachedRef
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.minecraft.bukkit.algebra.BukkitItemStackSerializeAndDeserialize
import com.github.unchama.minecraft.bukkit.algebra.CloneableBukkitItemStack.instance
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.BukkitItemStackCanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitStaticGachaPrizeFactory
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventName,
  GachaEventPersistence
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain._
import com.github.unchama.seichiassist.subsystems.gachaprize.infrastructure.{
  JdbcGachaEventPersistence,
  JdbcGachaPrizeListPersistence
}
import com.github.unchama.seichiassist.subsystems.gachaprize.usecase.GachaPrizeUseCase
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import cats.effect.Ref

trait System[F[_]] extends Subsystem[F] {
  val api: GachaPrizeAPI[F, ItemStack, Player]
  val backgroundProcess: F[Nothing]
}

object System {

  import scala.concurrent.duration._
  import cats.implicits._

  def wired[F[_]: Async]: F[System[F]] = {
    implicit val _serializeAndDeserialize: SerializeAndDeserialize[Nothing, ItemStack] =
      BukkitItemStackSerializeAndDeserialize
    implicit val _gachaPersistence: GachaPrizeListPersistence[F, ItemStack] =
      new JdbcGachaPrizeListPersistence[F, ItemStack]
    implicit val _canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      BukkitItemStackCanBeSignedAsGachaPrize
    implicit val _staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
      BukkitStaticGachaPrizeFactory
    implicit val _gachaEventPersistence: GachaEventPersistence[F] =
      new JdbcGachaEventPersistence[F]

    for {
      gachaPrizeList <- _gachaPersistence.list
    } yield {
      implicit val gachaPrizesRef: CachedRef[F, Vector[GachaPrizeTableEntry[ItemStack]]] =
        new CachedRef[F, Vector[GachaPrizeTableEntry[ItemStack]]] {
          override val initial: Ref[F, Vector[GachaPrizeTableEntry[ItemStack]]] =
            Ref.unsafe(gachaPrizeList)

          override val updateInterval: F[FiniteDuration] = Async[F].pure(1.minutes)
        }

      val gachaPrizeUseCase: GachaPrizeUseCase[F, ItemStack] =
        new GachaPrizeUseCase[F, ItemStack]

      new System[F] {
        override val backgroundProcess: F[Nothing] =
          gachaPrizesRef.startUpdateRoutine(_gachaPersistence.list)

        override implicit val api: GachaPrizeAPI[F, ItemStack, Player] =
          createApi(
            gachaPrizesRef,
            gachaPrizeUseCase,
            _gachaPersistence,
            _gachaEventPersistence,
            _staticGachaPrizeFactory,
            _canBeSignedAsGachaPrize
          )
      }
    }
  }

  /**
   * [[GachaPrizeAPI]] の実装を構築する。
   *
   * 実装上の注意: 匿名クラス内の `override protected implicit val F` を `implicitly` や
   * `Monad[F]` で初期化してはならない。Scala 3では定義中の `this.F` 自身が暗黙引数として
   * 解決され、未初期化の null が格納される（コンパイラの "Infinite loop in function body"
   * 警告が出る）。このため、匿名クラスの外側で確定させた `monadF` を明示的に代入している。
   */
  private[gachaprize] def createApi[F[_]: Monad](
    gachaPrizesRef: CachedRef[F, Vector[GachaPrizeTableEntry[ItemStack]]],
    gachaPrizeUseCase: GachaPrizeUseCase[F, ItemStack],
    gachaPersistence: GachaPrizeListPersistence[F, ItemStack],
    gachaEventPersistence: GachaEventPersistence[F],
    staticFactory: StaticGachaPrizeFactory[ItemStack],
    signableAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]
  ): GachaPrizeAPI[F, ItemStack, Player] = {
    val monadF: Monad[F] = Monad[F]

    new GachaPrizeAPI[F, ItemStack, Player] {
      override protected implicit val F: Monad[F] = monadF

      override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Boolean] =
        gachaPrizeUseCase.removeByGachaPrizeId(gachaPrizeId)

      override def addGachaPrize(gachaPrizeByGachaPrizeId: GachaPrizeByGachaPrizeId): F[Unit] =
        gachaPrizeUseCase.addGachaPrize(gachaPrizeByGachaPrizeId)

      override def upsertGachaPrize(gachaPrize: GachaPrizeTableEntry[ItemStack]): F[Unit] =
        gachaPersistence.upsertGachaPrize(gachaPrize)

      override def listOfNow: F[Vector[GachaPrizeTableEntry[ItemStack]]] =
        gachaPrizeUseCase.listOfNow

      override def allGachaPrizeList: F[Vector[GachaPrizeTableEntry[ItemStack]]] =
        gachaPrizesRef.read

      override def staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
        staticFactory

      override def createdGachaEvents: F[Vector[GachaEvent]] =
        gachaEventPersistence.gachaEvents

      override def createGachaEvent(gachaEvent: GachaEvent): F[Unit] =
        gachaPrizeUseCase.createGachaEvent(gachaEvent)

      override def deleteGachaEvent(gachaEventName: GachaEventName): F[Unit] =
        gachaEventPersistence.deleteGachaEvent(gachaEventName)

      override def canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
        signableAsGachaPrize
    }
  }

}
