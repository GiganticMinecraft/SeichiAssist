package com.github.unchama.seichiassist.util.itemcodec
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.{ChatColor, Material}
import org.bukkit.inventory.ItemStack

/**
 * 死神の鎌をモデリングするコーデック。
 */
object DeathGodSickleCodec extends ItemCodec[Unit] {
  private lazy val forTarget = new IconItemStackBuilder(Material.CARROT_STICK, 1)
    .amount(1)
    .title(ChatColor.DARK_RED + "死神の鎌")
    .lore(
      ChatColor.RED + "頭を狩り取る形をしている...",
      "",
      ChatColor.GRAY + "設置してある頭が",
      ChatColor.GRAY + "左クリックで即時に回収できます",
      ChatColor.DARK_GRAY + "インベントリに空きを作って使いましょう"
    )
    .build()

  /**
   * @inheritdoc
   */
  override def getProperty(from: ItemStack): Option[Unit] = Option.when(forTarget == from)(())

  /**
   * @inheritdoc
   */
  override def create(property: Unit): ItemStack = forTarget.clone()
}
