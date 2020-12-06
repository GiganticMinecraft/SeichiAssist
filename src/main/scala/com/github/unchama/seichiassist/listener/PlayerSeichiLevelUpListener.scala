package com.github.unchama.seichiassist.listener

import cats.implicits._
import com.github.unchama.seichiassist.DefaultEffectEnvironment
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.data.{GachaSkullData, ItemData}
import com.github.unchama.seichiassist.event.SeichiLevelUpEvent
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import org.bukkit.entity.Player
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

object PlayerSeichiLevelUpListener extends Listener {
  @EventHandler
  def onPlayerLevelUp(event: SeichiLevelUpEvent): Unit = {
    implicit val player: Player = event.getPlayer

    val level = event.getLevelAfterLevelUp

    if (level != 30) giveItem("ガチャ券を付与する", level * 5, GachaSkullData.gachaForSeichiLevelUp)

    level match {
      case 10 =>
        giveItem("SuperPickaxeを配布する", 5, ItemData.getSuperPickaxe(1))
      case 20 =>
        GachaCommand.Gachagive(player, 3, player.getName)
        GachaCommand.Gachagive(player, 10, player.getName)
      case 30 =>
        giveItem("ガチャ券を付与する", level * 5, GachaSkullData.gachaForSeichiLevelUp)
      case 40 =>
        giveItem("ガチャりんごを付与する", 256, ItemData.getGachaApple(1))
      case 50 =>
        GachaCommand.Gachagive(player, 27, player.getName)
      case 60 =>
        GachaCommand.Gachagive(player, 26, player.getName)
      case 70 =>
        GachaCommand.Gachagive(player, 25, player.getName)
      case 80 =>
        GachaCommand.Gachagive(player, 24, player.getName)
      case 90 =>
        GachaCommand.Gachagive(player, 20, player.getName)
      case 100 =>
        GachaCommand.Gachagive(player, 21, player.getName)
        giveItem("エルサを付与する", 1, ItemData.getElsa(1))
    }
  }

  private def giveItem(context: String, amount: Int, item: ItemStack)(implicit player: Player): Unit = {
    DefaultEffectEnvironment.runEffectAsync(
      context,
      List.fill(amount)(
        grantItemStacksEffect(item).run(player)
      ).sequence
    )
  }
}
