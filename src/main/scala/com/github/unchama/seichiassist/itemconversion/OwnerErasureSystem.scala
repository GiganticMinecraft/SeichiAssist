package com.github.unchama.seichiassist.itemconversion

import cats.effect.IO
import cats.kernel.Monoid
import com.github.unchama.itemconversion.{ConversionResultSet, ItemConversionSystem}
import com.github.unchama.menuinventory.MenuFrame
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 名義除去システム
 */
object OwnerErasureSystem extends ItemConversionSystem {
  override type Environment = Unit
  override type AggregationResultType = Int
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$GOLD${BOLD}所有者表記をなくしたいアイテムを投入してネ")

  /**
   * @inheritdoc
   */
  override def doMap(player: Player, itemStack: ItemStack)(implicit environment: Environment): IO[ConversionResultSet[AggregationResultType]] = IO {
    val shouldConvert = (item: ItemStack) => {
      (item ne null) &&
        item.hasItemMeta &&
        item.getItemMeta.hasLore &&
        Util.itemStackContainsOwnerName(item, player.getName)
    }

    if (shouldConvert(itemStack)) {
      import scala.jdk.CollectionConverters._
      import scala.util.chaining._
      val itemLore = itemStack.getItemMeta.getLore.asScala.toList
      //itemLoreのListの中から、"所有者"で始まるものを弾き、新しく「所有者:なし」を付け加えたLoreをアイテムにつける
      val newItemLore = itemLore.map(lore =>
        if (lore.startsWith("所有者")) "所有者:なし"
        else lore
      ).asJava
      val itemMeta = Bukkit.getItemFactory.getItemMeta(itemStack.getType).tap { meta =>
        import meta._
        //所有者表記をなしにしたLoreを付与する
        setLore(newItemLore)
      }

      ConversionResultSet.fromConvertedItemCount(Seq(new ItemStack(itemStack.getType, itemStack.getAmount).tap(_.setItemMeta(itemMeta))), Nil)
    } else {
      ConversionResultSet.fromConvertedItemCount(Nil, Seq(itemStack))
    }
  }

  override def postEffect(conversionResultSet: ConversionResultSet[AggregationResultType]): TargetedEffect[Player] = {
    val convertedCount = conversionResultSet.aggregationResult

    if (convertedCount == 0)
      MessageEffect(s"${GREEN}所有者表記のされたアイテムが認識されませんでした。すべてのアイテムを返却します。")
    else
      MessageEffect(s"$GREEN${convertedCount}個のアイテムを認識し、所有者表記を「なし」に変更しました")
  }
}
