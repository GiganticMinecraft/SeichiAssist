package com.github.unchama.seichiassist.subsystems.gacha.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize

trait GachaPrizeListPersistence[F[_]] {

  /**
   * ガチャアイテムとして登録されているアイテムリストをGachaPrizeのVectorとして返します。
   */
  def list: F[Vector[GachaPrize]]

  /**
   * ガチャリストを更新します。
   */
  def set(gachaPrizesList: Vector[GachaPrize]): F[Unit]

}
