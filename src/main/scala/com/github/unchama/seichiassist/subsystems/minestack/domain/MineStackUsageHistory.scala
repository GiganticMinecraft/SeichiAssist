package com.github.unchama.seichiassist.subsystems.minestack.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject

class MineStackUsageHistory[F[_]: Sync, ItemStack] {

  private val maxListSize = 27

  import cats.implicits._

  private val _usageHistory: F[Ref[F, Vector[MineStackObject[ItemStack]]]] = for {
    reference <- Ref.of(Vector.empty)
  } yield reference

  /**
   * 履歴に追加します。ただし、データの保存可能な最大値を超えていた場合、先頭から削除されます。
   */
  def addHistory(mineStackObject: MineStackObject[ItemStack]): F[Unit] = for {
    reference <- _usageHistory
    _ <- reference.update { oldHistories =>
      oldHistories.filterNot(_ == mineStackObject) :+ mineStackObject
    }
    oldHistories <- reference.get
    _ <- reference.update(_.drop(1)).whenA(oldHistories.size > maxListSize)
  } yield ()

  def usageHistory: F[Vector[MineStackObject[ItemStack]]] = for {
    reference <- _usageHistory
    histories <- reference.get
  } yield histories

}
