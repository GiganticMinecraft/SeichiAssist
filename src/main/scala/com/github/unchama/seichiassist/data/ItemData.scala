package com.github.unchama.seichiassist.data

import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

object ItemData {
  def getSuperPickaxe(amount: Int): ItemStack = {
    new IconItemStackBuilder(Material.DIAMOND_PICKAXE)
      .amount(amount)
      .title(s"$YELLOW${BOLD}Thanks for Voting!")
      .lore(
        "投票ありがとナス♡"
      )
      .addEnchant(Enchantment.DIG_SPEED, 3)
      .addEnchant(Enchantment.DURABILITY, 3)
      .build()
  }

  def getForLevelUpskull(name: String, amount: Int): ItemStack = {
    new SkullItemStackBuilder("unchama")
      .amount(amount)
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(
        s"$RESET${GREEN}右クリックで使えます",
        s"$RESET${DARK_GREEN}所有者：$name", RESET + "" + DARK_RED + "レベルアップ記念です"
      )
      .build()
  }

  def getGachaApple(amount: Int): ItemStack = {
    import scala.jdk.CollectionConverters._
    new IconItemStackBuilder(Material.GOLDEN_APPLE)
      .amount(amount)
      .title(StaticGachaPrizeFactory.getGachaRingoName)
      .lore(StaticGachaPrizeFactory.getGachaRingoLore.asScala.toList)
      .build()
  }

  def getElsa(amount: Int): ItemStack = {
    new IconItemStackBuilder(Material.DIAMOND_BOOTS)
      .amount(amount)
      .title(s"$AQUA${ITALIC}エルサ")
      .lore(
        "", 
        s"${GREEN}装備中の移動速度$YELLOW(中)$GREEN", 
        s"",
        s"${YELLOW}金床${RED}不可", 
        s"${YELLOW}修繕エンチャント${AQUA}可"
      )
      .addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5)
      .addEnchant(Enchantment.FROST_WALKER, 2)
      .addEnchant(Enchantment.DURABILITY, 4)
      .addEnchant(Enchantment.PROTECTION_FALL, 6)
      .addEnchant(Enchantment.MENDING, 1)
      .build()
  }

  def getVotingGift(amount: Int): ItemStack = {
    new IconItemStackBuilder(Material.PAPER)
      .amount(amount)
      .title(s"${AQUA}投票ギフト券")
      .lore(
        "",
        s"${WHITE}公共施設鯖にある",
        s"${WHITE}デパートで買い物ができます"
      )
      .build()
  }
}
