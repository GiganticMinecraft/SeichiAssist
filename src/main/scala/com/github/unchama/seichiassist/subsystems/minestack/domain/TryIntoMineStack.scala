package com.github.unchama.seichiassist.subsystems.minestack.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ListExtra
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectList,
  MineStackObjectWithAmount
}

class TryIntoMineStack[F[_]: Sync, Player, ItemStack](
  implicit mineStackObjectList: MineStackObjectList[F, ItemStack, Player],
  mineStackObjectRepository: KeyedDataRepository[Player, Ref[F, List[
    MineStackObjectWithAmount[ItemStack]
  ]]]
) {

  import cats.implicits._

  /**
   * [[Player]]のMineStackリポジトリに[[ItemStack]]を格納することを試みます。
   * @return 格納ができたかどうかを取得する作用
   */
  def apply(player: Player, itemStack: ItemStack, amount: Int): F[Boolean] = for {
    foundMineStackObject <- mineStackObjectList.findByItemStack(itemStack, player)
    _ <- foundMineStackObject.traverse(addStackedAmountOf(player, _, amount))
  } yield foundMineStackObject.nonEmpty

  /**
   * @return MineStackに指定した[[MineStackObject]]の量を加算する作用
   */
  private def addStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Int
  ): F[Unit] = mineStackObjectRepository(player).update { mineStackObjects =>
    ListExtra.rePrependOrAdd(mineStackObjects)(
      _.mineStackObject == mineStackObject,
      _.fold(MineStackObjectWithAmount(mineStackObject, amount))(value =>
        value.increase(amount)
      )
    )
  }

}
