package com.github.unchama.seichiassist.util.itemcodec
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.enchantments.Enchantment
import org.bukkit.{ChatColor, Material}
import org.bukkit.inventory.ItemStack

/**
 * 投票特典のピッケルをモデリングする[[ItemCodec]]。
 */
object VotePickaxeCodec extends ItemCodec[Unit] {
  import scala.util.chaining._

  private val enchantments = Map(
    Enchantment.DIG_SPEED -> int2Integer(3),
    Enchantment.DURABILITY -> int2Integer(3)
  ).foldLeft(new java.util.HashMap[Enchantment, Integer]())((m, t) => m.tap(_.put(t._1, t._2)))
  private lazy val singleton = new IconItemStackBuilder(Material.DIAMOND_PICKAXE)
    .amount(1)
    .title(ChatColor.YELLOW + "" + ChatColor.BOLD + "Thanks for Voting!")
    .lore(
      "投票ありがとナス♡"
    )
    .build()
    .tap(_.addUnsafeEnchantments(enchantments))

  /**
   * ItemStackからプロパティを得ることを試みる。
   *
   * @param from 対象
   * @return 有効なプロパティならSome、プロパティが取得できなかった場合はNone
   */
  override def getProperty(from: ItemStack): Option[Unit] = Option.when(singleton == from)(())

  /**
   * 指定したプロパティをItemStackへとデコードする。注意: 呼び出し側は返されたItemStackを自由に変更することができる。
   *
   * @param property 対象のプロパティ
   * @return
   */
  override def create(property: Unit): ItemStack = singleton.clone()
}
