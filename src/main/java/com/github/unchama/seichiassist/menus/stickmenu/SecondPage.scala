package com.github.unchama.seichiassist.menus.stickmenu

import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.menuinventory.{IndexedSlotLayout, Menu, MenuInventoryView}
import com.github.unchama.seasonalevents.events.valentine.Valentine
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.{Schedulers, SeichiAssist, SkullOwners}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import com.github.unchama.{menuinventory, targetedeffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Material, Sound}

/**
 * 木の棒メニュー2ページ目
 */
object SecondPage extends Menu {
  private object ConstantButtons {
    val officialWikiNavigationButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.BOOK)
          .title(s"$YELLOW$UNDERLINE${BOLD}公式Wikiにアクセス")
          .lore(List(
              s"$RESET${GREEN}鯖内の「困った」は公式Wikiで解決！",
              s"$RESET${DARK_GRAY}クリックするとチャット欄に",
              s"$RESET${DARK_GRAY}URLが表示されますので",
              s"$RESET${DARK_GRAY}Tキーを押してから",
              s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          ))
          .build()

      Button(
          iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                s"$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("official")}".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
            )
          }
      )
    }

    val rulesPageNavigationButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.PAPER)
          .title(s"$YELLOW$UNDERLINE${BOLD}運営方針とルールを確認")
          .lore(List(
              s"$RESET${GREEN}当鯖で遊ぶ前に確認してネ！",
              s"$RESET${DARK_GRAY}クリックするとチャット欄に",
              s"$RESET${DARK_GRAY}URLが表示されますので",
              s"$RESET${DARK_GRAY}Tキーを押してから",
              s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          ))
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                s"$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("rule")}".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
            )
          }
      )
    }

    val serverMapNavigationButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.MAP)
          .title(s"$YELLOW$UNDERLINE${BOLD}鯖Mapを見る")
          .lore(List(
              s"$RESET${GREEN}webブラウザから鯖Mapを閲覧出来ます",
              s"$RESET${GREEN}他人の居場所や保護の場所を確認出来ます",
              s"$RESET${DARK_GRAY}クリックするとチャット欄に",
              s"$RESET${DARK_GRAY}URLが表示されますので",
              s"$RESET${DARK_GRAY}Tキーを押してから",
              s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          ))
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                s"$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("map")}".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
            )
          }
      )
    }

    val JMSNavigationButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.SIGN)
          .title(s"$YELLOW$UNDERLINE${BOLD}JapanMinecraftServerリンク")
          .lore(List(
              s"$RESET${DARK_GRAY}クリックするとチャット欄に",
              s"$RESET${DARK_GRAY}URLが表示されますので",
              s"$RESET${DARK_GRAY}Tキーを押してから",
              s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          ))
          .build()

      Button(
          iconItemStack,
          action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                s"$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("jms")}".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
            )
          }
      )
    }

    val appleConversionButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.GOLDEN_APPLE, durability = 1)
          .title(s"$YELLOW$UNDERLINE${BOLD}GT景品→椎名林檎変換システム")
          .lore(List(
              s"$RESET${GREEN}不必要なGT大当り景品を",
              s"$RESET${GOLD}椎名林檎$RESET${GREEN}と交換できます",
              s"$RESET${GREEN}出てきたインベントリーに",
              s"$RESET${GREEN}交換したい景品を入れて",
              s"$RESET${GREEN}escキーを押してください",
              s"$RESET${DARK_GRAY}たまにアイテムが消失しますが",
              s"$RESET${DARK_GRAY}補償はしていません(ごめんなさい)",
              s"$RESET${DARK_GRAY}神に祈りながら交換しよう",
              s"${RESET}現在の交換レート:GT景品1つにつき${SeichiAssist.seichiAssistConfig.rateGiganticToRingo()}個",
              s"$RESET$DARK_GRAY$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
                TargetedEffect {
                  // TODO メニューインベントリに差し替える
                  it.openInventory(createInventory(size = 4.rows(), title =s"$GOLD${BOLD}椎名林檎と交換したい景品を入れてネ"))
                }
            )
          }
      )
    }

    val titanConversionButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.DIAMOND_AXE, durability = 1)
          .title(s"$YELLOW$UNDERLINE${BOLD}限定タイタン修繕システム")
          .lore(List(
              s"$RESET${GREEN}不具合によりテクスチャが反映されなくなってしまった",
              s"$RESET${GOLD}ホワイトデーイベント限定タイタン$RESET${GREEN}を修繕できます",
              s"$RESET${GREEN}出てきたインベントリーに",
              s"$RESET${GREEN}修繕したいタイタンを入れて",
              s"$RESET${GREEN}escキーを押してください",
              s"$RESET${DARK_GRAY}たまにアイテムが消失しますが",
              s"$RESET${DARK_GRAY}補償はしていません(ごめんなさい)",
              s"$RESET${DARK_GRAY}神に祈りながら交換しよう",
              s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .unbreakable()
          .build()

      Button(
          iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
                TargetedEffect {
                  // TODO メニューインベントリに差し替える
                  it.openInventory(createInventory(size = 4.rows(), title =s"$GOLD${BOLD}修繕したい限定タイタンを入れてネ"))
                }
            )
          }
      )
    }

    val recycleBinButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.BUCKET)
          .title(s"$YELLOW$UNDERLINE${BOLD}ゴミ箱を開く")
          .lore(List(
              s"$RESET${GREEN}不用品の大量処分にドウゾ！",
              s"$RESET${RED}復活しないので取扱注意",
              s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
          iconItemStack,
          action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 1.5f),
                TargetedEffect {
                  // TODO メニューインベントリに差し替える
                  it.openInventory(createInventory(size = 4.rows(), title = s"$RED${BOLD}ゴミ箱(取扱注意)"))
                }
            )
          }
      )
    }

    val hubCommandButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.NETHER_STAR)
          .title(s"$YELLOW$UNDERLINE${BOLD}ロビーサーバーへ移動")
          .lore(List(
              s"$RESET$DARK_RED${UNDERLINE}クリックすると移動します",
              s"$RESET${DARK_GRAY}command=>[/hub]"
          ))
          .build()

      Button(
          iconItemStack,
          action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
                "hub".asCommandEffect()
            )
          }
      )
    }
  }

  private case class ButtonComputations(val player: Player) extends AnyVal {
    @SuspendingMethod def computeHeadSummoningButton(): Button = recomputedButton {
      val iconItemStack = run {
        val baseLore = List(
            s"$RESET${GRAY}経験値10000を消費して",
            s"$RESET${GRAY}自分の頭を召喚します",
            s"$RESET${GRAY}装飾用にドウゾ！"
        )

        val actionNavigation =
            if (ExperienceManager(this).hasExp(10000)) {
              s"$RESET$DARK_GREEN${UNDERLINE}クリックで召喚"
            } else {
              s"$RESET$DARK_RED${UNDERLINE}経験値が足りません"
            }

        SkullItemStackBuilder(SkullOwners.MHF_Villager)
            .title(s"$YELLOW$UNDERLINE${BOLD}自分の頭を召喚")
            .lore(baseLore + actionNavigation)
            .build()
      }

      val effect = action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
        sequentialEffect(
            computedEffect {
              val expManager = ExperienceManager(it)
              if (expManager.hasExp(10000)) {
                val skullToGive = SkullItemStackBuilder(uniqueId).build().apply {
                  //バレンタイン中(イベント中かどうかの判断はSeasonalEvent側で行う)
                  itemMeta = Valentine.playerHeadLore(itemMeta as SkullMeta)
                }

                sequentialEffect(
                    UnfocusedEffect { expManager.changeExp(-10000) },
                    targetedeffect.UnfocusedEffect { Util.dropItem(it, skullToGive) },
                  s"${GOLD}経験値10000を消費して自分の頭を召喚しました".asMessageEffect(),
                    FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
                )
              } else {
                sequentialEffect(
                  s"${RED}必要な経験値が足りません".asMessageEffect(),
                    FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1.0f, 0.1f)
                )
              }
            }
        )
      }

      Button(iconItemStack, effect)
    }

    @SuspendingMethod def computeBroadcastMessageToggleButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]
      val iconItemStack = run {
        val currentSettings = playerData.settings.getBroadcastMutingSettings()

        val soundConfigurationState =
            if (currentSettings.shouldMuteSounds()) {
              s"$RESET${GREEN}全体通知音:消音しない"
            } else {
              s"$RESET${RED}全体通知音:消音する"
            }

        val messageConfigurationState =
            if (currentSettings.shouldMuteMessages()) {
              s"$RESET${GREEN}全体メッセージ:表示する"
            } else {
              s"$RESET${RED}全体メッセージ:表示しない"
            }

        IconItemStackBuilder(Material.JUKEBOX)
            .title(s"$YELLOW$UNDERLINE${BOLD}全体通知切替")
            .lore(List(
                soundConfigurationState,
                messageConfigurationState,
                s"$RESET$DARK_RED${UNDERLINE}クリックで変更"
            ))
            .build()
      }

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                playerData.settings.toggleBroadcastMutingSettings,
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
                deferredEffect {
                  when (playerData.settings.getBroadcastMutingSettings()) {
                    RECEIVE_MESSAGE_AND_SOUND => s"${GREEN}非表示/消音設定を解除しました"
                    RECEIVE_MESSAGE_ONLY => s"${RED}消音可能な全体通知音を消音します"
                    MUTE_MESSAGE_AND_SOUND => s"${RED}非表示可能な全体メッセージを非表示にします"
                  }.asMessageEffect()
                }
            )
          }
      )
    }

    @SuspendingMethod def computeDeathMessageToggleButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]

      val iconItemStack = run {
        val baseBuilder =
            IconItemStackBuilder(Material.FLINT_AND_STEEL)
                .title(s"$YELLOW$UNDERLINE${BOLD}死亡メッセージ表示切替")

        if (playerData.settings.shouldDisplayDeathMessages) {
          baseBuilder
              .enchanted()
              .lore(List(
                  s"$RESET${GREEN}表示する",
                  s"$RESET$DARK_RED${UNDERLINE}クリックで隠す"
              ))
        } else {
          baseBuilder
              .lore(List(
                  s"$RESET${RED}隠す",
                  s"$RESET$DARK_GREEN${UNDERLINE}クリックで表示する"
              ))
        }
      }.build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                playerData.settings.toggleDeathMessageMutingSettings,
                deferredEffect {
                  val (soundPitch, message) =
                      if (playerData.settings.shouldDisplayDeathMessages)
                        Pair(1.0f, s"${GREEN}死亡メッセージ:表示")
                      else
                        Pair(0.5f, s"${RED}死亡メッセージ:隠す")

                  sequentialEffect(
                      message.asMessageEffect(),
                      FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch)
                  )
                }
            )
          }
      )
    }

    @SuspendingMethod def computeWorldGuardMessageToggleButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]

      val iconItemStack = run {
        val baseBuilder = IconItemStackBuilder(Material.BARRIER)
            .title(s"$YELLOW$UNDERLINE${BOLD}ワールドガード保護メッセージ表示切替")

        val loreHeading = s"$RESET${GRAY}スキル使用時のワールドガード保護警告メッセージ"

        if (playerData.settings.shouldDisplayWorldGuardLogs) {
          baseBuilder
              .enchanted()
              .lore(List(
                  loreHeading,
                  s"$RESET${GREEN}表示する",
                  s"$RESET$DARK_RED${UNDERLINE}クリックで隠す"
              ))
        } else {
          baseBuilder
              .lore(List(
                  loreHeading,
                  s"$RESET${RED}隠す",
                  s"$RESET$DARK_GREEN${UNDERLINE}クリックで表示する"
              ))
        }
      }.build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                playerData.settings.toggleWorldGuardLogEffect,
                deferredEffect {
                  val (soundPitch, message) =
                      if (playerData.settings.shouldDisplayWorldGuardLogs)
                        Pair(1.0f, s"${GREEN}ワールドガード保護メッセージ:表示")
                      else
                        Pair(0.5f, s"${RED}ワールドガード保護メッセージ:隠す")

                  sequentialEffect(
                      FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch),
                      message.asMessageEffect()
                  )
                }
            )
          }
      )
    }

    @SuspendingMethod def computeShareInventoryButton(): Button = recomputedButton {
      val iconItemStack = run {
        val lore = run {
          val playerData = SeichiAssist.playermap[uniqueId]

          val base = List(
              s"$RESET${GREEN}現在の装備・アイテムを移動します。",
              s"${RESET}サーバー間のアイテム移動にご利用ください。",
              ""
          )

          val statusDisplay = if (playerData.contentsPresentInSharedInventory) {
            List(
                s"$RESET${GREEN}収納中",
                s"$RESET$DARK_RED${UNDERLINE}クリックでアイテムを取り出します。",
                s"$RESET${RED}現在の装備・アイテムが空であることを確認してください。"
            )
          } else {
            List(
                s"$RESET${GREEN}非収納中",
                s"$RESET$DARK_RED${UNDERLINE}クリックでアイテムを収納します。"
            )
          }

          base + statusDisplay
        }

        IconItemStackBuilder(Material.TRAPPED_CHEST)
            .title(s"$YELLOW$UNDERLINE${BOLD}インベントリ共有")
            .lore(lore)
            .build()
      }

      Button(iconItemStack, LeftClickButtonEffect("shareinv".asCommandEffect()))
    }
  }

  private @SuspendingMethod def computeMenuLayout(player: Player): IndexedSlotLayout = {
    import ConstantButtons._
    val computations = ButtonComputations(player)
    import computations._

    menuinventory.IndexedSlotLayout(
      0 -> officialWikiNavigationButton,
      1 -> rulesPageNavigationButton,
      2 -> serverMapNavigationButton,
      3 -> JMSNavigationButton,
      6 -> computeShareInventoryButton(),
      8 -> hubCommandButton,
      12 -> computeHeadSummoningButton(),
      13 -> computeBroadcastMessageToggleButton(),
      14 -> computeDeathMessageToggleButton(),
      15 -> computeWorldGuardMessageToggleButton(),
      27 -> CommonButtons.openStickMenu,
      30 -> recycleBinButton,
      34 -> titanConversionButton,
      35 -> appleConversionButton
    )
  }

  override val open: TargetedEffect[Player] = computedEffect { player =>
    val session = MenuInventoryView(4.rows(), s"${LIGHT_PURPLE}木の棒メニュー").createNewSession()

    sequentialEffect(
        session.openEffectThrough(Schedulers.sync),
        targetedeffect.UnfocusedEffect { session.overwriteViewWith(player.computeMenuLayout()) }
    )
  }
}

object StickMenu.secondPage: Menu
  get() = SecondPage