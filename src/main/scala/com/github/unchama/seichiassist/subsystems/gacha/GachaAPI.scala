package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gacha.domain.{GachaPrize, GachaPrizeId}

private trait GachaPrizesRepository[G[_]] {

  val gachaPrizes: Ref[G, GachaPrize]

}

object GachaPrizesRepository {

  def apply[F[_]](implicit ev: GachaPrizesRepository[F]): GachaPrizesRepository[F] = ev

}

trait GachaReadAPI[F[_]] {

  def list: F[Vector[GachaPrize]]

  def getGachaPrizesRepository: Ref[F, GachaPrize]

}

object GachaReadAPI {

  def apply[F[_]](implicit ev: GachaReadAPI[F]): GachaReadAPI[F] = ev

}

trait GachaWriteAPI[F[_]] {

  def upsert(gachaPrize: GachaPrize): F[Unit]

  def remove(id: GachaPrizeId): F[Boolean]

  def setGachaPrizesRepository(): Ref[F, GachaPrize]

}

object GachaWriteAPI {

  def apply[F[_]](implicit ev: GachaWriteAPI[F]): GachaWriteAPI[F] = ev

}

trait GachaAPI[F[_]] extends GachaReadAPI[F] with GachaWriteAPI[F]
