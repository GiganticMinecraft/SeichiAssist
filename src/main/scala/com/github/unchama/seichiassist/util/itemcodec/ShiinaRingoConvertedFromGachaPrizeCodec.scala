package com.github.unchama.seichiassist.util.itemcodec

import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.inventory.ItemStack
import org.bukkit.{ChatColor, Material}

/**
 * '''ガチャ景品から交換された'''椎名林檎をモデリングする[[ItemCodec]]。
 */
object ShiinaRingoConvertedFromGachaPrizeCodec extends ItemCodec[ShiinaRingoConvertedFromGachaPrizeCodec.Property] {
  import scala.jdk.CollectionConverters._
  final case class Property(ownedBy: String)

  override def getProperty(from: ItemStack): Option[ShiinaRingoConvertedFromGachaPrizeCodec.Property] = {
    if (from.getType ne Material.GOLDEN_APPLE) return None
    if (from.getDurability != 1) return None
    if (!from.hasItemMeta) return None
    val lore = from.getItemMeta.getLore.asScala
    if (lore.size != 4) return None
    val ownerLine = lore(2)
    val i = ownerLine.indexOf("所有者：")
    if (i == -1) return None
    val owner = ownerLine.substring(i + 4)
    Some(Property(owner))
  }

  override def create(property: ShiinaRingoConvertedFromGachaPrizeCodec.Property): ItemStack =
    new IconItemStackBuilder(Material.GOLDEN_APPLE, 1)
      .amount(1)
      .title(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "椎名林檎")
      .lore(
        ChatColor.RESET + "" + ChatColor.GRAY + "使用するとマナが全回復します",
        ChatColor.RESET + "" + ChatColor.AQUA + "マナ完全回復",
        ChatColor.RESET + "" + ChatColor.DARK_GREEN + "所有者:" + property.ownedBy,
        ChatColor.RESET + "" + ChatColor.GRAY + "ガチャ景品と交換しました。"
      )
      .build()
}
