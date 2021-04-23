package com.github.unchama.seichiassist.util.itemcodec
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.{ChatColor, Material}
import org.bukkit.inventory.ItemStack

/**
 * 投票ギフト券をモデリングする[[ItemCodec]]。
 */
object VotingGiftCodec extends ItemCodec[Unit] {
  private lazy val singleton = new IconItemStackBuilder(Material.PAPER)
    .amount(1)
    .title(ChatColor.AQUA + "投票ギフト券")
    .lore(
      "",
      ChatColor.WHITE + "公共施設鯖にある",
      ChatColor.WHITE + "デパートで買い物ができます"
    )
    .build()

  /**
   * @inheritdoc
   */
  override def getProperty(from: ItemStack): Option[Unit] = Option.when(singleton == from)(())

  /**
   * @inheritdoc
   */
  override def create(property: Unit): ItemStack = singleton.clone()
}
