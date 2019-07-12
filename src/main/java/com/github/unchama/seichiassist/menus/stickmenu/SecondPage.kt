package com.github.unchama.seichiassist.menus.stickmenu

import arrow.core.Left
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seasonalevents.events.valentine.Valentine
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.UUIDs
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.exp.ExperienceManager
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object SecondPage {
  private object ButtonComputations {
    suspend fun Player.computeHeadSummoningButton(): Button {
      val iconItemStack = run {
        val baseLore = listOf(
            "$RESET${GRAY}経験値10000を消費して",
            "$RESET${GRAY}自分の頭を召喚します",
            "$RESET${GRAY}装飾用にドウゾ！"
        )

        val actionNavigation =
            if (ExperienceManager(this).hasExp(10000)) {
              "$RESET$DARK_GREEN${UNDERLINE}クリックで召喚"
            } else {
              "$RESET$DARK_RED${UNDERLINE}経験値が足りません"
            }

        SkullItemStackBuilder(UUIDs.MHFVillager)
            .title("$YELLOW$UNDERLINE${BOLD}自分の頭を召喚")
            .lore(baseLore + actionNavigation)
            .build()
      }

      val effect = FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
        computedEffect {
          val expManager = ExperienceManager(it)
          if (expManager.hasExp(10000)) {
            val skullToGive = ItemStack(Material.SKULL_ITEM, 1).apply {
              durability = 3.toShort()
              itemMeta = (Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM) as SkullMeta).apply {
                owningPlayer = player
              }.let { meta ->
                //バレンタイン中(イベント中かどうかの判断はSeasonalEvent側で行う)
                Valentine.playerHeadLore(meta)
              }
            }

            sequentialEffect(
                unfocusedEffect { expManager.changeExp(-10000) },
                unfocusedEffect { Util.dropItem(it, skullToGive) },
                "${ChatColor.GOLD}経験値10000を消費して自分の頭を召喚しました".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
            )
          } else {
            sequentialEffect(
                "${ChatColor.RED}必要な経験値が足りません".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1.0f, 0.1f)
            )
          }
        }
      }

      return Button(iconItemStack, effect)
    }

    suspend fun Player.computeBroadcastMessageToggleButton(): Button {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val soundConfigurationState =
            if (playerData.everysoundflag) {
              "$RESET${GREEN}全体通知音:消音しない"
            } else {
              "$RESET${RED}全体通知音:消音する"
            }

        val messageConfigurationState =
            if (playerData.everymessageflag) {
              "$RESET${GREEN}全体メッセージ:表示する"
            } else {
              "$RESET${RED}全体メッセージ:表示しない"
            }

        IconItemStackBuilder(Material.JUKEBOX)
            .title("$YELLOW$UNDERLINE${BOLD}全体通知切替")
            .lore(listOf(
                soundConfigurationState,
                messageConfigurationState,
                "$RESET$DARK_RED${UNDERLINE}クリックで変更"
            ))
            .build()
      }

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeDeathMessageToggleButton(): Button {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val baseBuilder =
            IconItemStackBuilder(Material.FLINT_AND_STEEL)
                .title("$YELLOW$UNDERLINE${BOLD}死亡メッセージ表示切替")

        if (playerData.dispkilllogflag) {
          baseBuilder
              .enchanted()
              .lore(listOf(
                  "$RESET${GREEN}表示する",
                  "$RESET$DARK_RED${UNDERLINE}クリックで隠す"
              ))
        } else {
          baseBuilder
              .lore(listOf(
                  "$RESET${RED}隠す",
                  "$RESET$DARK_GREEN${UNDERLINE}クリックで表示する"
              ))
        }
      }.build()

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeWorldGuardMessageToggleButton(): Button {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val baseBuilder = IconItemStackBuilder(Material.BARRIER)
            .title("$YELLOW$UNDERLINE${BOLD}ワールドガード保護メッセージ表示切替")

        val loreHeading = "$RESET${GRAY}スキル使用時のワールドガード保護警告メッセージ"

        if (playerData.dispworldguardlogflag) {
          baseBuilder
              .enchanted()
              .lore(listOf(
                  loreHeading,
                  "$RESET${GREEN}表示する",
                  "$RESET$DARK_RED${UNDERLINE}クリックで隠す"
              ))
        } else {
          baseBuilder
              .lore(listOf(
                  loreHeading,
                  "$RESET${RED}隠す",
                  "$RESET$DARK_GREEN${UNDERLINE}クリックで表示する"
              ))
        }
      }.build()

      return Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                playerData.toggleWorldGuardLogEffect,
                deferredEffect {
                  val soundPitch: Float
                  val message: String
                  if (playerData.dispworldguardlogflag) {
                    soundPitch = 1.0f
                    message = "${ChatColor.GREEN}ワールドガード保護メッセージ:表示"
                  } else {
                    soundPitch = 0.5f
                    message = "${ChatColor.RED}ワールドガード保護メッセージ:隠す"
                  }

                  sequentialEffect(
                      FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch),
                      message.asMessageEffect()
                  )
                }
            )
          }
      )
    }

    suspend fun Player.computeHubCommandButton(): Button {
      val iconItemStack = IconItemStackBuilder(Material.NETHER_STAR)
          .title("$YELLOW$UNDERLINE${BOLD}ロビーサーバーへ移動")
          .lore(listOf(
              "$RESET$DARK_RED${UNDERLINE}クリックすると移動します",
              "$RESET${DARK_GRAY}command->[/hub]"
          ))
          .build()

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeOfficialWikiNavigationButton(): Button {
      val iconItemStack = IconItemStackBuilder(Material.BOOK)
          .title("$YELLOW$UNDERLINE${BOLD}公式Wikiにアクセス")
          .lore(listOf(
              "$RESET${GREEN}鯖内の「困った」は公式Wikiで解決！",
              "$RESET${DARK_GRAY}クリックするとチャット欄に",
              "$RESET${DARK_GRAY}URLが表示されますので",
              "$RESET${DARK_GRAY}Tキーを押してから",
              "$RESET${DARK_GRAY}そのURLをクリックしてください"
          ))
          .build()

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeRulesPageNavigationButton(): Button {
      val iconItemStack = IconItemStackBuilder(Material.PAPER)
          .title("$YELLOW$UNDERLINE${BOLD}運営方針とルールを確認")
          .lore(listOf(
              "$RESET${GREEN}当鯖で遊ぶ前に確認してネ！",
              "$RESET${DARK_GRAY}クリックするとチャット欄に",
              "$RESET${DARK_GRAY}URLが表示されますので",
              "$RESET${DARK_GRAY}Tキーを押してから",
              "$RESET${DARK_GRAY}そのURLをクリックしてください"
          ))
          .build()

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeServerMapNavigationButton(): Button {
      val iconItemStack = IconItemStackBuilder(Material.MAP)
          .title("$YELLOW$UNDERLINE${BOLD}鯖Mapを見る")
          .lore(listOf(
              "$RESET${GREEN}webブラウザから鯖Mapを閲覧出来ます",
              "$RESET${GREEN}他人の居場所や保護の場所を確認出来ます",
              "$RESET${DARK_GRAY}クリックするとチャット欄に",
              "$RESET${DARK_GRAY}URLが表示されますので",
              "$RESET${DARK_GRAY}Tキーを押してから",
              "$RESET${DARK_GRAY}そのURLをクリックしてください"
          ))
          .build()

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeJMSNavigationButton(): Button {
      val iconItemStack = IconItemStackBuilder(Material.SIGN)
          .title("$YELLOW$UNDERLINE${BOLD}JapanMinecraftServerリンク")
          .lore(listOf(
              "$RESET${DARK_GRAY}クリックするとチャット欄に",
              "$RESET${DARK_GRAY}URLが表示されますので",
              "$RESET${DARK_GRAY}Tキーを押してから",
              "$RESET${DARK_GRAY}そのURLをクリックしてください"
          ))
          .build()

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeAppleConversionButton(): Button {
      val iconItemStack = IconItemStackBuilder(Material.GOLDEN_APPLE)
          .title("$YELLOW$UNDERLINE${BOLD}GT景品→椎名林檎変換システム")
          .lore(listOf(
              "$RESET${GREEN}不必要なGT大当り景品を",
              "$RESET${GOLD}椎名林檎$RESET${GREEN}と交換できます",
              "$RESET${GREEN}出てきたインベントリーに",
              "$RESET${GREEN}交換したい景品を入れて",
              "$RESET${GREEN}escキーを押してください",
              "$RESET${DARK_GRAY}たまにアイテムが消失しますが",
              "$RESET${DARK_GRAY}補償はしていません(ごめんなさい)",
              "$RESET${DARK_GRAY}神に祈りながら交換しよう",
              "${RESET}現在の交換レート:GT景品1つにつき${SeichiAssist.seichiAssistConfig.rateGiganticToRingo()}個",
              "$RESET$DARK_GRAY$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeTitanConversionButton(): Button {
      val iconItemStack = IconItemStackBuilder(Material.DIAMOND_AXE)
          .title("$YELLOW$UNDERLINE${BOLD}限定タイタン修繕システム")
          .lore(listOf(
              "$RESET${GREEN}不具合によりテクスチャが反映されなくなってしまった",
              "$RESET${GOLD}ホワイトデーイベント限定タイタン$RESET${GREEN}を修繕できます",
              "$RESET${GREEN}出てきたインベントリーに",
              "$RESET${GREEN}修繕したいタイタンを入れて",
              "$RESET${GREEN}escキーを押してください",
              "$RESET${DARK_GRAY}たまにアイテムが消失しますが",
              "$RESET${DARK_GRAY}補償はしていません(ごめんなさい)",
              "$RESET${DARK_GRAY}神に祈りながら交換しよう",
              "$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .enchanted()
          .build()

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeShareInventoryButton(): Button {
      val iconItemStack = IconItemStackBuilder(Material.TRAPPED_CHEST)
          .build()

      // TODO add effect
      return Button(iconItemStack)
    }

    suspend fun Player.computeRecycleBinButton(): Button {
      val iconItemStack = IconItemStackBuilder(Material.BUCKET)
          .title("$YELLOW$UNDERLINE${BOLD}ゴミ箱を開く")
          .lore(listOf(
              "$RESET${GREEN}不用品の大量処分にドウゾ！",
              "$RESET${RED}復活しないので取扱注意",
              "$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      // TODO add effect
      return Button(iconItemStack)
    }
  }

  private suspend fun Player.computeMenuLayout(): IndexedSlotLayout = with(ButtonComputations) {
    IndexedSlotLayout(
        0 to computeOfficialWikiNavigationButton(),
        1 to computeRulesPageNavigationButton(),
        2 to computeServerMapNavigationButton(),
        3 to computeJMSNavigationButton(),
        6 to computeShareInventoryButton(),
        8 to computeHubCommandButton(),
        12 to computeHeadSummoningButton(),
        13 to computeBroadcastMessageToggleButton(),
        14 to computeDeathMessageToggleButton(),
        15 to computeWorldGuardMessageToggleButton(),
        27 to CommonButtons.openStickMenu,
        30 to computeRecycleBinButton(),
        34 to computeTitanConversionButton(),
        35 to computeAppleConversionButton()
    )
  }

  val open: TargetedEffect<Player> = computedEffect { player ->
    val view = MenuInventoryView(Left(4 * 9), "$DARK_PURPLE${BOLD}木の棒メニュー", player.computeMenuLayout())

    view.createNewSession().open
  }
}
