package com.github.unchama.seichiassist.subsystems.gacha

import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize

trait GachaReadAPI[F[_], Player] {

  def pull(player: Player, amount: Int): F[Unit]

  /**
   * ガチャの景品リストを返す
   */
  def list: F[Vector[GachaPrize]]

}

object GachaReadAPI {

  def apply[F[_], Player](implicit ev: GachaReadAPI[F, Player]): GachaReadAPI[F, Player] = ev

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

trait GachaAPI[F[_], Player] extends GachaReadAPI[F, Player] with GachaWriteAPI[F]
