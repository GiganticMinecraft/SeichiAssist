package com.github.unchama.seichiassist.subsystems.minestack.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject

class MineStackUsageHistory[F[_]: Sync, ItemStack] {

  // NOTE: 履歴を保存できる最大数
  private val maxListSize = 27

  import cats.implicits._

  private val _usageHistory: Ref[F, Vector[MineStackObject[ItemStack]]] =
    Ref.unsafe[F, Vector[MineStackObject[ItemStack]]](Vector.empty)

  /**
   * 指定されたアイテムを履歴に追加します。
   * ただし、履歴の数が保存できる最大エントリ数を超えていた場合、古い方から削除されます。
   *
   * @return 履歴を更新する作用
   */
  def addHistory(mineStackObject: MineStackObject[ItemStack]): F[Unit] = for {
    _ <- _usageHistory.update { oldHistories =>
      oldHistories.filterNot(_ == mineStackObject) :+ mineStackObject
    }
    oldHistories <- _usageHistory.get
    _ <- _usageHistory.update(_.drop(1)).whenA(oldHistories.size > maxListSize)
  } yield ()

  /**
   * @return MineStackの使用履歴を返す作用
   */
  def usageHistory: F[Vector[MineStackObject[ItemStack]]] =
    _usageHistory.get

}
