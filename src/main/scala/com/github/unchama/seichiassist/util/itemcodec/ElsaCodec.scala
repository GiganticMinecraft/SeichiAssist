package com.github.unchama.seichiassist.util.itemcodec
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.enchantments.Enchantment
import org.bukkit.{ChatColor, Material}
import org.bukkit.inventory.ItemStack

/**
 * エルサをモデリングする[[ItemCodec]]。
 */
object ElsaCodec extends ItemCodec[Unit] {
  import scala.util.chaining._

  private val enchantments = Map(
    Enchantment.PROTECTION_ENVIRONMENTAL -> int2Integer(5),
    Enchantment.FROST_WALKER -> int2Integer(2),
    Enchantment.DURABILITY -> int2Integer(4),
    Enchantment.PROTECTION_FALL -> int2Integer(6),
    Enchantment.MENDING -> int2Integer(1)
  ).foldLeft(new java.util.HashMap[Enchantment, Integer]())((m, t) => m.tap(_.put(t._1, t._2)))

  private lazy val singleton = new IconItemStackBuilder(Material.DIAMOND_BOOTS)
    .amount(1)
    .title(ChatColor.AQUA + "" + ChatColor.ITALIC + "エルサ")
    .lore(
      "",
      ChatColor.GREEN + "装備中の移動速度" + ChatColor.YELLOW + "(中)" + ChatColor.GREEN,
      "",
      ChatColor.YELLOW + "金床" + ChatColor.RED + "不可",
      ChatColor.YELLOW + "修繕エンチャント" + ChatColor.AQUA + "可"
    )
    .build()
    .tap(_.addUnsafeEnchantments(enchantments))
  /**
   * ItemStackからプロパティを得ることを試みる。
   *
   * @param from 対象
   * @return 有効なプロパティならSome、プロパティが取得できなかった場合はNone
   */
  override def getProperty(from: ItemStack): Option[Unit] = Option.when(from == singleton)(())

  /**
   * 指定したプロパティをItemStackへとデコードする。注意: 呼び出し側は返されたItemStackを自由に変更することができる。
   *
   * @param property 対象のプロパティ
   * @return
   */
  override def create(property: Unit): ItemStack = singleton.clone()
}
