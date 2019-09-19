package com.github.unchama.seichiassist.menus.stickmenu

import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.seasonalevents.events.valentine.Valentine
import com.github.unchama.seichiassist.{CommonSoundEffects, SkullOwners}
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.{Material, Sound}
import org.bukkit.entity.Player

/**
 * 木の棒メニュー1ページ目
 *
 * @author karayuu
 */
private object FirstPage: Menu {
  private object ConstantButtons {
    val teleportServerButton = run {
      val buttonLore = listOf(
          "${GRAY}・各サバイバルサーバー",
          "${GRAY}・建築サーバー",
          "${GRAY}・公共施設サーバー",
          "${GRAY}間を移動する時に使います",
          "$DARK_RED${UNDERLINE}クリックして開く"
      )

      Button(
          IconItemStackBuilder(Material.NETHER_STAR)
              .title("$RED$UNDERLINE${BOLD}サーバー間移動メニューへ")
              .lore(buttonLore)
              .build(),
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_PORTAL_AMBIENT, 0.6f, 1.5f),
              // TODO メニューに置き換える
              TargetedEffect { it.openInventory(MenuInventoryData.getServerSwitchMenu(it)) }
          )
      )
    }

    val spawnCommandButton = run {
      val buttonLore = listOf(
          "${GRAY}・メインワールド",
          "${GRAY}・整地ワールド",
          "${GRAY}間を移動するときに使います",
          "$DARK_RED${UNDERLINE}クリックするとワープします",
          "${DARK_GRAY}command->[/spawn]"
      )

      Button(
          IconItemStackBuilder(Material.BEACON)
              .title("$YELLOW$UNDERLINE${BOLD}スポーンワールドへワープ")
              .lore(buttonLore)
              .build(),
          LeftClickButtonEffect(
              TargetedEffect { it.closeInventory() },
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              "spawn".asCommandEffect()
          )
      )
    }

    val achievementSystemButton = run {
      val buttonLore = listOf(
          "${GRAY}様々な実績に挑んで、",
          "${GRAY}いろんな二つ名を手に入れよう！",
          "$DARK_GRAY${UNDERLINE}クリックで設定画面へ移動"
      )

      Button(
          IconItemStackBuilder(Material.END_CRYSTAL)
              .title("$YELLOW$UNDERLINE${BOLD}実績・二つ名システム")
              .lore(buttonLore)
              .build(),
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
              // TODO メニューに置き換える
              TargetedEffect { it.openInventory(MenuInventoryData.getTitleMenuData(it)) }
          )
      )
    }

    val seichiGodRankingButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.COOKIE)
              .title("$YELLOW$UNDERLINE${BOLD}整地神ランキングを見る")
              .lore(listOf(
                  "$RESET$RED(整地神ランキング150位以内のプレイヤーのみ表記されます)",
                  "$RESET$DARK_RED${UNDERLINE}クリックで開く"
              ))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              // TODO メニューに置き換える
              TargetedEffect { it.openInventory(MenuInventoryData.getRankingList(0)) }
          )
      )
    }

    val loginGodRankingButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.COOKIE)
              .title("$YELLOW$UNDERLINE${BOLD}ログイン神ランキングを見る")
              .lore(listOf(
                  "$RESET$RED(ログイン神ランキング150位以内のプレイヤーのみ表記されます)",
                  "$RESET$DARK_RED${UNDERLINE}クリックで開く"))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              // TODO メニューに置き換える
              TargetedEffect { it.openInventory(MenuInventoryData.getRankingList_playtick(0)) }
          )
      )
    }

    val voteGodRankingButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.COOKIE)
              .title("$YELLOW$UNDERLINE${BOLD}投票神ランキングを見る")
              .lore(listOf(
                  "$RESET$RED(投票神ランキング150位以内のプレイヤーのみ表記されます)",
                  "$RESET$DARK_RED${UNDERLINE}クリックで開く"
              ))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              // TODO メニューに置き換える
              TargetedEffect { it.openInventory(MenuInventoryData.getRankingList_p_vote(0)) }
          )
      )
    }

    val secondPageButton = run {
      val iconItemStack =
          SkullItemStackBuilder(SkullOwners.MHF_ArrowRight)
              .title("$YELLOW$UNDERLINE${BOLD}2ページ目へ")
              .lore(listOf("$RESET$DARK_RED${UNDERLINE}クリックで移動"))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.secondPage.open
          )
      )
    }

    val gachaPrizeExchangeButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.NOTE_BLOCK)
              .title("$YELLOW$UNDERLINE${BOLD}不要ガチャ景品交換システム")
              .lore(listOf(
                  "$RESET${GREEN}不必要な当たり、大当たり景品を",
                  "$RESET${GREEN}ガチャ券と交換出来ます",
                  "$RESET${GREEN}出てきたインベントリ―に",
                  "$RESET${GREEN}交換したい景品を入れて",
                  "$RESET${GREEN}escキーを押してください",
                  "$RESET${DARK_GRAY}たまにアイテムが消失するから",
                  "$RESET${DARK_GRAY}大事なものはいれないでネ",
                  "$RESET$DARK_RED${UNDERLINE}クリックで開く"
              ))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
              // TODO メニューに置き換える
              TargetedEffect {
                it.openInventory(
                    createInventory(
                        size = 4.rows(),
                        title = "$LIGHT_PURPLE${BOLD}交換したい景品を入れてください"
                    )
                )
              }
          )
      )
    }

    val homePointMenuButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.BED)
              .title("$YELLOW$UNDERLINE${BOLD}ホームメニューを開く")
              .lore(listOf(
                  "$RESET${GRAY}ホームポイントに関するメニュー",
                  "$RESET$DARK_RED${UNDERLINE}クリックで開く"
              ))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 1.5f),
              TargetedEffect { it.openInventory(MenuInventoryData.getHomeMenuData(it)) }
          )
      )
    }

    val randomTeleportButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.COMPASS)
              .title("$YELLOW$UNDERLINE${BOLD}ランダムテレポート(β)")
              .lore(listOf(
                  "$RESET${GRAY}整地ワールドで使うと、良さげな土地にワープします",
                  "$RESET${GRAY}βテスト中のため、謎挙動にご注意ください",
                  "$RESET$DARK_RED${UNDERLINE}クリックで発動",
                  "$RESET${DARK_GRAY}command->[/rtp]"
              ))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 1.5f),
              "rtp".asCommandEffect()
          )
      )
    }

    val fastCraftButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.WORKBENCH)
              .title("$YELLOW$UNDERLINE${BOLD}FastCraft機能")
              .lore(listOf(
                  "$RESET$DARK_RED${UNDERLINE}クリックで開く",
                  "$RESET${RED}ただの作業台じゃないんです…",
                  "$RESET${YELLOW}自動レシピ補完機能付きの",
                  "$RESET${YELLOW}最強な作業台はこちら",
                  "$RESET${DARK_GRAY}command->[/fc craft]"
              ))
              .build()


      Button(
          iconItemStack,
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
              "fc craft".asCommandEffect()
          )
      )
    }

    val passiveSkillBookButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.ENCHANTED_BOOK)
              .enchanted()
              .title("$YELLOW$UNDERLINE${BOLD}パッシブスキルブック")
              .lore(listOf(
                  "$RESET${GRAY}整地に便利なスキルを使用できるゾ",
                  "$RESET$DARK_RED${UNDERLINE}クリックでスキル一覧を開く"
              ))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.8f),
              // TODO メニューに置き換える
              TargetedEffect { it.openInventory(MenuInventoryData.getPassiveSkillMenuData(it)) }
          )
      )
    }

    val oreExchangeButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.DIAMOND_ORE)
              .title("$YELLOW$UNDERLINE${BOLD}鉱石・交換券変換システム")
              .lore(listOf(
                  "$RESET${GREEN}不必要な各種鉱石を",
                  "$RESET${DARK_RED}交換券$RESET${GREEN}と交換できます",
                  "$RESET${GREEN}出てきたインベントリ―に",
                  "$RESET${GREEN}交換したい鉱石を入れて",
                  "$RESET${GREEN}escキーを押してください",
                  "$RESET${DARK_GRAY}たまにアイテムが消失するから",
                  "$RESET${DARK_GRAY}大事なものはいれないでネ",
                  "$RESET$DARK_RED${UNDERLINE}クリックで開く"
              ))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
              // TODO メニューに置き換える
              TargetedEffect {
                it.openInventory(
                    createInventory(
                        size = 4.rows(),
                        title = "$LIGHT_PURPLE${BOLD}交換したい鉱石を入れてください"
                    )
                )
              }
          )
      )
    }

    val votePointMenuButton = run {
      val iconItemStack =
          IconItemStackBuilder(Material.DIAMOND)
              .enchanted()
              .title("$YELLOW$UNDERLINE${BOLD}投票ptメニュー")
              .lore(listOf("$RESET${GREEN}投票ptに関することはこちらから！"))
              .build()

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              // TODO メニューに置き換える
              TargetedEffect { it.openInventory(MenuInventoryData.getVotingMenuData(it)) }
          )
      )
    }
  }

  private object ButtonComputations {
    suspend def Player.computeStatsButton(): Button = recomputedButton {
      val openerData = SeichiAssist.playermap[uniqueId]!!

      Button(
          SkullItemStackBuilder(uniqueId)
              .title("$YELLOW$BOLD$UNDERLINE${name}の統計データ")
              .lore(PlayerStatsLoreGenerator(openerData).computeLore())
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                openerData.toggleExpBarVisibility,
                deferredEffect {
                  val toggleSoundPitch = if (openerData.settings.isExpBarVisible) 1.0f else 0.5f
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, toggleSoundPitch)
                }
            )
          }
      )
    }

    suspend def Player.computeEffectSuppressionButton(): Button = recomputedButton {
      val openerData = SeichiAssist.playermap[uniqueId]!!

      val buttonLore: List<String> = run {
        val toggleNavigation = listOf(
            openerData.settings.fastDiggingEffectSuppression.currentStatus(),
            "$RESET$DARK_RED${UNDERLINE}クリックで" + openerData.settings.fastDiggingEffectSuppression.nextToggledStatus()
        )

        val explanation = listOf(
            "$RESET${GRAY}採掘速度上昇効果とは",
            "$RESET${GRAY}接続人数と1分間の採掘量に応じて",
            "$RESET${GRAY}採掘速度が変化するシステムです",
            "$RESET${GOLD}現在の採掘速度上昇Lv：${openerData.minespeedlv + 1}"
        )

        val effectStats =
            listOf("$RESET$YELLOW${UNDERLINE}上昇量の内訳") +
                openerData.effectdatalist.map { "$RESET$RED${it.effectDescription}" }

        toggleNavigation + explanation + effectStats
      }

      Button(
          IconItemStackBuilder(Material.DIAMOND_PICKAXE)
              .title("$YELLOW$UNDERLINE${BOLD}採掘速度上昇効果")
              .enchanted()
              .lore(buttonLore)
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                openerData.settings.fastDiggingEffectSuppression.suppressionDegreeToggleEffect,
                deferredEffect { openerData.computeFastDiggingEffect() }
            )
          }
      )
    }

    suspend def Player.computeRegionMenuButton(): Button {
      val buttonLore = run {
        val worldGuardPlugin = ExternalPlugins.getWorldGuard()
        val regionManager = worldGuardPlugin.getRegionManager(world)

        val maxRegionCount = WorldGuard.getMaxRegionCount(this, world)
        val currentPlayerRegionCount =
            regionManager.getRegionCountOfPlayer(worldGuardPlugin.wrapPlayer(this))

        listOf(
            "${GRAY}土地の保護が行えます",
            "$DARK_RED${UNDERLINE}クリックで開く",
            "${GRAY}保護作成上限：$AQUA$maxRegionCount",
            "${GRAY}現在のあなたの保護作成数：$AQUA$currentPlayerRegionCount"
        )
      }

      return Button(
          IconItemStackBuilder(Material.DIAMOND_AXE)
              .title("$YELLOW${UNDERLINE}土地保護メニュー")
              .lore(buttonLore)
              .build(),
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.5f),
              RegionMenu.open
          )
      )
    }

    suspend def Player.computeMineStackButton(): Button {
      val openerData = SeichiAssist.playermap[uniqueId]!!

      val minimumLevelRequired = SeichiAssist.seichiAssistConfig.getMineStacklevel(1)

      val buttonLore: List<String> = run {
        val explanation = listOf(
            "$RESET${GREEN}説明しよう!MineStackとは…",
            "${RESET}主要アイテムを無限にスタック出来る!",
            "${RESET}スタックしたアイテムは",
            "${RESET}ここから取り出せるゾ!"
        )

        val actionGuidance = if (openerData.level >= minimumLevelRequired) {
          "$RESET$DARK_GREEN${UNDERLINE}クリックで開く"
        } else {
          "$RESET$DARK_RED${UNDERLINE}整地レベルが${minimumLevelRequired}以上必要です"
        }

        val annotation = listOf(
            "$RESET${DARK_GRAY}※スタックしたアイテムは",
            "$RESET${DARK_GRAY}各サバイバルサーバー間で",
            "$RESET${DARK_GRAY}共有されます"
        )

        explanation + actionGuidance + annotation
      }

      return Button(
          IconItemStackBuilder(Material.CHEST)
              .title("$YELLOW$UNDERLINE${BOLD}MineStack機能")
              .lore(buttonLore)
              .build(),
          LeftClickButtonEffect {
            if (openerData.level >= minimumLevelRequired) {
              sequentialEffect(
                  FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
                  MineStackMainMenu.open
              )
            } else FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)
          }
      )
    }

    suspend def Player.computePocketOpenButton(): Button {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val minimumRequiredLevel = SeichiAssist.seichiAssistConfig.passivePortalInventorylevel

      val iconItemStack = run {
        val loreAnnotation = listOf(
            "$RESET${DARK_GRAY}※4次元ポケットの中身は",
            "$RESET${DARK_GRAY}各サバイバルサーバー間で",
            "$RESET${DARK_GRAY}共有されます"
        )
        val loreHeading = if (playerData.level >= minimumRequiredLevel) {
          listOf(
              "$RESET${GRAY}ポケットサイズ:${playerData.pocketInventory.size}スタック",
              "$RESET$DARK_GREEN${UNDERLINE}クリックで開く"
          )
        } else {
          listOf("$RESET$DARK_RED${UNDERLINE}整地レベルが${minimumRequiredLevel}以上必要です")
        }

        IconItemStackBuilder(Material.ENDER_PORTAL_FRAME)
            .title("$YELLOW$UNDERLINE${BOLD}4次元ポケットを開く")
            .lore(loreHeading + loreAnnotation)
            .build()
      }

      return Button(
          iconItemStack,
          LeftClickButtonEffect(
              deferredEffect {
                if (playerData.level >= minimumRequiredLevel) {
                  sequentialEffect(
                    FocusedSoundEffect(Sound.BLOCK_ENDERCHEST_OPEN, 1.0f, 0.1f),
                    TargetedEffect { it.openInventory(playerData.pocketInventory) }
                  )
                } else FocusedSoundEffect(Sound.BLOCK_GRASS_PLACE, 1.0f, 0.1f)
              }
          )
      )
    }

    suspend def Player.computeEnderChestButton(): Button {
      val playerData = SeichiAssist.playermap[uniqueId]!!
      val minimumRequiredLevel = SeichiAssist.seichiAssistConfig.passivePortalInventorylevel

      val iconItemStack = run {
        val loreHeading = run {
          if (playerData.level >= minimumRequiredLevel) {
            "$RESET$DARK_GREEN${UNDERLINE}クリックで開く"
          } else {
            "$RESET$DARK_RED${UNDERLINE}整地レベルが${minimumRequiredLevel}以上必要です"
          }
        }

        IconItemStackBuilder(Material.ENDER_CHEST)
            .title("$DARK_PURPLE$UNDERLINE${BOLD}どこでもエンダーチェスト")
            .lore(listOf(loreHeading))
            .build()
      }

      return Button(
          iconItemStack,
          LeftClickButtonEffect(
              deferredEffect {
                if (playerData.level >= minimumRequiredLevel) {
                  sequentialEffect(
                      FocusedSoundEffect(Sound.BLOCK_ENDERCHEST_OPEN, 1.0f, 1.0f),
                      TargetedEffect { it.openInventory(player.enderChest) }
                  )
                } else {
                  FocusedSoundEffect(Sound.BLOCK_GRASS_PLACE, 1.0f, 0.1f)
                }
              }
          )
      )
    }

    suspend def Player.computeApologyItemsButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val lore = run {
          val explanation = listOf(
              "$RESET${GRAY}運営からのガチャ券を受け取ります",
              "$RESET${GRAY}以下の場合に配布されます",
              "$RESET${GRAY}・各種不具合のお詫びとして",
              "$RESET${GRAY}・イベント景品として",
              "$RESET${GRAY}・各種謝礼として"
          )

          val obtainableApologyItems = playerData.unclaimedApologyItems
          val currentStatus =
              if (obtainableApologyItems != 0)
                "$RESET${AQUA}未獲得ガチャ券：${obtainableApologyItems}枚"
              else
                "$RESET${RED}獲得できるガチャ券はありません"

          explanation + currentStatus
        }

        SkullItemStackBuilder(SkullOwners.whitecat_haru)
            .title("$DARK_AQUA$UNDERLINE${BOLD}運営からのガチャ券を受け取る")
            .lore(lore)
            .build()
      }

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              computedEffect {
                val numberOfItemsToGive = SeichiAssist.databaseGateway.playerDataManipulator.givePlayerBug(this, playerData)

                if (numberOfItemsToGive != 0) {
                  val itemToGive = Util.getForBugskull(this.name)

                  sequentialEffect(
                      unfocusedEffect {
                        repeat(numberOfItemsToGive) { Util.addItemToPlayerSafely(this, itemToGive) }
                        playerData.unclaimedApologyItems = 0
                      },
                      FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f),
                      "${GREEN}運営チームから${numberOfItemsToGive}枚の${GOLD}ガチャ券${WHITE}を受け取りました".asMessageEffect()
                  )
                } else EmptyEffect
              }
          )
      )
    }

    suspend def Player.computeStarLevelStatsButton(): Button {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val breakNumRequiredToNextStarLevel =
            (playerData.starLevels.fromBreakAmount.toLong() + 1) * 87115000 - playerData.totalbreaknum

        val lore = listOf(
            "$RESET$AQUA${BOLD}整地量：☆${playerData.starLevels.fromBreakAmount}",
            "$RESET${AQUA}次の☆まで：あと$breakNumRequiredToNextStarLevel",
            "$RESET$GREEN$UNDERLINE${BOLD}合計：☆${playerData.starLevels.total()}"
        )

        IconItemStackBuilder(Material.GOLD_INGOT)
            .title("$YELLOW$UNDERLINE${BOLD}スターレベル情報")
            .lore(lore)
            .build()
      }

      return Button(iconItemStack)
    }

    suspend def Player.computeActiveSkillButton(): Button {
      val iconItemStack = run {
        val lore =
            if (Util.isSkillEnable(this))
              listOf(
                  "$RESET${GRAY}整地に便利なスキルを使用できるゾ",
                  "$RESET$DARK_RED${UNDERLINE}クリックでスキル一覧を開く"
              )
            else
              listOf(
                  "$RESET${RED}このワールドでは",
                  "$RESET${RED}整地スキルを使えません"
              )

        IconItemStackBuilder(Material.ENCHANTED_BOOK)
            .enchanted()
            .title("$YELLOW$UNDERLINE${BOLD}アクティブスキルブック")
            .lore(lore)
            .build()
      }

      return Button(
          iconItemStack,
          LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.8f),
              // TODO メニューに置き換える
              TargetedEffect { it.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(it)) }
          )
      )
    }

    suspend def Player.computeGachaTicketButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val lore = run {
          val obtainableGachaTicket = playerData.gachapoint / 1000
          val gachaPointToNextTicket = 1000 - playerData.gachapoint % 1000

          val gachaTicketStatus = if (obtainableGachaTicket != 0)
            "$RESET${AQUA}未獲得ガチャ券：${obtainableGachaTicket}枚"
          else
            "$RESET${RED}獲得できるガチャ券はありません"

          val gachaPointStatus = "$RESET${AQUA}次のガチャ券まで:${gachaPointToNextTicket}ブロック"

          listOf(gachaTicketStatus, gachaPointStatus)
        }

        SkullItemStackBuilder(SkullOwners.unchama)
            .title("$DARK_AQUA$UNDERLINE${BOLD}整地報酬ガチャ券を受け取る")
            .lore(lore)
            .build()
      }

      Button(
          iconItemStack,
          LeftClickButtonEffect {
            if (playerData.gachacooldownflag) {
              CoolDownTask(this@computeGachaTicketButton, false, false, true).runTaskLater(SeichiAssist.instance, 20)

              val gachaPointPerTicket = SeichiAssist.seichiAssistConfig.gachaPresentInterval
              val gachaTicketsToGive = min(playerData.gachapoint / gachaPointPerTicket, 576)

              val itemStackToGive = Util.getskull(this@computeGachaTicketButton.name)

              if (gachaTicketsToGive > 0) {
                sequentialEffect(
                    unfocusedEffect {
                      playerData.gachapoint -= gachaPointPerTicket * gachaTicketsToGive
                      repeat(gachaTicketsToGive) { Util.addItemToPlayerSafely(this@computeGachaTicketButton, itemStackToGive) }
                    },
                    "${ChatColor.GOLD}ガチャ券${gachaTicketsToGive}枚${ChatColor.WHITE}プレゼントフォーユー".asMessageEffect(),
                    FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
                )
              } else EmptyEffect
            } else EmptyEffect
          }
      )
    }

    suspend def Player.computeGachaTicketDeliveryButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val lore = run {
          val settingsStatus =
              if (playerData.settings.receiveGachaTicketEveryMinute)
                "$RESET${GREEN}毎分受け取ります"
              else
                "$RESET${RED}後でまとめて受け取ります"

          val navigationMessage = "$RESET$DARK_RED${UNDERLINE}クリックで変更"

          listOf(settingsStatus, navigationMessage)
        }

        IconItemStackBuilder(Material.STONE_BUTTON)
            .title("$YELLOW$UNDERLINE${BOLD}整地報酬ガチャ券受け取り方法")
            .lore(lore)
            .build()
      }

      Button(
          iconItemStack,
          LeftClickButtonEffect(
              unfocusedEffect {
                playerData.settings.receiveGachaTicketEveryMinute = !playerData.settings.receiveGachaTicketEveryMinute
              },
              deferredEffect {
                if (playerData.settings.receiveGachaTicketEveryMinute) {
                  sequentialEffect(
                      "${ChatColor.GREEN}毎分のガチャ券受け取り:ON".asMessageEffect(),
                      FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                  )
                } else {
                  sequentialEffect(
                      "${ChatColor.RED}毎分のガチャ券受け取り:OFF".asMessageEffect(),
                      "${ChatColor.GREEN}ガチャ券受け取りボタンを押すともらえます".asMessageEffect(),
                      FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1.0f, 1.0f)
                  )
                }
              }
          )
      )
    }

    suspend def Player.computeValentineChocolateButton(): Button = recomputedButton {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      if (playerData.hasChocoGave && Valentine.isInEvent) {
        val iconItemStack =
            IconItemStackBuilder(Material.TRAPPED_CHEST)
                .enchanted()
                .title("プレゼントボックス")
                .lore(listOf(
                    "$RESET$RED<バレンタインイベント記念>",
                    "$RESET${AQUA}記念品として",
                    "$RESET${GREEN}チョコチップクッキー×64個",
                    "$RESET${AQUA}を配布します。",
                    "$RESET$DARK_RED$UNDERLINE${BOLD}クリックで受け取る"
                ))
                .build()

        Button(
            iconItemStack,
            LeftClickButtonEffect(
                deferredEffect {
                  if (Valentine.isInEvent) {
                    sequentialEffect(
                        FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f),
                        unfocusedEffect {
                          Valentine.giveChoco(this)
                          playerData.hasChocoGave = true
                        },
                        "${ChatColor.AQUA}チョコチップクッキーを付与しました。".asMessageEffect()
                    )
                  } else {
                    EmptyEffect
                  }
                }
            )
        )
      } else {
        Button(ItemStack(Material.AIR))
      }
    }
  }

  private suspend def Player.computeMenuLayout(): IndexedSlotLayout =
      with(ConstantButtons) {
        with(ButtonComputations) {
          IndexedSlotLayout(
              0 to computeStatsButton(),
              1 to computeEffectSuppressionButton(),
              3 to computeRegionMenuButton(),
              5 to computeValentineChocolateButton(),
              7 to teleportServerButton,
              8 to spawnCommandButton,
              9 to achievementSystemButton,
              10 to computeStarLevelStatsButton(),
              11 to passiveSkillBookButton,
              13 to computeActiveSkillButton(),
              16 to gachaPrizeExchangeButton,
              17 to oreExchangeButton,
              18 to homePointMenuButton,
              19 to randomTeleportButton,
              21 to computePocketOpenButton(),
              22 to computeEnderChestButton(),
              23 to fastCraftButton,
              24 to computeMineStackButton(),
              27 to computeGachaTicketButton(),
              28 to computeGachaTicketDeliveryButton(),
              29 to computeApologyItemsButton(),
              30 to votePointMenuButton,
              32 to seichiGodRankingButton,
              33 to loginGodRankingButton,
              34 to voteGodRankingButton,
              35 to secondPageButton
          )
        }
      }

  override val open: TargetedEffect<Player> = computedEffect { player ->
    val session = MenuInventoryView(4.rows(), "${LIGHT_PURPLE}木の棒メニュー").createNewSession()

    sequentialEffect(
        session.openEffectThrough(Schedulers.sync),
        unfocusedEffect { session.overwriteViewWith(player.computeMenuLayout()) }
    )
  }
}

@Suppress("unused")
val StickMenu.firstPage: Menu
  get() = FirstPage
