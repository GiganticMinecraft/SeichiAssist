package com.github.unchama.seichiassist.subsystems.gacha

import com.github.unchama.seichiassist.subsystems.gacha.domain.{GachaPrize, GachaPrizeId}

trait GachaReadAPI[F[_]] {

  def list: F[Vector[GachaPrize]]

}

object GachaReadAPI {

  def apply[F[_]](implicit ev: GachaReadAPI[F]): GachaReadAPI[F] = ev

}

trait GachaWriteAPI[F[_]] {

  def upsert(gachaPrize: GachaPrize): F[Unit]

  def remove(id: GachaPrizeId): F[Boolean]

}

object GachaWriteAPI {

  def apply[F[_]](implicit ev: GachaWriteAPI[F]): GachaWriteAPI[F] = ev

}

trait GachaAPI[F[_]] extends GachaReadAPI[F] with GachaWriteAPI[F]
