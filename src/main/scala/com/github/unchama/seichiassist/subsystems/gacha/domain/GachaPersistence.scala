package com.github.unchama.seichiassist.subsystems.gacha.domain

trait GachaPersistence[F[_]] {

  /**
   * ガチャアイテムとして登録されているアイテムリストをGachaPrizeのVectorとして返します。
   */
  def list: F[Vector[GachaPrize]]

  /**
   * ガチャアイテムを追加します。
   */
  def upsert(id: GachaPrizeId, gachaPrize: GachaPrize): F[Unit]

  /**
   * ガチャアイテムを削除します。
   */
  def remove(id: GachaPrizeId): F[Boolean]

  /**
   * 指定IDのガチャアイテムを置き換えます。
   */
  final def replace(id: GachaPrizeId, gachaPrize: GachaPrize): F[Unit] = {
    upsert(id, gachaPrize)
  }

}
