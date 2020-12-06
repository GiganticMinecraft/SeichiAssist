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

    giveItem("ガチャ券を付与する", level * 5, GachaSkullData.gachaForSeichiLevelUp)

    level match {
      case 10 =>
        giveItem("SuperPickaxeを配布する", 5, ItemData.getSuperPickaxe(1))
      case 20 =>
        runGacha(13)
      case 40 =>
        giveItem("ガチャりんごを付与する", 256, ItemData.getGachaApple(1))
      case 50 =>
        runGacha(23)
      case 60 =>
        runGacha(26)
      case 70 =>
        runGacha(25)
      case 80 =>
        runGacha(24)
      case 90 =>
        runGacha(20)
      case 100 =>
        runGacha(21)
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

  private def runGacha(times: Int)(implicit player: Player) = GachaCommand.Gachagive(player, times, player.getName)
}
