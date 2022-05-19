package com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.BukkitDrawGacha
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.util._
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.ChatColor._
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.{GameMode, Material}

class GachaController[F[_]: ConcurrentEffect: OnMinecraftServerThread](
  implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
) extends Listener {

  @EventHandler
  def onPlayerRightClickGachaEvent(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer

    // サバイバルモードでない場合は処理を終了
    if (player.getGameMode != GameMode.SURVIVAL) return

    val clickedItemStack = event.getItem.ifNull {
      return
    }

    // ガチャ券でない場合は終了
    if (!ItemInformation.isGachaTicket(clickedItemStack)) return

    event.setCancelled(true)

    val playerData = SeichiAssist.playermap(player.getUniqueId)

    // 連打防止クールダウン処理
    if (!playerData.gachacooldownflag) return

    // 連打による負荷防止の為クールダウン処理
    new CoolDownTask(player, false, true).runTaskLater(SeichiAssist.instance, 4)

    // オフハンドから実行された時処理を終了
    if (event.getHand == EquipmentSlot.OFF_HAND) return

    // ガチャデータが設定されていない場合
    if (gachaPrizesDataOperations.getGachaPrizesList.toIO.unsafeRunSync().isEmpty) {
      player.sendMessage("ガチャが設定されていません")
      return
    }

    val action = event.getAction
    val clickedBlock = event.getClickedBlock.ifNull {
      return
    }

    /*
      AIRまたはBlockを右クリックしていない、または、Blockのときにチェストやトラップチェストをクリックしていれば処理を終了
      参照：https://github.com/GiganticMinecraft/SeichiAssist/issues/770
     */
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return
    if (
      action == Action.RIGHT_CLICK_BLOCK && (clickedBlock.getType == Material.CHEST || clickedBlock.getType == Material.TRAPPED_CHEST)
    ) return

    val count =
      if (player.isSneaking) {
        val amount = clickedItemStack.getAmount
        player.sendMessage(s"$AQUA${amount}回ガチャを回しました。")
        amount
      } else 1

    if (
      !InventoryOperations.removeItemfromPlayerInventory(
        player.getInventory,
        clickedItemStack,
        count
      )
    ) {
      player.sendMessage(RED.toString + "ガチャ券の数が不正です。")
      return
    }

    // ガチャを実行
    BukkitDrawGacha[F].draw(player, count).toIO.unsafeRunAsyncAndForget()

  }

}
