package com.github.unchama.seichiassist.itemconversion

import cats.effect.{IO, Sync}
import cats.kernel.Monoid
import com.github.unchama.itemconversion.{ConversionResultSet, ItemConversionSystem}
import com.github.unchama.menuinventory.MenuFrame
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import scala.util.chaining._

/**
 * GT --> 椎名林檎
 */
object GiganticTrade extends ItemConversionSystem {
  override type AggregationResultType = Int

  trait GiganticTradeRatioConfig[F[_]] {
    def getRaito: F[Int]
  }

  object GiganticTradeRatioConfig {
    def apply[F[_]: Sync]: GiganticTradeRatioConfig[F] = new GiganticTradeRatioConfig[F] {
      override def getRaito: F[Int] = Sync[F].delay {
        SeichiAssist.seichiAssistConfig.rateGiganticToRingo
      }
    }
  }

  case class Environment()(implicit val giganticTradeRaitoConfig: GiganticTradeRatioConfig[IO])
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"${GOLD.toString}${BOLD}椎名林檎と交換したい景品を入れてネ")

  override def doOperation(player: Player, inventory: Map[Int, ItemStack])(implicit environment: Environment): IO[ConversionResultSet[AggregationResultType]] = {
    if (SeichiAssist.gachamente) {
      // early-return
      IO.pure(ConversionResultSet(Nil, inventory.values.toSeq))
    } else {
      super.doOperation(player, inventory)
    }
  }
  
  /**
   * @inheritdoc
   */
  override def doMap(player: Player, itemStack: ItemStack)(implicit environment: Environment): IO[ConversionResultSet[AggregationResultType]] = {
    if (!itemStack.hasItemMeta ||
      !itemStack.getItemMeta.hasLore ||
      itemStack.getType == Material.SKULL_ITEM) {
      return IO.pure(ConversionResultSet(Nil, Seq(itemStack)))
    }
    val gachaDataList = SeichiAssist.gachadatalist
    val name = player.getName.toLowerCase
    for {
      appleRatio <- environment.giganticTradeRaitoConfig.getRaito
      crs <- IO {
        val (found, notFound) = gachaDataList.partition(gachaPrize => gachaPrize.itemStack.hasItemMeta && gachaPrize.itemStack.getItemMeta.hasLore && gachaPrize.compare(itemStack, name))
        val found2 = found.map(gachaPrize => {
          if (SeichiAssist.DEBUG) {
            player.sendMessage(gachaPrize.itemStack.getItemMeta.getDisplayName)
          }
          val amount = itemStack.getAmount
          if (gachaPrize.probability < 0.001) {
            //ギガンティック大当たりの部分
            //1個につき椎名林檎n個と交換する
            ConversionResultSet[AggregationResultType](Seq(StaticGachaPrizeFactory.getMaxRingo(player.getName).tap(_.setAmount(amount * appleRatio))), Nil)
          } else {
            //それ以外アイテム返却
            ConversionResultSet[AggregationResultType](Nil, Seq(itemStack))
          }
        })
        val m = Monoid[ConversionResultSet[AggregationResultType]]
        m.combine(m.combineAll(found2), ConversionResultSet(Nil, notFound.toSeq.map(_.itemStack)))
      }
    } yield crs

  }

  override def postEffect(conversionResultSet: ConversionResultSet[AggregationResultType]): TargetedEffect[Player] = if (SeichiAssist.gachamente) {
    MessageEffect(s"${RED}ガチャシステムメンテナンス中の為全てのアイテムを返却します")
  } else if (conversionResultSet.aggregationResult <= 0) {
    MessageEffect(s"${YELLOW}ギガンティック大当り景品を認識しませんでした。全てのアイテムを返却します")
  } else {
    MessageEffect(s"${GREEN}ギガンティック大当り景品を${conversionResultSet.convertedItems}個認識しました")
  }
}
