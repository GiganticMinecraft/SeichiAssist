package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.effect.{IO, Sync}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.DrawGacha
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.util.PlayerSendable.{forString, forTextComponent}
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryone
import com.github.unchama.seichiassist.util._
import net.md_5.bungee.api.chat.{HoverEvent, TextComponent}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player

object BukkitDrawGacha {

  import cats.implicits._
  import scala.jdk.CollectionConverters._

  def apply[F[_]: Sync]: DrawGacha[F, Player] =
    new DrawGacha[F, Player] {
      override def draw(player: Player, amount: Int)(
        implicit gachaAPI: GachaAPI[F]
      ): F[Unit] = {
        for {
          gachaPrizes <- gachaAPI.runLottery(amount)
        } yield {
          gachaPrizes.foreach { gachaPrize =>
            val givenItem = gachaPrize.createNewItem(Some(player.getName))

            /*
             *  メッセージ設定
             *  ①まずMineStackに入るか試す
             *  ②入らなかったらインベントリに直接入れる
             *  ③インベントリが満タンだったらドロップする
             */
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

            if (gachaPrize.probability.value < GachaRarity.Gigantic.maxProbability.value) {
              val loreWithoutOwnerName =
                givenItem.getItemMeta.getLore.asScala.toList.filterNot {
                  _ == s"§r§2所有者：${player.getName}"
                }

              val localizedEnchantmentList =
                givenItem.getItemMeta.getEnchants.asScala.toSeq.map {
                  case (enchantment, level) =>
                    s"$GRAY${EnchantNameToJapanese.getEnchantName(enchantment.getName, level)}"
                }

              import scala.util.chaining._
              val message =
                new TextComponent().tap { c =>
                  import c._
                  setText(
                    s"$AQUA${givenItem.getItemMeta.getDisplayName}${GOLD}を引きました！おめでとうございます！"
                  )
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
              player.spigot().sendMessage(message)
              sendMessageToEveryone(s"$GOLD${player.getName}がガチャでGigantic☆大当たり！")(forString[IO])
              sendMessageToEveryone(message)(forTextComponent[IO])
              SendSoundEffect.sendEverySoundWithoutIgnore(
                Sound.ENTITY_ENDERDRAGON_DEATH,
                0.5f,
                2f
              )
            } else if (gachaPrize.probability.value < GachaRarity.Big.maxProbability.value) {
              player.playSound(player.getLocation, Sound.ENTITY_WITHER_SPAWN, 0.8f, 1f)
              if (amount == 1) player.sendMessage(s"${GOLD}おめでとう！！大当たり！$additionalMessage")
            } else if (
              gachaPrize.probability.value < GachaRarity.Regular.maxProbability.value
            ) {
              if (amount == 1) player.sendMessage(s"${YELLOW}おめでとう！当たり！$additionalMessage")
            } else {
              if (amount == 1) player.sendMessage(s"${WHITE}はずれ！また遊んでね！$additionalMessage")
            }
          }
        }
      }
    }

}
