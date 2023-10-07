package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.effect.Sync
import com.github.unchama.minecraft.actions.{GetConnectedPlayers, OnMinecraftServerThread}
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.{
  DrawGacha,
  GrantGachaPrize
}
import com.github.unchama.seichiassist.subsystems.gacha.domain.{GrantState, LotteryOfGachaItems}
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaRarity._
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryoneIgnoringPreferenceM
import com.github.unchama.seichiassist.util._
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.api.chat.{HoverEvent, TextComponent}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitDrawGacha[F[_]: Sync: OnMinecraftServerThread: GetConnectedPlayers[*[_], Player]](
  implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
  lotteryOfGachaItems: LotteryOfGachaItems[F, ItemStack],
  grantGachaPrize: GrantGachaPrize[F, ItemStack, Player]
) extends DrawGacha[F, Player] {

  import cats.implicits._

  import scala.jdk.CollectionConverters._

  override def draw(player: Player, count: Int): F[Unit] = {
    implicitly[PlayerSendable[TextComponent, F]]

    for {
      currentGachaPrizes <- gachaPrizeAPI.listOfNow
      gachaPrizes <- lotteryOfGachaItems.runLottery(count, currentGachaPrizes)
      gachaPrizeWithGrantStates <- grantGachaPrize.grantGachaPrize(gachaPrizes)(player)
      _ <- gachaPrizeWithGrantStates.traverse {
        case (gachaPrize, grantState) =>
          val additionalMessage = grantState match {
            case GrantState.GrantedMineStack =>
              s"${AQUA}景品をマインスタックに収納しました。"
            case GrantState.GrantedInventoryOrDrop =>
              s"${AQUA}景品をインベントリに収納しました。"
          }

          GachaRarity.of(gachaPrize) match {
            case GachaRarity.Gigantic =>
              val prizeItem = gachaPrize.itemStack

              val localizedEnchantmentList =
                prizeItem.getItemMeta.getEnchants.asScala.toSeq.map {
                  case (enchantment, level) =>
                    s"$GRAY${EnchantNameToJapanese.getEnchantName(enchantment.getKey.toString, level)}"
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
                      new Text(
                        s" ${prizeItem.getItemMeta.getDisplayName}\n" +
                          ListFormatters.getDescFormat(localizedEnchantmentList.toList) +
                          ListFormatters
                            .getDescFormat(prizeItem.getItemMeta.getLore.asScala.toList)
                      )
                    )
                  }
                }
              Sync[F].delay {
                player.sendMessage(s"${RED}おめでとう！！！！！Gigantic☆大当たり！$additionalMessage")
                player.spigot().sendMessage(message)
              } >> sendMessageToEveryoneIgnoringPreferenceM[TextComponent, F](message) >> Sync[
                F
              ].delay {
                SendSoundEffect.sendEverySoundWithoutIgnore(
                  Sound.ENTITY_ENDER_DRAGON_DEATH,
                  0.5f,
                  2f
                )
              } >> sendMessageToEveryoneIgnoringPreferenceM[String, F](
                s"$GOLD${player.getName}がガチャでGigantic☆大当たり！"
              )
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
          val regularAmount = gachaPrizes.count(GachaRarity.of(_) == GachaRarity.Regular)
          val bigAmount = gachaPrizes.count(GachaRarity.of(_) == GachaRarity.Big)
          player.sendMessage(s"${YELLOW}当たりが${regularAmount}個,${GOLD}大当たりが${bigAmount}個出ました!")
          player.sendMessage(s"$AQUA${count}回ガチャを回しました。")
        }
        .whenA(count > 1)
      _ <- Sync[F].delay {
        player.playSound(player.getLocation, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0.1f)
      }
    } yield ()
  }
}
