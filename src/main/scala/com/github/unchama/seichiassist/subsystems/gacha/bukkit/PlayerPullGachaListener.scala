package com.github.unchama.seichiassist.subsystems.gacha.bukkit

import cats.effect.ConcurrentEffect
import cats.effect.Effect.ops.toAllEffectOps
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.DrawGacha
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.util._
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.{EquipmentSlot, ItemStack}
import org.bukkit.{GameMode, Material}

class PlayerPullGachaListener[F[_]: ConcurrentEffect: OnMinecraftServerThread](
  implicit drawGacha: DrawGacha[F, Player],
  gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player]
) extends Listener {

  @EventHandler
  def onPlayerRightClickGachaEvent(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer

    if (player.getGameMode != GameMode.SURVIVAL) return

    val clickedItemStack = event.getItem.ifNull {
      return
    }

    if (!ItemInformation.isGachaTicket(clickedItemStack)) return

    event.setCancelled(true)

    val playerData = SeichiAssist.playermap(player.getUniqueId)

    // 連打防止クールダウン処理
    if (!playerData.gachacooldownflag) return

    // 連打による負荷防止の為クールダウン処理
    new CoolDownTask(player, false, true).runTaskLater(SeichiAssist.instance, 4)

    // オフハンドから実行された時処理を終了
    if (event.getHand == EquipmentSlot.OFF_HAND) return

    val action = event.getAction
    val clickedBlock = event.getClickedBlock.ifNull {
      return
    }

    /*
      以下の場合、処理を終了
     * - AIRを右クリックしていないかつ、Blockを右クリックしていない
     * - チェストやトラップチェストをクリックしている
      参照：https://github.com/GiganticMinecraft/SeichiAssist/issues/770
     */
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return
    if (
      clickedBlock.getType == Material.CHEST || clickedBlock.getType == Material.TRAPPED_CHEST
    ) return

    // ガチャデータが設定されていない場合
    if (gachaPrizeAPI.listOfNow.toIO.unsafeRunSync().isEmpty) {
      player.sendMessage("ガチャデータがありません")
      return
    }

    val count =
      if (player.isSneaking) {
        clickedItemStack.getAmount
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

    // ガチャの実行
    drawGacha.draw(player, count).toIO.unsafeRunAsyncAndForget()
  }

}
