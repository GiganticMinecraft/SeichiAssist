package com.github.unchama.seichiassist.menus.stickmenu

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory._
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect, LeftClickButtonEffect}
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton, action}
import com.github.unchama.seasonalevents.events.valentine.Valentine
import com.github.unchama.seichiassist.data.descrptions.PlayerStatsLoreGenerator
import com.github.unchama.seichiassist.data.{ActiveSkillInventoryData, MenuInventoryData}
import com.github.unchama.seichiassist.menus.achievement.AchievementMenu
import com.github.unchama.seichiassist.menus.minestack.MineStackMainMenu
import com.github.unchama.seichiassist.menus.skill.PassiveSkillMenu
import com.github.unchama.seichiassist.menus.{CommonButtons, HomeMenu, RegionMenu, ServerSwitchMenu}
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.external.{ExternalPlugins, WorldGuardWrapper}
import com.github.unchama.seichiassist.{CommonSoundEffects, SeichiAssist, SkullOwners}
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.InventoryUtil
import org.bukkit.ChatColor.{DARK_RED, RESET, _}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

/**
 * 木の棒メニュー1ページ目
 *
 * @author karayuu
 */
object FirstPage extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}
  import com.github.unchama.targetedeffect.player.PlayerEffects._
  import com.github.unchama.targetedeffect.syntax._
  import eu.timepit.refined.auto._

  override val frame: MenuFrame =
    MenuFrame(4.chestRows, s"${LIGHT_PURPLE}木の棒メニュー")

  import com.github.unchama.targetedeffect._

  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
    import ConstantButtons._
    val computations = ButtonComputations(player)
    import computations._

    val constantPart = Map(
      ChestSlotRef(0, 7) -> teleportServerButton,
      ChestSlotRef(0, 8) -> spawnCommandButton,
      ChestSlotRef(1, 0) -> achievementSystemButton,
      ChestSlotRef(1, 2) -> passiveSkillBookButton,
      ChestSlotRef(1, 7) -> gachaPrizeExchangeButton,
      ChestSlotRef(1, 8) -> oreExchangeButton,
      ChestSlotRef(2, 0) -> homePointMenuButton,
      ChestSlotRef(2, 1) -> randomTeleportButton,
      ChestSlotRef(2, 5) -> fastCraftButton,
      ChestSlotRef(3, 3) -> votePointMenuButton,
      ChestSlotRef(3, 5) -> seichiGodRankingButton,
      ChestSlotRef(3, 6) -> loginGodRankingButton,
      ChestSlotRef(3, 7) -> voteGodRankingButton,
      ChestSlotRef(3, 8) -> secondPageButton
    )

    import cats.implicits._

    val dynamicPartComputation =
      List(
        ChestSlotRef(0, 0) -> computeStatsButton,
        ChestSlotRef(0, 1) -> computeEffectSuppressionButton,
        ChestSlotRef(0, 3) -> computeRegionMenuButton,
        ChestSlotRef(0, 5) -> computeValentineChocolateButton,
        ChestSlotRef(1, 1) -> computeStarLevelStatsButton,
        ChestSlotRef(1, 4) -> computeActiveSkillButton,
        ChestSlotRef(2, 3) -> computePocketOpenButton,
        ChestSlotRef(2, 4) -> computeEnderChestButton,
        ChestSlotRef(2, 6) -> computeMineStackButton,
        ChestSlotRef(3, 0) -> computeGachaTicketButton,
        ChestSlotRef(3, 1) -> computeGachaTicketDeliveryButton,
        ChestSlotRef(3, 2) -> computeApologyItemsButton,
      )
        .map(_.sequence)
        .sequence

    for {
      dynamicPart <- dynamicPartComputation
    } yield MenuSlotLayout(constantPart ++ dynamicPart.toMap)
  }

  private case class ButtonComputations(player: Player) {

    import player._

    val computeStatsButton: IO[Button] = RecomputedButton {
      val openerData = SeichiAssist.playermap(getUniqueId)

      for {
        lore <- new PlayerStatsLoreGenerator(openerData).computeLore()
      } yield Button(
        new SkullItemStackBuilder(getUniqueId)
          .title(s"$YELLOW$BOLD$UNDERLINE${getName}の統計データ")
          .lore(lore)
          .build(),
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          sequentialEffect(
            openerData.toggleExpBarVisibility,
            deferredEffect(IO {
              val toggleSoundPitch = if (openerData.settings.isExpBarVisible) 1.0f else 0.5f
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, toggleSoundPitch)
            })
          )
        }
      )
    }

    val computeEffectSuppressionButton: IO[Button] = RecomputedButton {
      val openerData = SeichiAssist.playermap(getUniqueId)

      val computeButtonLore: IO[List[String]] = for {
        toggleNavigation <- for {
          currentStatus <- openerData.settings.fastDiggingEffectSuppression.currentStatus()
          nextStatus <- openerData.settings.fastDiggingEffectSuppression.nextToggledStatus()
        } yield
          List(
            currentStatus,
            s"$RESET$DARK_RED${UNDERLINE}クリックで" + nextStatus
          )
      } yield {
        val explanation = List(
          s"$RESET${GRAY}採掘速度上昇効果とは",
          s"$RESET${GRAY}接続人数と1分間の採掘量に応じて",
          s"$RESET${GRAY}採掘速度が変化するシステムです",
          s"$RESET${GOLD}現在の採掘速度上昇Lv：${openerData.minespeedlv + 1}"
        )

        val effectStats =
          List(s"$RESET$YELLOW${UNDERLINE}上昇量の内訳") ++
            openerData.effectdatalist.map(effect => s"$RESET$RED${effect.effectDescription}")

        toggleNavigation ++ explanation ++ effectStats
      }

      for {
        buttonLore <- computeButtonLore
      } yield
        Button(
          new IconItemStackBuilder(Material.DIAMOND_PICKAXE)
            .title(s"$YELLOW$UNDERLINE${BOLD}採掘速度上昇効果")
            .enchanted()
            .lore(buttonLore)
            .build(),
          action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
            sequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              openerData.settings.fastDiggingEffectSuppression.suppressionDegreeToggleEffect,
              deferredEffect[IO, Player, Unit](openerData.computeFastDiggingEffect)
            )
          }
        )
    }

    val computeRegionMenuButton: IO[Button] = IO {
      val buttonLore = {
        val worldGuardPlugin = ExternalPlugins.getWorldGuard
        val regionManager = worldGuardPlugin.getRegionManager(getWorld)

        val maxRegionCount = WorldGuardWrapper.getMaxRegionCount(player, getWorld)
        val currentPlayerRegionCount =
          regionManager.getRegionCountOfPlayer(worldGuardPlugin.wrapPlayer(player))

        List(
          s"${GRAY}土地の保護が行えます",
          s"$DARK_RED${UNDERLINE}クリックで開く",
          s"${GRAY}保護作成上限：$AQUA$maxRegionCount",
          s"${GRAY}現在のあなたの保護作成数：$AQUA$currentPlayerRegionCount"
        )
      }

      Button(
        new IconItemStackBuilder(Material.DIAMOND_AXE)
          .title(s"$YELLOW${UNDERLINE}土地保護メニュー")
          .lore(buttonLore)
          .build(),
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.5f),
          RegionMenu.open
        )
      )
    }

    val computeMineStackButton: IO[Button] = IO {
      val openerData = SeichiAssist.playermap(getUniqueId)

      val minimumLevelRequired = SeichiAssist.seichiAssistConfig.getMineStacklevel(1)

      val buttonLore: List[String] = {
        val explanation = List(
          s"$RESET${GREEN}説明しよう!MineStackとは…",
          s"${RESET}主要アイテムを無限にスタック出来る!",
          s"${RESET}スタックしたアイテムは",
          s"${RESET}ここから取り出せるゾ!"
        )

        val actionGuidance = if (openerData.level >= minimumLevelRequired) {
          s"$RESET$DARK_GREEN${UNDERLINE}クリックで開く"
        } else {
          s"$RESET$DARK_RED${UNDERLINE}整地レベルが${minimumLevelRequired}以上必要です"
        }

        val annotation = List(
          s"$RESET$DARK_GRAY※スタックしたアイテムは",
          s"$RESET${DARK_GRAY}各サバイバルサーバー間で",
          s"$RESET${DARK_GRAY}共有されます"
        )

        explanation ++ List(actionGuidance) ++ annotation
      }

      Button(
        new IconItemStackBuilder(Material.CHEST)
          .title(s"$YELLOW$UNDERLINE${BOLD}MineStack機能")
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

    val computePocketOpenButton: IO[Button] = IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      val minimumRequiredLevel = SeichiAssist.seichiAssistConfig.getPassivePortalInventorylevel

      val iconItemStack = {
        val loreAnnotation = List(
          s"$RESET$DARK_GRAY※4次元ポケットの中身は",
          s"$RESET${DARK_GRAY}各サバイバルサーバー間で",
          s"$RESET${DARK_GRAY}共有されます"
        )
        val loreHeading = if (playerData.level >= minimumRequiredLevel) {
          List(
            s"$RESET${GRAY}ポケットサイズ:${playerData.pocketInventory.getSize}スタック",
            s"$RESET$DARK_GREEN${UNDERLINE}クリックで開く"
          )
        } else {
          List(s"$RESET$DARK_RED${UNDERLINE}整地レベルが${minimumRequiredLevel}以上必要です")
        }

        new IconItemStackBuilder(Material.ENDER_PORTAL_FRAME)
          .title(s"$YELLOW$UNDERLINE${BOLD}4次元ポケットを開く")
          .lore(loreHeading ++ loreAnnotation)
          .build()
      }

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          deferredEffect(IO {
            if (playerData.level >= minimumRequiredLevel)
              sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_ENDERCHEST_OPEN, 1.0f, 0.1f),
                openInventoryEffect(playerData.pocketInventory),
              ) else FocusedSoundEffect(Sound.BLOCK_GRASS_PLACE, 1.0f, 0.1f)
          })
        )
      )
    }

    val computeEnderChestButton: IO[Button] = IO {
      val playerData = SeichiAssist.playermap(getUniqueId)
      val minimumRequiredLevel = SeichiAssist.seichiAssistConfig.getDokodemoEnderlevel

      val iconItemStack = {
        val loreHeading = {
          if (playerData.level >= minimumRequiredLevel) {
            s"$RESET$DARK_GREEN${UNDERLINE}クリックで開く"
          } else {
            s"$RESET$DARK_RED${UNDERLINE}整地レベルが${minimumRequiredLevel}以上必要です"
          }
        }

        new IconItemStackBuilder(Material.ENDER_CHEST)
          .title(s"$DARK_PURPLE$UNDERLINE${BOLD}どこでもエンダーチェスト")
          .lore(List(loreHeading))
          .build()
      }

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          deferredEffect(IO {
            if (playerData.level >= minimumRequiredLevel) {
              sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_ENDERCHEST_OPEN, 1.0f, 1.0f),
                openInventoryEffect(player.getEnderChest)
              )
            } else {
              FocusedSoundEffect(Sound.BLOCK_GRASS_PLACE, 1.0f, 0.1f)
            }
          })
        )
      )
    }

    val computeApologyItemsButton: IO[Button] = RecomputedButton(IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      val iconItemStack = {
        val lore = {
          val explanation = List(
            s"$RESET${GRAY}運営からのガチャ券を受け取ります",
            s"$RESET${GRAY}以下の場合に配布されます",
            s"$RESET$GRAY・各種不具合のお詫びとして",
            s"$RESET$GRAY・イベント景品として",
            s"$RESET$GRAY・各種謝礼として"
          )

          val obtainableApologyItems = playerData.unclaimedApologyItems
          val currentStatus =
            if (obtainableApologyItems != 0)
              s"$RESET${AQUA}未獲得ガチャ券：${obtainableApologyItems}枚"
            else
              s"$RESET${RED}獲得できるガチャ券はありません"

          explanation.appended(currentStatus)
        }

        new SkullItemStackBuilder(SkullOwners.whitecat_haru)
          .title(s"$DARK_AQUA$UNDERLINE${BOLD}運営からのガチャ券を受け取る")
          .lore(lore)
          .build()
      }

      Button(
        iconItemStack,
        LeftClickButtonEffect(deferredEffect(IO {
          val numberOfItemsToGive = SeichiAssist.databaseGateway.playerDataManipulator.givePlayerBug(player, playerData)

          if (numberOfItemsToGive != 0) {
            val itemToGive = Util.getForBugskull(player.getName)
            val itemStacksToGive = Seq.fill(numberOfItemsToGive)(itemToGive)

            sequentialEffect(
              Util.grantItemStacksEffect(itemStacksToGive: _*),
              UnfocusedEffect { playerData.unclaimedApologyItems -= numberOfItemsToGive },
              FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f),
              s"${GREEN}運営チームから${numberOfItemsToGive}枚の${GOLD}ガチャ券${WHITE}を受け取りました".asMessageEffect()
            )
          } else emptyEffect
        }))
      )
    })

    val computeStarLevelStatsButton: IO[Button] = IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      val iconItemStack = {
        val breakNumRequiredToNextStarLevel =
          (playerData.starLevels.fromBreakAmount.toLong + 1) * 87115000 - playerData.totalbreaknum

        val lore = List(
          s"$RESET$AQUA${BOLD}整地量：☆${playerData.starLevels.fromBreakAmount}",
          s"$RESET${AQUA}次の☆まで：あと$breakNumRequiredToNextStarLevel",
          s"$RESET$GREEN$UNDERLINE${BOLD}合計：☆${playerData.starLevels.total()}"
        )

        new IconItemStackBuilder(Material.GOLD_INGOT)
          .title(s"$YELLOW$UNDERLINE${BOLD}スターレベル情報")
          .lore(lore)
          .build()
      }

      Button(iconItemStack)
    }

    val computeActiveSkillButton: IO[Button] = IO {
      val iconItemStack = {
        val lore =
          if (Util.isSkillEnable(player))
            List(
              s"$RESET${GRAY}整地に便利なスキルを使用できるゾ",
              s"$RESET$DARK_RED${UNDERLINE}クリックでスキル一覧を開く"
            )
          else
            List(
              s"$RESET${RED}このワールドでは",
              s"$RESET${RED}整地スキルを使えません"
            )

        new IconItemStackBuilder(Material.ENCHANTED_BOOK)
          .enchanted()
          .title(s"$YELLOW$UNDERLINE${BOLD}アクティブスキルブック")
          .lore(lore)
          .build()
      }

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.8f),
          // TODO メニューに置き換える
          openInventoryEffect(ActiveSkillInventoryData.getActiveSkillMenuData(player)),
        )
      )
    }

    val computeGachaTicketButton: IO[Button] = RecomputedButton(IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      val iconItemStack = {
        val lore = {
          val obtainableGachaTicket = playerData.gachapoint / 1000
          val gachaPointToNextTicket = 1000 - playerData.gachapoint % 1000

          val gachaTicketStatus = if (obtainableGachaTicket != 0)
            s"$RESET${AQUA}未獲得ガチャ券：${obtainableGachaTicket}枚"
          else
            s"$RESET${RED}獲得できるガチャ券はありません"

          val gachaPointStatus = s"$RESET${AQUA}次のガチャ券まで:${gachaPointToNextTicket}ブロック"

          List(gachaTicketStatus, gachaPointStatus)
        }

        new SkullItemStackBuilder(SkullOwners.unchama)
          .title(s"$DARK_AQUA$UNDERLINE${BOLD}整地報酬ガチャ券を受け取る")
          .lore(lore)
          .build()
      }

      Button(
        iconItemStack,
        LeftClickButtonEffect {
          if (playerData.gachacooldownflag) {
            new CoolDownTask(player, false, false, true).runTaskLater(SeichiAssist.instance, 20)

            val gachaPointPerTicket = SeichiAssist.seichiAssistConfig.getGachaPresentInterval
            val gachaTicketsToGive = Math.min(playerData.gachapoint / gachaPointPerTicket, 576)

            val itemStackToGive = Util.getskull(player.getName)

            if (gachaTicketsToGive > 0) {
              sequentialEffect(
                Util.grantItemStacksEffect(Seq.fill(gachaTicketsToGive)(itemStackToGive): _*),
                targetedeffect.UnfocusedEffect {
                  playerData.gachapoint -= gachaPointPerTicket * gachaTicketsToGive
                },
                s"${GOLD}ガチャ券${gachaTicketsToGive}枚${WHITE}プレゼントフォーユー".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
              )
            } else emptyEffect
          } else emptyEffect
        }
      )
    })

    val computeGachaTicketDeliveryButton: IO[Button] = RecomputedButton(IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      val iconItemStack = {
        val lore = {
          val settingsStatus =
            if (playerData.settings.receiveGachaTicketEveryMinute)
              s"$RESET${GREEN}毎分受け取ります"
            else
              s"$RESET${RED}後でまとめて受け取ります"

          val navigationMessage = s"$RESET$DARK_RED${UNDERLINE}クリックで変更"

          List(settingsStatus, navigationMessage)
        }

        new IconItemStackBuilder(Material.STONE_BUTTON)
          .title(s"$YELLOW$UNDERLINE${BOLD}整地報酬ガチャ券受け取り方法")
          .lore(lore)
          .build()
      }

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          targetedeffect.UnfocusedEffect {
            playerData.settings.receiveGachaTicketEveryMinute = !playerData.settings.receiveGachaTicketEveryMinute
          },
          deferredEffect(IO {
            if (playerData.settings.receiveGachaTicketEveryMinute) {
              sequentialEffect(
                s"${GREEN}毎分のガチャ券受け取り:ON".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
              )
            } else {
              sequentialEffect(
                s"${RED}毎分のガチャ券受け取り:OFF".asMessageEffect(),
                s"${GREEN}ガチャ券受け取りボタンを押すともらえます".asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1.0f, 1.0f)
              )
            }
          })
        )
      )
    })

    val computeValentineChocolateButton: IO[Button] = RecomputedButton(IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      if (playerData.hasChocoGave && Valentine.isInEvent) {
        val iconItemStack =
          new IconItemStackBuilder(Material.TRAPPED_CHEST)
            .enchanted()
            .title("プレゼントボックス")
            .lore(List(
              s"$RESET$RED[バレンタインイベント記念]",
              s"$RESET${AQUA}記念品として",
              s"$RESET${GREEN}チョコチップクッキー×64個",
              s"$RESET${AQUA}を配布します。",
              s"$RESET$DARK_RED$UNDERLINE${BOLD}クリックで受け取る"
            ))
            .build()

        Button(
          iconItemStack,
          LeftClickButtonEffect(
            deferredEffect(IO {
              if (Valentine.isInEvent) {
                sequentialEffect(
                  FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f),
                  targetedeffect.UnfocusedEffect {
                    Valentine.giveChoco(player)
                    playerData.hasChocoGave = true
                  },
                  s"${AQUA}チョコチップクッキーを付与しました。".asMessageEffect()
                )
              } else {
                emptyEffect
              }
            })
          )
        )
      } else {
        Button(new ItemStack(Material.AIR))
      }
    })
  }

  private object ConstantButtons {
    val teleportServerButton: Button = {
      val buttonLore = List(
        s"$GRAY・各サバイバルサーバー",
        s"$GRAY・建築サーバー",
        s"$GRAY・公共施設サーバー",
        s"${GRAY}間を移動する時に使います",
        s"$DARK_RED${UNDERLINE}クリックして開く"
      )

      Button(
        new IconItemStackBuilder(Material.NETHER_STAR)
          .title(s"$RED$UNDERLINE${BOLD}サーバー間移動メニューへ")
          .lore(buttonLore)
          .build(),
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_PORTAL_AMBIENT, 0.6f, 1.5f),
          ServerSwitchMenu.open
        )
      )
    }

    val spawnCommandButton: Button = {
      val buttonLore = List(
        s"$GRAY・メインワールド",
        s"$GRAY・整地ワールド",
        s"${GRAY}間を移動するときに使います",
        s"$DARK_RED${UNDERLINE}クリックするとワープします",
        s"${DARK_GRAY}command=>[/spawn]"
      )

      Button(
        new IconItemStackBuilder(Material.BEACON)
          .title(s"$YELLOW$UNDERLINE${BOLD}スポーンワールドへワープ")
          .lore(buttonLore)
          .build(),
        LeftClickButtonEffect(
          closeInventoryEffect,
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          "spawn".asCommandEffect()
        )
      )
    }

    val achievementSystemButton: Button = {
      val buttonLore = List(
        s"${GRAY}様々な実績に挑んで、",
        s"${GRAY}いろんな二つ名を手に入れよう！",
        s"$DARK_GRAY${UNDERLINE}クリックで設定画面へ移動"
      )

      Button(
        new IconItemStackBuilder(Material.END_CRYSTAL)
          .title(s"$YELLOW$UNDERLINE${BOLD}実績・二つ名システム")
          .lore(buttonLore)
          .build(),
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
          AchievementMenu.open
        )
      )
    }

    val seichiGodRankingButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.COOKIE)
          .title(s"$YELLOW$UNDERLINE${BOLD}整地神ランキングを見る")
          .lore(List(
            s"$RESET$RED(整地神ランキング150位以内のプレイヤーのみ表記されます)",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          // TODO メニューに置き換える
          openInventoryEffect(MenuInventoryData.getRankingList(0)),
        )
      )
    }

    val loginGodRankingButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.COOKIE)
          .title(s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキングを見る")
          .lore(List(
            s"$RESET$RED(ログイン神ランキング150位以内のプレイヤーのみ表記されます)",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          // TODO メニューに置き換える
          openInventoryEffect(MenuInventoryData.getRankingList_playtick(0)),
        )
      )
    }

    val voteGodRankingButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.COOKIE)
          .title(s"$YELLOW$UNDERLINE${BOLD}投票神ランキングを見る")
          .lore(List(
            s"$RESET$RED(投票神ランキング150位以内のプレイヤーのみ表記されます)",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          // TODO メニューに置き換える
          openInventoryEffect(MenuInventoryData.getRankingList_p_vote(0)),
        )
      )
    }

    val secondPageButton: Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowRight),
        "2ページ目へ",
        StickMenu.secondPage
      )

    val gachaPrizeExchangeButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.NOTE_BLOCK)
          .title(s"$YELLOW$UNDERLINE${BOLD}不要ガチャ景品交換システム")
          .lore(List(
            s"$RESET${GREEN}不必要な当たり、大当たり景品を",
            s"$RESET${GREEN}ガチャ券と交換出来ます",
            s"$RESET${GREEN}出てきたインベントリ―に",
            s"$RESET${GREEN}交換したい景品を入れて",
            s"$RESET${GREEN}escキーを押してください",
            s"$RESET${DARK_GRAY}たまにアイテムが消失するから",
            s"$RESET${DARK_GRAY}大事なものはいれないでネ",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
          openInventoryEffect(
            InventoryUtil.createInventory(
              size = 4.chestRows,
              title = Some(s"$LIGHT_PURPLE${BOLD}交換したい景品を入れてください")
            )
          ),
        )
      )
    }

    val homePointMenuButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.BED)
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームメニューを開く")
          .lore(List(
            s"$RESET${GRAY}ホームポイントに関するメニュー",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 1.5f),
          HomeMenu.open
        )
      )
    }

    val randomTeleportButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.COMPASS)
          .title(s"$YELLOW$UNDERLINE${BOLD}ランダムテレポート(β)")
          .lore(List(
            s"$RESET${GRAY}整地ワールドで使うと、良さげな土地にワープします",
            s"$RESET${GRAY}βテスト中のため、謎挙動にご注意ください",
            s"$RESET$DARK_RED${UNDERLINE}クリックで発動",
            s"$RESET${DARK_GRAY}command=>[/rtp]"
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

    val fastCraftButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.WORKBENCH)
          .title(s"$YELLOW$UNDERLINE${BOLD}FastCraft機能")
          .lore(List(
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く",
            s"$RESET${RED}ただの作業台じゃないんです…",
            s"$RESET${YELLOW}自動レシピ補完機能付きの",
            s"$RESET${YELLOW}最強な作業台はこちら",
            s"$RESET${DARK_GRAY}command=>[/fc craft]"
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

    val passiveSkillBookButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.ENCHANTED_BOOK)
          .enchanted()
          .title(s"$YELLOW$UNDERLINE${BOLD}パッシブスキルブック")
          .lore(List(
            s"$RESET${GRAY}整地に便利なスキルを使用できるゾ",
            s"$RESET$DARK_RED${UNDERLINE}クリックでスキル一覧を開く"
          ))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.8f),
          PassiveSkillMenu.open
        )
      )
    }

    val oreExchangeButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.DIAMOND_ORE)
          .title(s"$YELLOW$UNDERLINE${BOLD}鉱石・交換券変換システム")
          .lore(List(
            s"$RESET${GREEN}不必要な各種鉱石を",
            s"$RESET${DARK_RED}交換券$RESET${GREEN}と交換できます",
            s"$RESET${GREEN}出てきたインベントリ―に",
            s"$RESET${GREEN}交換したい鉱石を入れて",
            s"$RESET${GREEN}escキーを押してください",
            s"$RESET${DARK_GRAY}たまにアイテムが消失するから",
            s"$RESET${DARK_GRAY}大事なものはいれないでネ",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
          // TODO メニューに置き換える
          openInventoryEffect(
            InventoryUtil.createInventory(
              size = 4.chestRows,
              title = Some(s"$LIGHT_PURPLE${BOLD}交換したい鉱石を入れてください")
            )
          )
        )
      )
    }

    val votePointMenuButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.DIAMOND)
          .enchanted()
          .title(s"$YELLOW$UNDERLINE${BOLD}投票ptメニュー")
          .lore(List(s"$RESET${GREEN}投票ptに関することはこちらから！"))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          // TODO メニューに置き換える
          computedEffect(p => openInventoryEffect(MenuInventoryData.getVotingMenuData(p))),
        )
      )
    }
  }

}
