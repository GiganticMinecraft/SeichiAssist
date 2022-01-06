package com.github.unchama.seichiassist.itemconversionstorage

import cats.effect.IO
import com.github.unchama.itemconversionstorage.{ConversionResultSet, ItemConversionStorage}
import com.github.unchama.menuinventory.MenuFrame
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.{Bukkit, Material}

import scala.util.chaining._

/**
 * 鉱石 --> 交換券
 */
object OreTrade extends ItemConversionStorage {
  override type Environment = ()
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$LIGHT_PURPLE${BOLD}交換したい鉱石を入れてください")

  val requiredAmountPerTicket = Map(
    Material.COAL_ORE -> 128,
    Material.IRON_ORE -> 64,
    Material.GOLD_ORE -> 8,
    Material.LAPIS_ORE -> 8,
    Material.DIAMOND_ORE -> 4,
    Material.REDSTONE_ORE -> 32,
    Material.EMERALD_ORE -> 4,
    Material.QUARTZ_ORE -> 16
  )

  /**
   * @inheritdoc
   */
  override def doMap(player: Player, itemStack: ItemStack): IO[ConversionResultSet] = IO {
    val material = itemStack.getType
    val create = (amount: Int) => new ItemStack(Material.PAPER, amount).tap {
      _.setItemMeta {
        Bukkit.getItemFactory.getItemMeta(Material.PAPER).tap { m =>
          import m._
          setDisplayName(s"$DARK_RED${BOLD}交換券")
          addEnchant(Enchantment.PROTECTION_FIRE, 1, false)
          addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
      }
    }
    requiredAmountPerTicket.get(material).map(requiredAmount => {
      ConversionResultSet(
        Seq(
          create(itemStack.getAmount / requiredAmount),
          itemStack.clone().tap(_.setAmount(itemStack.getAmount % requiredAmount))
        ),
        Nil
      )
    }).getOrElse(
      ConversionResultSet(
        Nil,
        itemStack
      )
    )
  }
}
