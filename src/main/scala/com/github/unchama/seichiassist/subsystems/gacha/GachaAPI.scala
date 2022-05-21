package com.github.unchama.seichiassist.subsystems.gacha

import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize

trait GachaMotionAPI[F[_], Player] {

  /**
   * playerがガチャをamount回引く作用
   */
  def pull(player: Player, amount: Int): F[Unit]

}

object GachaMotionAPI {

  def apply[F[_], Player](implicit ev: GachaMotionAPI[F, Player]): GachaMotionAPI[F, Player] =
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

trait GachaAPI[F[_], Player]
    extends GachaReadAPI[F]
    with GachaWriteAPI[F]
    with GachaMotionAPI[F, Player]
