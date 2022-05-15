package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryone
import com.github.unchama.seichiassist.util._
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect, UnfocusedEffect}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import net.md_5.bungee.api.chat.{HoverEvent, TextComponent}
import org.bukkit.ChatColor._
import org.bukkit.Sound

/**
 * ガチャを引く作用を返すtrait
 */

trait DoGachaDrawing[F[_], Player] {

  def draw: F[Unit] = draw(1)

  def draw(amount: Int): F[Unit]

}

object DoGachaDrawing {

  def apply[F[_], Player](implicit ev: DoGachaDrawing[F, Player]): DoGachaDrawing[F, Player] =
    ev

  import cats.implicits._
  import scala.jdk.CollectionConverters._

  def using[F[_]: Sync: ConcurrentEffect, Player](player: Player, name: String)(
    implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): DoGachaDrawing[F, Player] = (amount: Int) =>
    Sync[F].delay {
      val gachaPrizes = LotteryOfGachaItems.using.lottery(amount).toIO.unsafeRunSync()
      gachaPrizes.foreach { gachaPrize =>
        val givenItem = gachaPrize.getGiveItemStack(Some(name))
        // アイテム付与
        InventoryOperations.grantItemStacksEffect(givenItem) >> {
          // 確率に応じてメッセージを送信
          if (gachaPrize.probability < 0.001) {

            val loreWithoutOwnerName = givenItem.getItemMeta.getLore.asScala.toList.filterNot {
              _ == s"§r§2所有者：$name"
            }

            val localizedEnchantmentList = givenItem.getItemMeta.getEnchants.asScala.toSeq.map {
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

            SequentialEffect {
              MessageEffect(s"${RED}おめでとう！！！！！Gigantic☆大当たり！")
              MessageEffect(message)
              UnfocusedEffect {
                sendMessageToEveryone(s"$GOLD${name}がガチャでGigantic☆大当たり！")
                SendSoundEffect.sendEverySoundWithoutIgnore(
                  Sound.ENTITY_ENDERDRAGON_DEATH,
                  0.5f,
                  2f
                )
              }
            }
          } else if (gachaPrize.probability < 0.01) {
            SequentialEffect {
              FocusedSoundEffect(Sound.ENTITY_WITHER_SPAWN, 0.8f, 1f)
              if (amount == 1) MessageEffect(s"${GOLD}おめでとう！！大当たり！")
              else emptyEffect
            }
          } else if (gachaPrize.probability < 0.1) {
            SequentialEffect {
              if (amount == 1) MessageEffect(s"${YELLOW}おめでとう！当たり！")
              else emptyEffect
            }
          } else {
            SequentialEffect {
              if (amount == 1) MessageEffect(s"${WHITE}はずれ！また遊んでね！")
              else emptyEffect
            }
          }
        }
      }

    }

}
