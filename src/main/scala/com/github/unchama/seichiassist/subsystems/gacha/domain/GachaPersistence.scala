package com.github.unchama.seichiassist.subsystems.gacha.domain

trait GachaPersistence[F[_]] {

  /**
   * ガチャアイテムとして登録されているアイテムリストをGachaPrizeのVectorとして返します。
   */
  def list: F[Vector[GachaPrize]]

  /**
   * ガチャアイテムを追加します。
   * idが同じだった場合は置き換えられます
   */
  def upsert(gachaPrize: GachaPrize): F[Unit]

  /**
   * ガチャアイテムを削除します。
   */
  def remove(id: GachaPrizeId): F[Boolean]

}
