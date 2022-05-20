package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize

final class GachaPrizesDataOperations[F[_]: Sync] {

  import cats.implicits._

  private val gachaPrizes: Ref[F, Vector[GachaPrize]] =
    Ref.unsafe[F, Vector[GachaPrize]](Vector.empty)

  /**
   * 与えられた`gachaPersistence`からガチャ景品データを読み込む
   */
  def loadGachaPrizes(gachaPersistence: GachaPersistence[F]): F[Unit] = for {
    prizes <- gachaPersistence.list
    _ <- gachaPrizes.set(prizes)
  } yield ()

  /**
   * 指定された`GachaPrize`が存在するかどうか
   */
  def existsGachaPrize(gachaPrizeId: GachaPrizeId): F[Boolean] = for {
    gachaPrizes <- gachaPrizes.get
  } yield gachaPrizes.exists(_.id == gachaPrizeId)

  /**
   * `GachaPrize`を追加する。
   * `GachaPrizeId`を与えなかった場合は最大`GachaPrizeId`の次の値が指定されます
   */
  def addGachaPrize(gachaPrize: GachaPrizeId => GachaPrize): F[Unit] = for {
    prizes <- gachaPrizes.get
    newList = prizes ++ Vector(gachaPrize(GachaPrizeId(prizes.map(_.id.id).max + 1)))
    _ <- gachaPrizes.set(newList)
  } yield ()

  /**
   * `GachaPrizeId`から`GachaPrize`を取得する
   */
  def gachaPrize(gachaPrizeId: GachaPrizeId): F[Option[GachaPrize]] = for {
    prizes <- gachaPrizes.get
  } yield prizes.find(_.id == gachaPrizeId)

  /**
   * `gachaPrizeId`を利用して`GachaPrize`を削除する
   */
  def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Unit] = for {
    prizes <- gachaPrizes.get
    targetPrize = prizes.filter(_.id == gachaPrizeId)
    _ <- gachaPrizes.set(prizes.diff(targetPrize))
  } yield ()

  /**
   * すべてのガチャ景品データを返す
   */
  def gachaPrizesList: F[Vector[GachaPrize]] = gachaPrizes.get

  /**
   * すべてのガチャ景品データを削除する
   */
  def clear(): F[Unit] = for {
    _ <- gachaPrizes.set(Vector.empty)
  } yield ()

}
