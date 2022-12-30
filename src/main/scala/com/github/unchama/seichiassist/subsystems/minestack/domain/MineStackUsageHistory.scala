package com.github.unchama.seichiassist.subsystems.minestack.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject

class MineStackUsageHistory[F[_]: Sync, ItemStack] {

  // NOTE: 履歴を保存できる最大数
  private val maxListSize = 27

  import cats.implicits._

  private val _usageHistory: F[Ref[F, Vector[MineStackObject[ItemStack]]]] = for {
    reference <- Ref.of[F, Vector[MineStackObject[ItemStack]]](Vector.empty)
  } yield reference

  /**
   * 指定されたアイテムを履歴に追加します。
   * ただし、履歴の数が保存できる最大エントリ数を超えていた場合、古い方から削除されます。
   *
   * @return 履歴を更新する作用
   */
  def addHistory(mineStackObject: MineStackObject[ItemStack]): F[Unit] = for {
    reference <- _usageHistory
    _ <- reference.update { oldHistories =>
      oldHistories.filterNot(_ == mineStackObject) :+ mineStackObject
    }
    oldHistories <- reference.get
    _ <- reference.update(_.drop(1)).whenA(oldHistories.size > maxListSize)
  } yield ()

  /**
   * @return MineStackの使用履歴を返す作用
   */
  def usageHistory: F[Vector[MineStackObject[ItemStack]]] = for {
    reference <- _usageHistory
    histories <- reference.get
  } yield histories

}
