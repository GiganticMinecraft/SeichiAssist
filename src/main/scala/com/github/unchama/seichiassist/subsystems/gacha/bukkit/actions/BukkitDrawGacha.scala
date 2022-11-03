package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.effect.{IO, Sync}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.{DrawGacha, GrantGachaPrize}
import com.github.unchama.seichiassist.subsystems.gacha.domain.{GrantState, LotteryOfGachaItems}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaRarity._
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.GachaPrize
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryone
import com.github.unchama.seichiassist.util._
import net.md_5.bungee.api.chat.{HoverEvent, TextComponent}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitDrawGacha[F[_]: Sync: OnMinecraftServerThread](
  gachaPrizesRepository: Vector[GachaPrize[ItemStack]]
)(
  implicit lotteryOfGachaItems: LotteryOfGachaItems[F, ItemStack],
  grantGachaPrize: GrantGachaPrize[F, ItemStack]
) extends DrawGacha[F, Player] {

  import PlayerSendable._
  import cats.implicits._

  import scala.jdk.CollectionConverters._

  override def draw(player: Player, count: Int): F[Unit] = {
    for {
      gachaPrizes <- lotteryOfGachaItems.runLottery(count, gachaPrizesRepository)
      states <- gachaPrizes.traverse(gachaPrize =>
        grantGachaPrize.grantGachaPrize(gachaPrize)(player)
      )
      _ <-
        (gachaPrizes zip states).traverse {
          case (gachaPrize, state) =>
            val additionalMessage = state match {
              case GrantState.GrantedMineStack =>
                s"${AQUA}景品をマインスタックに収納しました。"
              case GrantState.Dropped =>
                s"${AQUA}景品がドロップしました。"
              case _ =>
                ""
            }

            GachaRarity.of(gachaPrize) match {
              case GachaRarity.Gigantic =>
                val prizeItem = gachaPrize.itemStack

                val localizedEnchantmentList =
                  prizeItem.getItemMeta.getEnchants.asScala.toSeq.map {
                    case (enchantment, level) =>
                      s"$GRAY${EnchantNameToJapanese.getEnchantName(enchantment.getName, level)}"
                  }

                import scala.util.chaining._
                val message =
                  new TextComponent().tap { c =>
                    import c._
                    setText(
                      s"$AQUA${prizeItem.getItemMeta.getDisplayName}${GOLD}を引きました！おめでとうございます！"
                    )
                    setHoverEvent {
                      new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Array(
                          new TextComponent(
                            s" ${prizeItem.getItemMeta.getDisplayName}\n" +
                              ListFormatters.getDescFormat(localizedEnchantmentList.toList) +
                              ListFormatters
                                .getDescFormat(prizeItem.getItemMeta.getLore.asScala.toList)
                          )
                        )
                      )
                    }
                  }
                Sync[F].delay {
                  player.sendMessage(s"${RED}おめでとう！！！！！Gigantic☆大当たり！$additionalMessage")
                  player.spigot().sendMessage(message)
                  sendMessageToEveryone(s"$GOLD${player.getName}がガチャでGigantic☆大当たり！")(
                    forString[IO]
                  )
                  sendMessageToEveryone(message)(forTextComponent[IO])
                  SendSoundEffect.sendEverySoundWithoutIgnore(
                    Sound.ENTITY_ENDERDRAGON_DEATH,
                    0.5f,
                    2f
                  )
                }
              case GachaRarity.Big =>
                Sync[F].delay {
                  player.playSound(player.getLocation, Sound.ENTITY_WITHER_SPAWN, 0.8f, 1f)
                  if (count == 1) player.sendMessage(s"${GOLD}おめでとう！！大当たり！$additionalMessage")
                }
              case GachaRarity.Regular if count == 1 =>
                Sync[F].delay {
                  player.sendMessage(s"${YELLOW}おめでとう！当たり！$additionalMessage")
                }
              case GachaRarity.GachaRingoOrExpBottle if count == 1 =>
                Sync[F].delay {
                  player.sendMessage(s"${WHITE}はずれ！また遊んでね！$additionalMessage")
                }
              case _ => Sync[F].unit
            }
        }
      _ <- Sync[F]
        .delay {
          player.sendMessage(s"$AQUA${count}回ガチャを回しました。")
        }
        .whenA(count > 1)
    } yield ()
  }
}
