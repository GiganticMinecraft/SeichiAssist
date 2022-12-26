package com.github.unchama.seichiassist.menus.stickmenu

import cats.effect.{IO, SyncIO}
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory
import com.github.unchama.menuinventory._
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.{
  ClickEventFilter,
  FilteredButtonEffect,
  LeftClickButtonEffect
}
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton, action}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.data.player.settings.BroadcastMutingSettings.{
  MuteMessageAndSound,
  ReceiveMessageAndSound,
  ReceiveMessageOnly
}
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.subsystems.gacha.GachaDrawAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket.ConsumeGachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.Anniversary
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryItemData.anniversaryPlayerHead
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.Christmas
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemData.christmasPlayerHead
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.Valentine
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.ValentineItemData.valentinePlayerHead
import com.github.unchama.seichiassist.subsystems.sharedinventory.SharedInventoryAPI
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.SharedFlag
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.seichiassist.util.exp.ExperienceManager
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.{
  CommandEffect,
  FocusedSoundEffect,
  PlayerEffects
}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Material, Sound}

/**
 * 木の棒メニュー2ページ目
 */
object SecondPage extends Menu {

  import PluginExecutionContexts.onMainThread
  import com.github.unchama.targetedeffect._
  import com.github.unchama.targetedeffect.player.PlayerEffects._
  import com.github.unchama.util.InventoryUtil._
  import eu.timepit.refined.auto._
  import menuinventory.syntax._

  class Environment(
    implicit val ioCanOpenFirstPage: IO CanOpen FirstPage.type,
    val sharedInventoryAPI: SharedInventoryAPI[IO, Player],
    val gachaDrawAPI: GachaDrawAPI[IO, Player],
    val gachaPointAPI: GachaPointApi[IO, SyncIO, Player],
    val consumeGachaTicketAPI: ConsumeGachaTicketAPI[IO, Player]
  )

  override val frame: MenuFrame =
    MenuFrame(4.chestRows, s"${LIGHT_PURPLE}木の棒メニュー")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import ConstantButtons._
    import environment._
    val computations = ButtonComputations(player)
    import computations._

    val constantPart = Map(
      ChestSlotRef(0, 0) -> officialWikiNavigationButton,
      ChestSlotRef(0, 1) -> rulesPageNavigationButton,
      ChestSlotRef(0, 2) -> serverMapNavigationButton,
      ChestSlotRef(0, 3) -> JMSNavigationButton,
      ChestSlotRef(0, 8) -> hubCommandButton,
      ChestSlotRef(3, 0) -> CommonButtons.openStickMenu,
      ChestSlotRef(3, 3) -> recycleBinButton,
      ChestSlotRef(3, 6) -> randomTeleportButton,
      ChestSlotRef(3, 8) -> appleConversionButton
    )

    import cats.implicits._

    val dynamicPartComputation = Map(
      ChestSlotRef(0, 6) -> computeShareInventoryButton,
      ChestSlotRef(1, 3) -> computeHeadSummoningButton,
      ChestSlotRef(1, 4) -> computeBroadcastMessageToggleButton,
      ChestSlotRef(1, 5) -> computeDeathMessageToggleButton,
      ChestSlotRef(1, 6) -> computeWorldGuardMessageToggleButton,
      ChestSlotRef(2, 8) -> computeBulkDrawGachaButton
    ).toList.traverse(_.sequence)

    for {
      dynamicPart <- dynamicPartComputation
    } yield menuinventory.MenuSlotLayout(constantPart ++ dynamicPart)
  }

  private case class ButtonComputations(player: Player)(
    implicit sharedInventoryAPI: SharedInventoryAPI[IO, Player]
  ) {

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
    import player._

    val computeHeadSummoningButton: IO[Button] = RecomputedButton(IO {
      val iconItemStack = {
        val baseLore = List(
          s"$RESET${GRAY}経験値10000を消費して",
          s"$RESET${GRAY}自分の頭を召喚します",
          s"$RESET${GRAY}装飾用にドウゾ！"
        )

        val actionNavigation =
          if (new ExperienceManager(player).hasExp(10000)) {
            s"$RESET$DARK_GREEN${UNDERLINE}クリックで召喚"
          } else {
            s"$RESET$DARK_RED${UNDERLINE}経験値が足りません"
          }

        new SkullItemStackBuilder(SkullOwners.MHF_Villager)
          .title(s"$YELLOW$UNDERLINE${BOLD}自分の頭を召喚")
          .lore(baseLore.appended(actionNavigation))
          .build()
      }

      val effect = action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
        DeferredEffect(IO {
          val expManager = new ExperienceManager(player)
          if (expManager.hasExp(10000)) {
            import scala.util.chaining._
            val skullToGive = new SkullItemStackBuilder(getUniqueId).build().tap { stack =>
              import stack._
              // 季節イベント中の特殊lore
              if (Valentine.isInEvent) setItemMeta {
                valentinePlayerHead(getItemMeta.asInstanceOf[SkullMeta])
              }
              else if (Christmas.isInEventNow) setItemMeta {
                christmasPlayerHead(getItemMeta.asInstanceOf[SkullMeta])
              }
              else if (Anniversary.isInEvent) setItemMeta {
                anniversaryPlayerHead(getItemMeta.asInstanceOf[SkullMeta])
              }
            }

            SequentialEffect(
              InventoryOperations.grantItemStacksEffect(skullToGive),
              UnfocusedEffect { expManager.changeExp(-10000) },
              MessageEffect(s"${GOLD}経験値10000を消費して自分の頭を召喚しました"),
              FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
            )
          } else {
            SequentialEffect(
              MessageEffect(s"${RED}必要な経験値が足りません"),
              FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1.0f, 0.1f)
            )
          }
        })
      }

      Button(iconItemStack, effect)
    })

    val computeBroadcastMessageToggleButton: IO[Button] = RecomputedButton {
      val playerData = SeichiAssist.playermap(getUniqueId)

      for {
        currentSettings <- playerData.settings.getBroadcastMutingSettings
        iconItemStack = {
          val soundConfigurationState =
            if (currentSettings.shouldMuteSounds) {
              s"$RESET${RED}全体大当たり通知音:消音する"
            } else {
              s"$RESET${GREEN}全体大当たり通知音:消音しない"
            }

          val messageConfigurationState =
            if (currentSettings.shouldMuteMessages) {
              s"$RESET${RED}全体大当たりメッセージ:表示しない"
            } else {
              s"$RESET${GREEN}全体大当たりメッセージ:表示する"
            }

          new IconItemStackBuilder(Material.JUKEBOX)
            .title(s"$YELLOW$UNDERLINE${BOLD}全体大当たり通知切替")
            .lore(
              List(
                soundConfigurationState,
                messageConfigurationState,
                s"$RESET$DARK_RED${UNDERLINE}クリックで変更"
              )
            )
            .build()
        }
      } yield Button(
        iconItemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            playerData.settings.toggleBroadcastMutingSettings,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
            DeferredEffect {
              playerData
                .settings
                .getBroadcastMutingSettings
                .map {
                  case ReceiveMessageAndSound => s"${GREEN}非表示/消音設定を解除しました"
                  case ReceiveMessageOnly     => s"${RED}消音可能な全体大当たり通知音を消音します"
                  case MuteMessageAndSound    => s"${RED}非表示可能な全体大当たりメッセージを非表示にします"
                }
                .map(MessageEffect(_))
            }
          )
        }
      )
    }

    val computeDeathMessageToggleButton: IO[Button] = RecomputedButton(IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      val iconItemStack = {
        val baseBuilder =
          new IconItemStackBuilder(Material.FLINT_AND_STEEL)
            .title(s"$YELLOW$UNDERLINE${BOLD}死亡メッセージ表示切替")

        if (playerData.settings.shouldDisplayDeathMessages) {
          baseBuilder
            .enchanted()
            .lore(List(s"$RESET${GREEN}表示する", s"$RESET$DARK_RED${UNDERLINE}クリックで隠す"))
        } else {
          baseBuilder.lore(List(s"$RESET${RED}隠す", s"$RESET$DARK_GREEN${UNDERLINE}クリックで表示する"))
        }
      }.build()

      Button(
        iconItemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            playerData.settings.toggleDeathMessageMutingSettings,
            DeferredEffect(IO {
              val (soundPitch, message) =
                if (playerData.settings.shouldDisplayDeathMessages)
                  (1.0f, s"${GREEN}死亡メッセージ:表示")
                else
                  (0.5f, s"${RED}死亡メッセージ:隠す")

              SequentialEffect(
                MessageEffect(message),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch)
              )
            })
          )
        }
      )
    })

    val computeWorldGuardMessageToggleButton: IO[Button] = RecomputedButton(IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      val iconItemStack = {
        val baseBuilder = new IconItemStackBuilder(Material.BARRIER)
          .title(s"$YELLOW$UNDERLINE${BOLD}ワールドガード保護メッセージ表示切替")

        val loreHeading = s"$RESET${GRAY}スキル使用時のワールドガード保護警告メッセージ"

        if (playerData.settings.shouldDisplayWorldGuardLogs) {
          baseBuilder
            .enchanted()
            .lore(
              List(loreHeading, s"$RESET${GREEN}表示する", s"$RESET$DARK_RED${UNDERLINE}クリックで隠す")
            )
        } else {
          baseBuilder.lore(
            List(loreHeading, s"$RESET${RED}隠す", s"$RESET$DARK_GREEN${UNDERLINE}クリックで表示する")
          )
        }
      }.build()

      Button(
        iconItemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            playerData.settings.toggleWorldGuardLogEffect,
            DeferredEffect(IO {
              val (soundPitch, message) =
                if (playerData.settings.shouldDisplayWorldGuardLogs)
                  (1.0f, s"${GREEN}ワールドガード保護メッセージ:表示")
                else
                  (0.5f, s"${RED}ワールドガード保護メッセージ:隠す")

              SequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch),
                MessageEffect(message)
              )
            })
          )
        }
      )
    })

    val computeShareInventoryButton: IO[Button] = RecomputedButton(IO {
      val iconItemStack = {
        val lore = {
          val base =
            List(s"$RESET${GREEN}現在の装備・アイテムを移動します。", s"${RESET}サーバー間のアイテム移動にご利用ください。", "")

          val statusDisplay =
            if (sharedInventoryAPI.sharedFlag(player).unsafeRunSync() == SharedFlag.Sharing) {
              List(
                s"$RESET${GREEN}収納中",
                s"$RESET$DARK_RED${UNDERLINE}クリックでアイテムを取り出します。",
                s"$RESET${RED}現在の装備・アイテムが空であることを確認してください。"
              )
            } else {
              List(s"$RESET${GREEN}非収納中", s"$RESET$DARK_RED${UNDERLINE}クリックでアイテムを収納します。")
            }

          base ++ statusDisplay
        }

        new IconItemStackBuilder(Material.TRAPPED_CHEST)
          .title(s"$YELLOW$UNDERLINE${BOLD}インベントリ共有")
          .lore(lore)
          .build()
      }

      Button(iconItemStack, LeftClickButtonEffect(CommandEffect("shareinv")))
    })

    def computeBulkDrawGachaButton(implicit environment: Environment): IO[Button] =
      RecomputedButton {
        import environment._

        val leftClickButtonEffect = action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
          _ =>
            DeferredEffect {
              for {
                _ <- consumeGachaTicketAPI.toggleConsumeGachaTicketAmount.apply(player)
                consumeGachaTicketAmount <- consumeGachaTicketAPI.consumeGachaTicketAmount(
                  player
                )
              } yield SequentialEffect(
                MessageEffect(s"まとめ引きするガチャ券の数を${consumeGachaTicketAmount.value}枚に変更しました。"),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
              )
            }
        }

        val rightClickButtonEffect =
          action.FilteredButtonEffect(ClickEventFilter.RIGHT_CLICK) { _ =>
            DeferredEffect {
              for {
                currentConsumeGachaTicketAmount <- consumeGachaTicketAPI
                  .consumeGachaTicketAmount(player)
                currentGachaPoint <- gachaPointAPI.gachaPoint(player).read.toIO
              } yield {
                val currentGachaTicketAmount = currentGachaPoint.availableTickets
                // 残ガチャ券のストックがまとめ引き指定数に足りない場合は何もしない
                if (currentGachaTicketAmount < currentConsumeGachaTicketAmount.value) {
                  SequentialEffect(
                    MessageEffect(s"${RED}整地報酬ガチャ券のストックが足りません。"),
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                  )
                } else {
                  SequentialEffect(
                    // 無暗に連打させないよう、まとめ引き時にメニューを閉じる
                    closeInventoryEffect,
                    DeferredEffect(IO {
                      gachaPointAPI.subtractGachaPoint(
                        GachaPoint.gachaPointBy(currentConsumeGachaTicketAmount.value)
                      )
                    }),
                    DeferredEffect(IO {
                      gachaDrawAPI.drawGacha(currentConsumeGachaTicketAmount.value)
                    })
                  )
                }
              }
            }
          }

        val computeItemStack: IO[ItemStack] =
          consumeGachaTicketAPI.consumeGachaTicketAmount(player).map { amount =>
            val lore = List(
              s"$RESET${GREEN}ガチャを一気に$YELLOW${amount.value}回${GREEN}引きます!",
              "左クリックで一度に引く枚数を変更します",
              "右クリックでガチャを引きます",
              "ガチャ券は整地報酬ガチャ券のストックから直接差し引かれます"
            )
            new IconItemStackBuilder(Material.PAPER)
              .title(s"$YELLOW$UNDERLINE${BOLD}ガチャ一括まとめ引き!")
              .lore(lore)
              .build()
          }

        for {
          itemStack <- computeItemStack
        } yield Button(itemStack, leftClickButtonEffect, rightClickButtonEffect)
      }
  }

  private object ConstantButtons {
    val officialWikiNavigationButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.BOOK)
        .title(s"$YELLOW$UNDERLINE${BOLD}公式サイトにアクセス")
        .lore(
          List(
            s"$RESET${GREEN}鯖内の「困った」は公式サイトで解決！",
            s"$RESET${DARK_GRAY}クリックするとチャット欄に",
            s"$RESET${DARK_GRAY}URLが表示されますので",
            s"$RESET${DARK_GRAY}Tキーを押してから",
            s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          )
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            closeInventoryEffect,
            MessageEffect(
              s"$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("official")}"
            ),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
          )
        }
      )
    }

    val rulesPageNavigationButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.PAPER)
        .title(s"$YELLOW$UNDERLINE${BOLD}運営方針とルールを確認")
        .lore(
          List(
            s"$RESET${GREEN}当鯖で遊ぶ前に確認してネ！",
            s"$RESET${DARK_GRAY}クリックするとチャット欄に",
            s"$RESET${DARK_GRAY}URLが表示されますので",
            s"$RESET${DARK_GRAY}Tキーを押してから",
            s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          )
        )
        .build()

      Button(
        iconItemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            closeInventoryEffect,
            MessageEffect(s"$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("rule")}"),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
          )
        }
      )
    }

    val serverMapNavigationButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.MAP)
        .title(s"$YELLOW$UNDERLINE${BOLD}鯖Mapを見る")
        .lore(
          List(
            s"$RESET${GREEN}webブラウザから鯖Mapを閲覧出来ます",
            s"$RESET${GREEN}他人の居場所や保護の場所を確認出来ます",
            s"$RESET${DARK_GRAY}クリックするとチャット欄に",
            s"$RESET${DARK_GRAY}URLが表示されますので",
            s"$RESET${DARK_GRAY}Tキーを押してから",
            s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          )
        )
        .build()

      Button(
        iconItemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            closeInventoryEffect,
            MessageEffect(s"$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("map")}"),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
          )
        }
      )
    }

    val JMSNavigationButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.SIGN)
        .title(s"$YELLOW$UNDERLINE${BOLD}JapanMinecraftServerリンク")
        .lore(
          List(
            s"$RESET${DARK_GRAY}クリックするとチャット欄に",
            s"$RESET${DARK_GRAY}URLが表示されますので",
            s"$RESET${DARK_GRAY}Tキーを押してから",
            s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          )
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            closeInventoryEffect,
            MessageEffect(s"$RED$UNDERLINE${SeichiAssist.seichiAssistConfig.getUrl("jms")}"),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
          )
        }
      )
    }

    val appleConversionButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.GOLDEN_APPLE, durability = 1)
        .title(s"$YELLOW$UNDERLINE${BOLD}GT景品→椎名林檎変換システム")
        .lore(
          List(
            s"$RESET${GREEN}不必要なGT大当り景品を",
            s"$RESET${GOLD}椎名林檎$RESET${GREEN}と交換できます",
            s"$RESET${GREEN}出てきたインベントリに",
            s"$RESET${GREEN}交換したい景品を入れて",
            s"$RESET${GREEN}escキーを押してください",
            s"$RESET${DARK_GRAY}たまにアイテムが消失しますが",
            s"$RESET${DARK_GRAY}補償はしていません(ごめんなさい)",
            s"$RESET${DARK_GRAY}神に祈りながら交換しよう",
            s"${RESET}現在の交換レート:GT景品1つにつき${SeichiAssist.seichiAssistConfig.rateGiganticToRingo}個",
            s"$RESET$DARK_GRAY$DARK_RED${UNDERLINE}クリックで開く"
          )
        )
        .build()

      Button(
        iconItemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
            // TODO メニューインベントリに差し替える
            openInventoryEffect(
              createInventory(
                size = 4.chestRows,
                title = Some(s"$GOLD${BOLD}椎名林檎と交換したい景品を入れてネ")
              )
            )
          )
        }
      )
    }

    val recycleBinButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.BUCKET)
        .title(s"$YELLOW$UNDERLINE${BOLD}ゴミ箱を開く")
        .lore(
          List(
            s"$RESET${GREEN}不用品の大量処分にドウゾ！",
            s"$RESET${RED}復活しないので取扱注意",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          )
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 1.5f),
            // クローズ時に何も処理されないインベントリを開くことでアイテムを虚空に飛ばす
            openInventoryEffect(
              createInventory(size = 4.chestRows, title = Some(s"$RED${BOLD}ゴミ箱(取扱注意)"))
            )
          )
        }
      )
    }

    val hubCommandButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.NETHER_STAR)
        .title(s"$YELLOW$UNDERLINE${BOLD}ロビーサーバーへ移動")
        .lore(
          List(s"$RESET$DARK_RED${UNDERLINE}クリックすると移動します", s"$RESET${DARK_GRAY}command=>[/hub]")
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
            PlayerEffects.connectToServerEffect("lobby")
          )
        }
      )
    }

    val randomTeleportButton: Button = Button(
      new IconItemStackBuilder(Material.COMPASS)
        .title(s"$YELLOW$UNDERLINE${BOLD}ランダムテレポートします。")
        .lore(
          List(
            s"$RESET$DARK_RED${UNDERLINE}ランダムな場所にテレポートします。",
            s"$RESET${DARK_GRAY}command=>[/rtp]"
          )
        )
        .build(),
      LeftClickButtonEffect {
        CommandEffect("rtp")
      }
    )

  }
}
