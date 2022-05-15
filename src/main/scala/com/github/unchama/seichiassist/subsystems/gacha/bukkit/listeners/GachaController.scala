package com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.LotteryOfGachaItems
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.util._
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import net.md_5.bungee.api.chat.{HoverEvent, TextComponent}
import org.bukkit.ChatColor._
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.{GameMode, Material, Sound}

import scala.collection.mutable

class GachaController[F[_]: ConcurrentEffect](
  implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
) extends Listener {

  import scala.jdk.CollectionConverters._

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

    // 各自当たった個数を記録するための変数
    var gachaBigWin = 0
    var gachaWin = 0
    var gachaGTWin = 0

    val prizes = LotteryOfGachaItems.using.draw(count).toIO.unsafeRunSync()

    prizes.foreach { prize =>
      /**
       *  メッセージ設定
       *  ①まずMineStackに入るか試す
       *  ②入らなかったらインベントリに直接入れる
       *  ③インベントリが満タンだったらドロップする
       */
      val givenItem = prize.getGiveItemStack(Some(player.getName))
      val additionalMessage =
        if (BreakUtil.tryAddItemIntoMineStack(player, givenItem)) {
          s"${AQUA}景品をマインスタックに収納しました。"
        } else {
          if (!InventoryOperations.isPlayerInventoryFull(player)) {
            InventoryOperations.addItem(player, givenItem)
            ""
          } else {
            InventoryOperations.dropItem(player, givenItem)
            s"${AQUA}景品がドロップしました。"
          }
        }

      // 確率に応じてメッセージを送信
      if (prize.probability < 0.001) {
        SendSoundEffect.sendEverySoundWithoutIgnore(Sound.ENTITY_ENDERDRAGON_DEATH, 0.5f, 2f)

        {
          playerData
            .settings
            .getBroadcastMutingSettings
            .flatMap(settings =>
              IO {
                if (!settings.shouldMuteMessages) {
                  player.playSound(player.getLocation, Sound.ENTITY_ENDERDRAGON_DEATH, 0.5f, 2f)
                }
              }
            )
        }.unsafeRunSync()

        val loreWithoutOwnerName = givenItem.getItemMeta.getLore.asScala.toList.filterNot {
          _ == s"§r§2所有者：${player.getName}"
        }

        val localizedEnchantmentList = givenItem.getItemMeta.getEnchants.asScala.toSeq.map {
          case (enchantment, level) =>
            s"$GRAY${EnchantNameToJapanese.getEnchantName(enchantment.getName, level)}"
        }

        import scala.util.chaining._
        val message =
          new TextComponent().tap { c =>
            import c._
            setText(s"$AQUA${givenItem.getItemMeta.getDisplayName}${GOLD}を引きました！おめでとうございます！")
            setHoverEvent {
              new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Array(
                  new TextComponent(
                    s" ${givenItem.getItemMeta.getDisplayName}\n" +
                      ListFormatters.getDescFormat(localizedEnchantmentList.toList) +
                      ListFormatters.getDescFormat(loreWithoutOwnerName)
                  )
                )
              )
            }
          }

        player.sendMessage(s"${RED}おめでとう！！！！！Gigantic☆大当たり！$additionalMessage")
        player.spigot.sendMessage(message)
        SendMessageEffect.sendMessageToEveryone(
          s"$GOLD${player.getDisplayName}がガチャでGigantic☆大当たり！"
        )
        SendMessageEffect.sendMessageToEveryone(message)
        gachaGTWin += 1
      } else if (prize.probability < 0.01) {
        player.playSound(player.getLocation, Sound.ENTITY_WITHER_SPAWN, 0.8f, 1f)
        if (count == 1) {
          player.sendMessage(s"${GOLD}おめでとう！！大当たり！$additionalMessage")
        }
        gachaBigWin += 1
      } else if (prize.probability < 0.1) {
        if (count == 1) {
          player.sendMessage(s"${YELLOW}おめでとう！当たり！$additionalMessage")
        }
        gachaWin += 1
      } else {
        if (count == 1) {
          player.sendMessage(s"${WHITE}はずれ！また遊んでね！$additionalMessage")
        }
      }
    }

    val rewardDetailTexts = mutable.ArrayBuffer[String]()
    if (gachaWin > 0) rewardDetailTexts += s"${YELLOW}当たりが${gachaWin}個"
    if (gachaBigWin > 0) rewardDetailTexts += s"${GOLD}大当たりが${gachaBigWin}個"
    if (gachaGTWin > 0) rewardDetailTexts += s"${RED}Gigantic☆大当たりが${gachaGTWin}個"
    if (count != 1) {
      player.sendMessage(if (rewardDetailTexts.isEmpty) {
        s"${WHITE}はずれ！また遊んでね！"
      } else {
        s"${rewardDetailTexts.mkString(s"$GRAY,")}${GOLD}出ました！"
      })
    }
    player.playSound(player.getLocation, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0.1f)
  }

}
