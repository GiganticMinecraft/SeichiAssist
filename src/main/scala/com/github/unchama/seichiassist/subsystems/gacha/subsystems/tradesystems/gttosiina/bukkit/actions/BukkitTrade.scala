package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gttosiina.bukkit.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gttosiina.application.actions.Trade
import org.bukkit.inventory.ItemStack

object BukkitTrade {

  import cats.implicits._

  def apply[F[_]: Sync](name: String)(
    implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): Trade[F, ItemStack] = (contents: List[ItemStack]) =>
    for {
      gachaList <- gachaPrizesDataOperations.gachaPrizesList
    } yield {
      // TODO GTアイテムかどうかを確率に依存すべきではない
      val giganticItemStacks =
        gachaList.filter(_.probability.value < 0.001).map(_.createNewItem(Some(name)))

      // 交換可能なItemStack達
      val trueItems = contents.filter { targetItem =>
        giganticItemStacks.exists(_.isSimilar(targetItem))
      }

      // 交換可能なアイテムたちから算出する椎名林檎の数
      val siinaringoAmount =
        trueItems.map(_.getAmount).sum * SeichiAssist.seichiAssistConfig.rateGiganticToRingo

      // 交換不可能なItemStack達
      val falseItems = contents.diff(trueItems)

      (siinaringoAmount, falseItems)
    }

}
