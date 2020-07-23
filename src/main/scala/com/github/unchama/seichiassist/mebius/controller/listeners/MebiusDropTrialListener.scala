package com.github.unchama.seichiassist.mebius.controller.listeners

import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.service.MebiusDroppingService
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.{MaterialSets, SeichiAssist}
import org.bukkit.ChatColor.{RED, RESET}
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.{Bukkit, ChatColor, Sound}

class MebiusDropTrialListener extends Listener {
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusDropOn(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer

    val droppedMebiusProperty = MebiusDroppingService
      .tryForDrop(player.getPlayer.getName).unsafeRunSync()
      .getOrElse(return)

    val mebius = ItemStackMebiusCodec.materialize(droppedMebiusProperty)

    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}おめでとうございます。採掘中にMEBIUSを発見しました。")
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}MEBIUSはプレイヤーと共に成長するヘルメットです。")
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}あなただけのMEBIUSを育てましょう！")
    Bukkit.getServer.getScheduler.runTaskLater(
      SeichiAssist.instance,
      () => SeichiAssist.playermap(player.getUniqueId).mebius.speakForce {
        s"こんにちは、${player.getName}$RESET。" +
          s"僕は${ItemStackMebiusCodec.displayNameOfMaterializedItem(droppedMebiusProperty)}" +
          s"$RESET！これからよろしくね！"
      },
      10
    )

    player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)

    if (!Util.isPlayerInventoryFull(player)) {
      Util.addItem(player, mebius)
    } else {
      player.sendMessage(s"$RESET$RED${ChatColor.BOLD}所持しきれないためMEBIUSをドロップしました。")
      Util.dropItem(player, mebius)
    }
  }
}
