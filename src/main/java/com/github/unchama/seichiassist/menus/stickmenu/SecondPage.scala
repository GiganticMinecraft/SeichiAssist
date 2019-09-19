package com.github.unchama.seichiassist.menus.stickmenu

import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.seasonalevents.events.valentine.Valentine
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Material, Sound}

/**
 * 木の棒メニュー2ページ目
 */
object SecondPage: Menu {
  private object ConstantButtons {
    val officialWikiNavigationButton: Button = run {
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

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                "$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("official")}".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
            )
          }
      )
    }

    val rulesPageNavigationButton: Button = run {
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

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                "$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("rule")}".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
            )
          }
      )
    }

    val serverMapNavigationButton: Button = run {
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

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                "$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("map")}".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
            )
          }
      )
    }

    val JMSNavigationButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.SIGN)
          .title("$YELLOW$UNDERLINE${BOLD}JapanMinecraftServerリンク")
          .lore(listOf(
              "$RESET${DARK_GRAY}クリックするとチャット欄に",
              "$RESET${DARK_GRAY}URLが表示されますので",
              "$RESET${DARK_GRAY}Tキーを押してから",
              "$RESET${DARK_GRAY}そのURLをクリックしてください"
          ))
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                "$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("jms")}".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
            )
          }
      )
    }

    val appleConversionButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.GOLDEN_APPLE, durability = 1)
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

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
                TargetedEffect {
                  // TODO メニューインベントリに差し替える
                  it.openInventory(createInventory(size = 4.rows(), title ="$GOLD${BOLD}椎名林檎と交換したい景品を入れてネ"))
                }
            )
          }
      )
    }

    val titanConversionButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.DIAMOND_AXE, durability = 1)
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
          .unbreakable()
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
                TargetedEffect {
                  // TODO メニューインベントリに差し替える
                  it.openInventory(createInventory(size = 4.rows(), title ="$GOLD${BOLD}修繕したい限定タイタンを入れてネ"))
                }
            )
          }
      )
    }

    val recycleBinButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.BUCKET)
          .title("$YELLOW$UNDERLINE${BOLD}ゴミ箱を開く")
          .lore(listOf(
              "$RESET${GREEN}不用品の大量処分にドウゾ！",
              "$RESET${RED}復活しないので取扱注意",
              "$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 1.5f),
                TargetedEffect {
                  // TODO メニューインベントリに差し替える
                  it.openInventory(createInventory(size = 4.rows(), title = "$RED${BOLD}ゴミ箱(取扱注意)"))
                }
            )
          }
      )
    }

    val hubCommandButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.NETHER_STAR)
          .title("$YELLOW$UNDERLINE${BOLD}ロビーサーバーへ移動")
          .lore(listOf(
              "$RESET$DARK_RED${UNDERLINE}クリックすると移動します",
              "$RESET${DARK_GRAY}command->[/hub]"
          ))
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                closeInventoryEffect,
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
                "hub".asCommandEffect()
            )
          }
      )
    }
  }

  private object ButtonComputations {
    suspend def Player.computeHeadSummoningButton(): Button = recomputedButton {
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

        SkullItemStackBuilder(SkullOwners.MHF_Villager)
            .title("$YELLOW$UNDERLINE${BOLD}自分の頭を召喚")
            .lore(baseLore + actionNavigation)
            .build()
      }

      val effect = FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
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
                    UnfocusedEffect { Util.dropItem(it, skullToGive) },
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
        )
      }

      Button(iconItemStack, effect)
    }

    suspend def Player.computeBroadcastMessageToggleButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]!!
      val iconItemStack = run {
        val currentSettings = playerData.settings.getBroadcastMutingSettings()

        val soundConfigurationState =
            if (currentSettings.shouldMuteSounds()) {
              "$RESET${GREEN}全体通知音:消音しない"
            } else {
              "$RESET${RED}全体通知音:消音する"
            }

        val messageConfigurationState =
            if (currentSettings.shouldMuteMessages()) {
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

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                playerData.settings.toggleBroadcastMutingSettings,
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
                deferredEffect {
                  when (playerData.settings.getBroadcastMutingSettings()) {
                    RECEIVE_MESSAGE_AND_SOUND -> "${GREEN}非表示/消音設定を解除しました"
                    RECEIVE_MESSAGE_ONLY -> "${RED}消音可能な全体通知音を消音します"
                    MUTE_MESSAGE_AND_SOUND -> "${RED}非表示可能な全体メッセージを非表示にします"
                  }.asMessageEffect()
                }
            )
          }
      )
    }

    suspend def Player.computeDeathMessageToggleButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val baseBuilder =
            IconItemStackBuilder(Material.FLINT_AND_STEEL)
                .title("$YELLOW$UNDERLINE${BOLD}死亡メッセージ表示切替")

        if (playerData.settings.shouldDisplayDeathMessages) {
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

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                playerData.settings.toggleDeathMessageMutingSettings,
                deferredEffect {
                  val (soundPitch, message) =
                      if (playerData.settings.shouldDisplayDeathMessages)
                        Pair(1.0f, "${GREEN}死亡メッセージ:表示")
                      else
                        Pair(0.5f, "${RED}死亡メッセージ:隠す")

                  sequentialEffect(
                      message.asMessageEffect(),
                      FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch)
                  )
                }
            )
          }
      )
    }

    suspend def Player.computeWorldGuardMessageToggleButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val baseBuilder = IconItemStackBuilder(Material.BARRIER)
            .title("$YELLOW$UNDERLINE${BOLD}ワールドガード保護メッセージ表示切替")

        val loreHeading = "$RESET${GRAY}スキル使用時のワールドガード保護警告メッセージ"

        if (playerData.settings.shouldDisplayWorldGuardLogs) {
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

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                playerData.settings.toggleWorldGuardLogEffect,
                deferredEffect {
                  val (soundPitch, message) =
                      if (playerData.settings.shouldDisplayWorldGuardLogs)
                        Pair(1.0f, "${ChatColor.GREEN}ワールドガード保護メッセージ:表示")
                      else
                        Pair(0.5f, "${ChatColor.RED}ワールドガード保護メッセージ:隠す")

                  sequentialEffect(
                      FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch),
                      message.asMessageEffect()
                  )
                }
            )
          }
      )
    }

    suspend def Player.computeShareInventoryButton(): Button = recomputedButton {
      val iconItemStack = run {
        val lore = run {
          val playerData = SeichiAssist.playermap[uniqueId]!!

          val base = listOf(
              "$RESET${GREEN}現在の装備・アイテムを移動します。",
              "${RESET}サーバー間のアイテム移動にご利用ください。",
              ""
          )

          val statusDisplay = if (playerData.contentsPresentInSharedInventory) {
            listOf(
                "$RESET${GREEN}収納中",
                "$RESET$DARK_RED${UNDERLINE}クリックでアイテムを取り出します。",
                "$RESET${RED}現在の装備・アイテムが空であることを確認してください。"
            )
          } else {
            listOf(
                "$RESET${GREEN}非収納中",
                "$RESET$DARK_RED${UNDERLINE}クリックでアイテムを収納します。"
            )
          }

          base + statusDisplay
        }

        IconItemStackBuilder(Material.TRAPPED_CHEST)
            .title("$YELLOW$UNDERLINE${BOLD}インベントリ共有")
            .lore(lore)
            .build()
      }

      Button(iconItemStack, LeftClickButtonEffect("shareinv".asCommandEffect()))
    }
  }

  private suspend def Player.computeMenuLayout(): IndexedSlotLayout =
      with(ConstantButtons) {
        with(ButtonComputations) {
          IndexedSlotLayout(
              0 to officialWikiNavigationButton,
              1 to rulesPageNavigationButton,
              2 to serverMapNavigationButton,
              3 to JMSNavigationButton,
              6 to computeShareInventoryButton(),
              8 to hubCommandButton,
              12 to computeHeadSummoningButton(),
              13 to computeBroadcastMessageToggleButton(),
              14 to computeDeathMessageToggleButton(),
              15 to computeWorldGuardMessageToggleButton(),
              27 to CommonButtons.openStickMenu,
              30 to recycleBinButton,
              34 to titanConversionButton,
              35 to appleConversionButton
          )
        }
      }

  override val open: TargetedEffect<Player> = computedEffect { player ->
    val session = MenuInventoryView(4.rows(), "${LIGHT_PURPLE}木の棒メニュー").createNewSession()

    sequentialEffect(
        session.openEffectThrough(Schedulers.sync),
        UnfocusedEffect { session.overwriteViewWith(player.computeMenuLayout()) }
    )
  }
}

@Suppress("unused")
val StickMenu.secondPage: Menu
  get() = SecondPage