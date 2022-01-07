package com.github.unchama.seichiassist.itemconversion

import cats.effect.IO
import cats.kernel.Monoid
import com.github.unchama.itemconversion.{ConversionResultSet, ItemConversionSystem}
import com.github.unchama.menuinventory.MenuFrame
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.GachaSkullData
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import scala.util.chaining._

/**
 * 非GT --> ガチャ券
 */
object GachaTrade extends ItemConversionSystem {
  override type AggregationResultType = AggregationResult
  override type Environment = Unit
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$LIGHT_PURPLE${BOLD}交換したい景品を入れてください")

  override def doOperation(player: Player, inventory: Map[Int, ItemStack])(implicit environment: Environment): IO[ConversionResultSet[AggregationResultType]] = {
    if (SeichiAssist.gachamente) {
      IO.pure(ConversionResultSet(Nil, inventory.values.toSeq))
    } else {
      super.doOperation(player, inventory)
    }
  }

  override def doMap(player: Player, itemStack: ItemStack)(implicit environment: Environment): IO[ConversionResultSet[AggregationResultType]] = IO {
    val gachaDataList = SeichiAssist.gachadatalist
    val name = player.getName.toLowerCase
    val x = GachaSkullData.gachaForExchanging
    val (found, notFound) = gachaDataList.partition(gachaPrize => gachaPrize.itemStack.hasItemMeta && gachaPrize.itemStack.getItemMeta.hasLore && gachaPrize.compare(itemStack, name))
    val found2 = found.map(gachaPrize => {
      if (SeichiAssist.DEBUG) {
        player.sendMessage(gachaPrize.itemStack.getItemMeta.getDisplayName)
      }
      val amount = itemStack.getAmount
      if (gachaPrize.probability < 0.001) {
        //ギガンティック大当たりの部分
        //ガチャ券に交換せずそのままアイテムを返す
        ConversionResultSet(Nil, Seq(itemStack), AggregationResult(0, 0))
      } else if (gachaPrize.probability < 0.01) {
        //大当たりの部分
        ConversionResultSet(Seq(x.clone().tap(_.setAmount(12 * amount))), Nil, AggregationResult(1, 0))
      } else if (gachaPrize.probability < 0.1) {
        //当たりの部分
        ConversionResultSet(Seq(x.clone().tap(_.setAmount(3 * amount))), Nil, AggregationResult(0, 1))
      } else {
        //それ以外アイテム返却(経験値ポーションとかがここにくるはず)
        ConversionResultSet(Nil, Seq(itemStack), AggregationResult(0, 0))
      }
    })
    val m = summonMonoid
    m.combine(m.combineAll(found2.toList), ConversionResultSet(Nil, notFound.toSeq.map(_.itemStack), AggregationResult(0, 0)))
  }

  override def postEffect(conversionResultSet: ConversionResultSet[AggregationResultType]): TargetedEffect[Player] = {
    val AggregationResult(big, reg) = conversionResultSet.aggregationResult
    if (SeichiAssist.gachamente) {
      MessageEffect(s"${RED}ガチャシステムメンテナンス中の為全てのアイテムを返却します")
    } else if (big == 0 && reg == 0) {
      MessageEffect(s"${YELLOW}景品を認識しませんでした。全てのアイテムを返却します")
    } else {
      val count = conversionResultSet.convertedItems.map(_.getAmount).sum
      MessageEffect(
        List(
          s"${GREEN}大当たり景品を${big}個、当たり景品を${reg}個認識しました",
          s"$GREEN${count}枚の${GOLD}ガチャ券${WHITE}を受け取りました"
        )
      )
    }
  }

  private[GachaTrade] case class AggregationResult(big: Int, reg: Int)

  private object AggregationResult {
    implicit val monoid: Monoid[AggregationResult] = Monoid.instance(AggregationResult(0, 0), {
      case (AggregationResult(big1, reg1), AggregationResult(big2, reg2)) =>
        AggregationResult(big1 + big2, reg1 + reg2)
    })
  }
}
