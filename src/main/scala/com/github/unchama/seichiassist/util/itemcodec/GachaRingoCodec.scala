package com.github.unchama.seichiassist.util.itemcodec
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.inventory.ItemStack
import org.bukkit.{ChatColor, Material}

/**
 * がちゃりんごをモデリングするコーデック。
 */
object GachaRingoCodec extends ItemCodec[Unit] {
  private lazy val decodeTarget =
    new IconItemStackBuilder(Material.GOLDEN_APPLE)
      .amount(1)
      .title(s"${ChatColor.GOLD}${ChatColor.BOLD}がちゃりんご")
      .lore(
        s"${ChatColor.RESET}${ChatColor.GRAY}序盤に重宝します。",
        s"${ChatColor.RESET}${ChatColor.AQUA}マナ回復（小）"
      )
      .build()

  /**
   * @inheritdoc
   */
  override def getProperty(from: ItemStack): Option[Unit] = Option.when(from == decodeTarget)(())

  /**
   * @inheritdoc
   */
  override def create(property: Unit): ItemStack = decodeTarget.clone()
}
