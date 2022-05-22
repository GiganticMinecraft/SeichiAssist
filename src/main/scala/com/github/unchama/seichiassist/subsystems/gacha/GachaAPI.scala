package com.github.unchama.seichiassist.subsystems.gacha

import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize

trait GachaLotteryAPI[F[_]] {

  /**
   * ガチャを抽選してその結果を返す作用
   */
  def lottery(amount: Int): F[Vector[GachaPrize]]

}

object GachaLotteryAPI {

  def apply[F[_]](implicit ev: GachaLotteryAPI[F]): GachaLotteryAPI[F] =
    ev

}

trait GachaReadAPI[F[_]] {

  /**
   * ガチャの景品リストを返す
   */
  def list: F[Vector[GachaPrize]]

}

object GachaReadAPI {

  def apply[F[_]](implicit ev: GachaReadAPI[F]): GachaReadAPI[F] = ev

}

trait GachaWriteAPI[F[_]] {

  /**
   * ガチャの景品リストを、与えたGachaPrizesListに置き換えを行う
   */
  def replace(gachaPrizesList: Vector[GachaPrize]): F[Unit]

}

object GachaWriteAPI {

  def apply[F[_]](implicit ev: GachaWriteAPI[F]): GachaWriteAPI[F] = ev

}

trait GachaAPI[F[_]] extends GachaReadAPI[F] with GachaWriteAPI[F] with GachaLotteryAPI[F]
