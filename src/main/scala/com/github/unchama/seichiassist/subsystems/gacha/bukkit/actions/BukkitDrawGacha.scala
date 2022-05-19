package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.effect.{ConcurrentEffect, IO, Sync, SyncIO}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.{
  DrawGacha,
  LotteryOfGachaItems
}
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryone
import com.github.unchama.seichiassist.util._
import net.md_5.bungee.api.chat.{HoverEvent, TextComponent}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player

object BukkitDrawGacha {

  import cats.effect.ConcurrentEffect.ops._

  import scala.jdk.CollectionConverters._

  import PlayerSendable._

  def apply[F[_]: Sync: ConcurrentEffect: OnMinecraftServerThread](
    implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): DrawGacha[F, Player] = (player: Player, amount: Int) => {
    OnMinecraftServerThread[F].runAction(SyncIO[Unit] {
      val gachaLotteryItems = LotteryOfGachaItems.using.lottery(amount).toIO.unsafeRunSync()

      val rarities: Vector[GachaRarity] = gachaLotteryItems.map { gachaPrize =>
        if (gachaPrize.probability.value < 0.001) gigantic
        else if (gachaPrize.probability.value < 0.01) big
        else if (gachaPrize.probability.value < 0.1) regular
        else potato
      }
      rarities.zip(gachaLotteryItems).map {
        case (rarity, gachaPrize) =>
          val givenItem = gachaPrize.getGiveItemStack(Some(player.getName))

          /**
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

          if (rarity == gigantic) {
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
            SendSoundEffect
              .sendEverySoundWithoutIgnore(Sound.ENTITY_ENDERDRAGON_DEATH, 0.5f, 2f)
          } else if (rarity == big) {
            player.playSound(player.getLocation, Sound.ENTITY_WITHER_SPAWN, 0.8f, 1f)
            if (amount == 1) player.sendMessage(s"${GOLD}おめでとう！！大当たり！$additionalMessage")
          } else if (rarity == regular) {
            if (amount == 1) player.sendMessage(s"${YELLOW}おめでとう！当たり！$additionalMessage")
          } else {
            if (amount == 1) player.sendMessage(s"${WHITE}はずれ！また遊んでね！$additionalMessage")
          }
      }
    })
  }

}
