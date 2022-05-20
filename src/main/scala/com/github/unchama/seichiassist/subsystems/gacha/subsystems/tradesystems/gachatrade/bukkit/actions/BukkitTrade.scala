package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gachatrade.bukkit.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gachatrade.application.actions.Trade
import org.bukkit.inventory.ItemStack

object BukkitTrade {

  import cats.implicits._

  def apply[F[_]: Sync](name: String)(
    implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): Trade[F, ItemStack] = (contents: List[ItemStack]) =>
    for {
      gachaList <- gachaPrizesDataOperations.gachaPrizesList
    } yield {
      // GTアイテムを除去し、今回の対象であるあたりまでを含めたリスト
      val targetsList =
        gachaList.filterNot(_.probability.value < 0.001).filter(_.probability.value < 0.1)

      // 大当たりのアイテム
      val bigList = targetsList.filter(_.probability.value < 0.01)

      // あたりのアイテム
      val regularList = targetsList.diff(bigList)

      // 交換可能な大当たりのアイテム
      val trueBigItems =
        contents.filter(targetItem => bigList.exists(_.createNewItem(Some(name)) == targetItem))

      // 交換可能なあたりのアイテム
      val trueRegularItems = contents.filter(targetItem =>
        regularList.exists(_.createNewItem(Some(name)) == targetItem)
      )

      // 交換不可能なアイテム達
      val falseItems = contents.diff(trueBigItems :: trueRegularItems)

      (
        trueBigItems.map(_.getAmount).sum * 12,
        trueRegularItems.map(_.getAmount).sum * 3,
        falseItems
      )
    }

}
