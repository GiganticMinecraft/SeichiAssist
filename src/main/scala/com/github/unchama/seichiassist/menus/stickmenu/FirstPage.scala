package com.github.unchama.seichiassist.menus.stickmenu

import cats.data.Kleisli
import cats.effect.{IO, SyncIO}
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory._
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.{
  ClickEventFilter,
  FilteredButtonEffect,
  LeftClickButtonEffect
}
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton, action}
import com.github.unchama.seichiassist.data.descrptions.PlayerStatsLoreGenerator
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.achievement.AchievementMenu
import com.github.unchama.seichiassist.menus.home.HomeMenu
import com.github.unchama.seichiassist.menus.minestack.MineStackMainMenu
import com.github.unchama.seichiassist.menus.ranking.RankingRootMenu
import com.github.unchama.seichiassist.menus.skill.{ActiveSkillMenu, PassiveSkillMenu}
import com.github.unchama.seichiassist.menus.{
  CommonButtons,
  RegionMenu,
  ServerSwitchMenu,
  VoteMenu
}
import com.github.unchama.seichiassist.subsystems.anywhereender.AnywhereEnderChestAPI
import com.github.unchama.seichiassist.subsystems.anywhereender.domain.AccessDenialReason
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiStarLevel
import com.github.unchama.seichiassist.subsystems.breakcountbar.BreakCountBarAPI
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.BreakCountBarVisibility
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.{
  FastDiggingEffectApi,
  FastDiggingSettingsApi
}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.FourDimensionalPocketApi
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.PocketSize
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.ranking.api.RankingProvider
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners, util}
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.{CommandEffect, FocusedSoundEffect}
import com.github.unchama.util.InventoryUtil
import com.github.unchama.util.external.WorldGuardWrapper
import io.chrisdavenport.cats.effect.time.JavaTime
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

/**
 * 木の棒メニュー1ページ目
 *
 * @author
 *   karayuu
 */
object FirstPage extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{
    layoutPreparationContext,
    onMainThread
  }
  import com.github.unchama.targetedeffect.player.PlayerEffects._
  import eu.timepit.refined.auto._

  class Environment(
    implicit val breakCountAPI: BreakCountReadAPI[IO, SyncIO, Player],
    val breakCountBarApi: BreakCountBarAPI[SyncIO, Player],
    val fourDimensionalPocketApi: FourDimensionalPocketApi[IO, Player],
    val fastDiggingEffectApi: FastDiggingEffectApi[IO, Player],
    val fastDiggingSettingsApi: FastDiggingSettingsApi[IO, Player],
    val rankingApi: RankingProvider[IO, SeichiAmountData],
    val gachaPointApi: GachaPointApi[IO, SyncIO, Player],
    val ioJavaTime: JavaTime[IO],
    val ioCanOpenSecondPage: IO CanOpen SecondPage.type,
    val ioCanOpenMineStackMenu: IO CanOpen MineStackMainMenu.type,
    val ioCanOpenRegionMenu: IO CanOpen RegionMenu.type,
    val ioCanOpenActiveSkillMenu: IO CanOpen ActiveSkillMenu.type,
    val ioCanOpenServerSwitchMenu: IO CanOpen ServerSwitchMenu.type,
    val ioCanOpenAchievementMenu: IO CanOpen AchievementMenu.type,
    val ioCanOpenHomeMenu: IO CanOpen HomeMenu,
    val ioCanOpenPassiveSkillMenu: IO CanOpen PassiveSkillMenu.type,
    val ioCanOpenRankingRootMenu: IO CanOpen RankingRootMenu.type,
    val ioCanOpenVoteMenu: IO CanOpen VoteMenu.type,
    val enderChestAccessApi: AnywhereEnderChestAPI[IO],
    val gachaTicketAPI: GachaTicketAPI[IO],
    val voteAPI: VoteAPI[IO, Player]
  )

  override val frame: MenuFrame =
    MenuFrame(4.chestRows, s"${LIGHT_PURPLE}木の棒メニュー")

  import com.github.unchama.targetedeffect._

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import ConstantButtons._
    val computations = ButtonComputations(player)
    import computations._
    import environment._

    val constantPart =
      Map(
        ChestSlotRef(0, 7) -> teleportServerButton,
        ChestSlotRef(0, 8) -> spawnCommandButton,
        ChestSlotRef(1, 0) -> achievementSystemButton,
        ChestSlotRef(1, 2) -> passiveSkillBookButton,
        ChestSlotRef(1, 7) -> gachaPrizeExchangeButton,
        ChestSlotRef(1, 8) -> oreExchangeButton,
        ChestSlotRef(2, 0) -> homePointMenuButton,
        ChestSlotRef(2, 5) -> fastCraftButton,
        ChestSlotRef(3, 3) -> votePointMenuButton,
        ChestSlotRef(3, 4) -> mapCommandButton,
        ChestSlotRef(3, 6) -> CommonButtons.transferButton(
          new IconItemStackBuilder(Material.COOKIE),
          "ランキングメニューを開く",
          RankingRootMenu
        ),
        ChestSlotRef(3, 8) -> secondPageButton
      )

    import cats.implicits._

    val dynamicPartComputation: IO[List[(Int, Button)]] = {
      val computeConstantlyShownPart: IO[List[(Int, Button)]] = List(
        ChestSlotRef(0, 0) -> computeStatsButton,
        ChestSlotRef(0, 1) -> computeEffectSuppressionButton,
        ChestSlotRef(0, 3) -> computeRegionMenuButton,
        ChestSlotRef(0, 5) -> computeGachaTicketButton,
        ChestSlotRef(1, 4) -> computeActiveSkillButton,
        ChestSlotRef(2, 3) -> computePocketOpenButton,
        ChestSlotRef(2, 4) -> computeEnderChestButton,
        ChestSlotRef(2, 6) -> computeMineStackButton,
        ChestSlotRef(3, 2) -> computeApologyItemsButton
      ).traverse(_.sequence)

      val computeOptionallyShownPart: IO[List[(Int, Option[Button])]] =
        List(ChestSlotRef(1, 1) -> computeStarLevelStatsButton).traverse(_.sequence)

      for {
        constantlyShownPart <- computeConstantlyShownPart
        optionallyShownPart <- computeOptionallyShownPart
      } yield {
        constantlyShownPart ++ optionallyShownPart.mapFilter(_.sequence)
      }
    }

    for {
      dynamicPart <- dynamicPartComputation

    } yield MenuSlotLayout(constantPart ++ dynamicPart.toMap)
  }

  private case class ButtonComputations(player: Player)(implicit environment: Environment) {

    import player._

    val computeStatsButton: IO[Button] = RecomputedButton {
      val openerData = SeichiAssist.playermap(getUniqueId)

      val visibilityRef =
        environment.breakCountBarApi.breakCountBarVisibility(player)

      for {
        seichiAmountData <-
          environment.breakCountAPI.seichiAmountDataRepository(player).read.toIO
        ranking <- environment.rankingApi.ranking.read
        visibility <- visibilityRef.get.toIO
        lore <- new PlayerStatsLoreGenerator(
          openerData,
          ranking,
          seichiAmountData,
          visibility,
          environment.voteAPI
        ).computeLore()
      } yield Button(
        new SkullItemStackBuilder(getUniqueId)
          .title(s"$YELLOW$BOLD$UNDERLINE${getName}の統計データ")
          .lore(lore)
          .build(),
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          DeferredEffect {
            visibilityRef.updateAndGet(_.nextValue).toIO.map { updatedVisibility =>
              val toggleSoundPitch =
                if (updatedVisibility == BreakCountBarVisibility.Shown) 1.0f else 0.5f
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, toggleSoundPitch)
            }
          }
        }
      )
    }

    val computeEffectSuppressionButton: IO[Button] = RecomputedButton {
      import cats.implicits._
      import environment._

      val computeButtonLore: IO[List[String]] = for {
        currentStatus <- environment
          .fastDiggingSettingsApi
          .currentSuppressionSettings(player)
          .read
        effectList <- environment.fastDiggingEffectApi.currentEffect(player).read
        currentEffects <- effectList.filteredList[IO]
        currentAmplifier = currentEffects.map(_.effect.amplifier).combineAll
      } yield {
        val toggleNavigation = {
          val currentStatusDescription =
            currentStatus match {
              case FastDiggingEffectSuppressionState.EnabledWithoutLimit =>
                s"${GREEN}現在有効です(無制限)"
              case limit: FastDiggingEffectSuppressionState.EnabledWithLimit =>
                s"${GREEN}現在有効です$YELLOW(${limit.limit}制限)"
              case FastDiggingEffectSuppressionState.Disabled =>
                s"${RED}現在OFFです"
            }

          val nextStatusDescription = {
            val nextStatus = currentStatus.nextState

            s"$RESET$DARK_RED${UNDERLINE}クリックで" + {
              nextStatus match {
                case FastDiggingEffectSuppressionState.EnabledWithoutLimit =>
                  "無制限"
                case limit: FastDiggingEffectSuppressionState.EnabledWithLimit =>
                  s"${limit.limit}制限"
                case FastDiggingEffectSuppressionState.Disabled =>
                  "OFF"
              }
            }
          }

          List(currentStatusDescription, nextStatusDescription)
        }

        val explanation = List(
          s"$RESET${GRAY}採掘速度上昇効果とは",
          s"$RESET${GRAY}接続人数と1分間の採掘量に応じて",
          s"$RESET${GRAY}採掘速度が変化するシステムです",
          s"$RESET${GOLD}現在の採掘速度上昇Lv：${currentAmplifier.formatted}"
        )

        val effectStats =
          List(s"$RESET$YELLOW${UNDERLINE}上昇量の内訳") ++
            currentEffects.map { effectTimings =>
              s"$RESET$RED +${effectTimings.effect.amplifier.formatted} ${effectTimings.effect.cause.description}"
            }

        toggleNavigation ++ explanation ++ effectStats
      }

      for {
        buttonLore <- computeButtonLore
      } yield Button(
        new IconItemStackBuilder(Material.DIAMOND_PICKAXE)
          .title(s"$YELLOW$UNDERLINE${BOLD}採掘速度上昇効果")
          .enchanted()
          .lore(buttonLore)
          .build(),
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            environment.fastDiggingSettingsApi.toggleEffectSuppression.as(())
          )
        }
      )
    }

    val computeRegionMenuButton: IO[Button] = IO {
      val (buttonLore, effect) = {
        val world = getWorld

        if (!WorldGuardWrapper.canProtectionWorld(world)) {
          (List(s"${GRAY}このワールドでは土地の保護は行なえません"), LeftClickButtonEffect(emptyEffect))
        } else {
          val maxRegionCount = WorldGuardWrapper.getWorldMaxRegion(world)
          val currentPlayerRegionCount =
            WorldGuardWrapper.getMaxRegion(player, world)

          (
            List(
              s"${GRAY}土地の保護が行えます",
              s"$DARK_RED${UNDERLINE}クリックで開く",
              s"${GRAY}保護作成上限：$AQUA$maxRegionCount",
              s"${GRAY}現在のあなたの保護作成数：$AQUA$currentPlayerRegionCount"
            ),
            LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.5f),
              environment.ioCanOpenRegionMenu.open(RegionMenu)
            )
          )
        }
      }

      Button(
        new IconItemStackBuilder(Material.DIAMOND_AXE)
          .title(s"$YELLOW${UNDERLINE}土地保護メニュー")
          .lore(buttonLore)
          .build(),
        effect
      )
    }

    val computeMineStackButton: IO[Button] = {
      val buttonLore: List[String] = {
        val explanation = List(
          s"$RESET${GREEN}説明しよう!MineStackとは…",
          s"${RESET}主要アイテムを無限にスタック出来る!",
          s"${RESET}スタックしたアイテムは",
          s"${RESET}ここから取り出せるゾ!"
        )

        val actionGuidance = s"$RESET$DARK_GREEN${UNDERLINE}クリックで開く"

        val annotation = List(
          s"$RESET$DARK_GRAY※スタックしたアイテムは",
          s"$RESET${DARK_GRAY}各サバイバルサーバー間で",
          s"$RESET${DARK_GRAY}共有されます"
        )

        explanation ++ List(actionGuidance) ++ annotation
      }

      IO(
        Button(
          new IconItemStackBuilder(Material.CHEST)
            .title(s"$YELLOW$UNDERLINE${BOLD}MineStack機能")
            .lore(buttonLore)
            .build(),
          LeftClickButtonEffect {
            SequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
              environment.ioCanOpenMineStackMenu.open(MineStackMainMenu)
            )
          }
        )
      )
    }

    val computePocketOpenButton: IO[Button] = {
      val api = environment.fourDimensionalPocketApi

      val readCurrentSize: IO[PocketSize] = api.currentPocketSize(player).read

      val openPocket: Kleisli[IO, Player, Unit] = api.openPocketInventory

      for {
        currentSize <- readCurrentSize
      } yield {
        val iconItemStack = {
          val loreAnnotation = List(
            s"$RESET$DARK_GRAY※4次元ポケットの中身は",
            s"$RESET${DARK_GRAY}各サバイバルサーバー間で",
            s"$RESET${DARK_GRAY}共有されます"
          )

          val loreHeading = List(
            s"$RESET${GRAY}ポケットサイズ:${currentSize.totalStackCount}スタック",
            s"$RESET$DARK_GREEN${UNDERLINE}クリックで開く"
          )

          new IconItemStackBuilder(Material.END_PORTAL_FRAME)
            .title(s"$YELLOW$UNDERLINE${BOLD}4次元ポケットを開く")
            .lore(loreHeading ++ loreAnnotation)
            .build()
        }

        Button(iconItemStack, LeftClickButtonEffect(openPocket))
      }
    }

    val computeEnderChestButton: IO[Button] = for {
      canAccess <- environment.enderChestAccessApi.canAccessAnywhereEnderChest(player)
    } yield {
      val iconItemStack =
        new IconItemStackBuilder(Material.ENDER_CHEST)
          .title(s"$DARK_PURPLE$UNDERLINE${BOLD}どこでもエンダーチェスト")
          .lore(
            List(
              canAccess.fold(
                {
                  case AccessDenialReason.NotEnoughLevel(current, required) =>
                    s"$RESET$DARK_RED${UNDERLINE}整地Lvが${required.level}以上必要です(現在${current.level})"
                },
                _ => s"$RESET$DARK_GREEN${UNDERLINE}クリックで開く"
              )
            )
          )
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          environment.enderChestAccessApi.openEnderChestOrNotifyInsufficientLevel.flatMap {
            case Right(_) =>
              // 開くのに成功した場合の音
              FocusedSoundEffect(Sound.BLOCK_GRASS_PLACE, 1.0f, 0.1f)
            case Left(_) =>
              // 開くのに失敗した場合の音
              FocusedSoundEffect(Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f)
          }
        )
      )
    }

    val computeApologyItemsButton: IO[Button] = RecomputedButton(IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      val iconItemStack = {
        val lore = {
          val explanation = List(s"$RESET${GRAY}運営からのガチャ券を受け取ります")

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
        LeftClickButtonEffect(DeferredEffect(IO {
          if (playerData.gachacooldownflag) {
            new CoolDownTask(player, false, true).runTaskLater(SeichiAssist.instance, 20)

            val numberOfItemsToGive =
              environment.gachaTicketAPI.receive(player.getUniqueId).unsafeRunSync().value

            if (numberOfItemsToGive > 0) {
              val itemToGive = BukkitGachaSkullData.gachaSkull
              val itemStacksToGive = Seq.fill(numberOfItemsToGive)(itemToGive)

              SequentialEffect(
                util.InventoryOperations.grantItemStacksEffect(itemStacksToGive: _*),
                UnfocusedEffect {
                  playerData.unclaimedApologyItems -= numberOfItemsToGive
                },
                FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f),
                MessageEffect(
                  s"${GREEN}運営チームから${numberOfItemsToGive}枚の${GOLD}ガチャ券${WHITE}を受け取りました"
                )
              )
            } else emptyEffect
          } else emptyEffect
        }))
      )
    })

    val computeStarLevelStatsButton: IO[Option[Button]] =
      environment
        .breakCountAPI
        .seichiAmountDataRepository(player)
        .read
        .map(seichiAmountData => {
          val starLevel = seichiAmountData.starLevelCorrespondingToExp

          Option.when(starLevel != SeichiStarLevel.zero) {
            val iconItemStack = {
              val lore = List(
                s"$RESET$GREEN$UNDERLINE${BOLD}現在のスターレベル：☆${starLevel.level}",
                s"$RESET${AQUA}次の☆まで：あと${seichiAmountData.levelProgress.expAmountToNextLevel.formatted}"
              )

              new IconItemStackBuilder(Material.GOLD_INGOT)
                .title(s"$YELLOW$UNDERLINE${BOLD}スターレベル情報")
                .lore(lore)
                .build()
            }

            Button(iconItemStack)
          }
        })
        .toIO

    val computeActiveSkillButton: IO[Button] = IO {
      val iconItemStack = {
        val lore =
          if (player.getWorld.asManagedWorld().exists(_.isSeichiSkillAllowed))
            List(s"$RESET${GRAY}整地に便利なスキルを使用できるゾ", s"$RESET$DARK_RED${UNDERLINE}クリックでスキル一覧を開く")
          else
            List(s"$RESET${RED}このワールドでは", s"$RESET${RED}整地スキルを使えません")

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
          environment.ioCanOpenActiveSkillMenu.open(ActiveSkillMenu)
        )
      )
    }

    val computeGachaTicketButton: IO[Button] = {
      val effect: FilteredButtonEffect = LeftClickButtonEffect(
        environment.gachaPointApi.receiveBatch
      )

      val computeItemStack: IO[ItemStack] =
        environment.gachaPointApi.gachaPoint(player).read.toIO.map { point =>
          val lore = {
            val gachaTicketStatus =
              if (point.availableTickets != BigInt(0))
                s"$RESET${AQUA}未獲得ガチャ券：${point.availableTickets}枚"
              else
                s"$RESET${RED}獲得できるガチャ券はありません"

            val requiredToNextTicket =
              s"$RESET${AQUA}次のガチャ券まで:${point.amountUntilNextGachaTicket.amount}ブロック"

            List(gachaTicketStatus, requiredToNextTicket)
          }

          new SkullItemStackBuilder(SkullOwners.unchama)
            .title(s"$DARK_AQUA$UNDERLINE${BOLD}整地報酬ガチャ券を受け取る")
            .lore(lore)
            .build()
        }

      val computeButton: IO[Button] = computeItemStack.map { itemStack =>
        Button(itemStack, effect)
      }

      RecomputedButton(computeButton)
    }
  }

  private object ConstantButtons {
    def teleportServerButton(
      implicit ioCanOpenServerSwitchMenu: IO CanOpen ServerSwitchMenu.type
    ): Button = {
      val buttonLore = List(
        s"$GRAY・各サバイバルサーバー",
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
          ioCanOpenServerSwitchMenu.open(ServerSwitchMenu)
        )
      )
    }

    val spawnCommandButton: Button = {
      val buttonLore = List(
        s"$GRAY・整地ワールド間を移動するとき",
        s"$GRAY・拠点を建築するとき",
        s"$GRAY に使います",
        s"$DARK_RED${UNDERLINE}クリックするとワープします",
        s"${DARK_GRAY}command=>[/spawn]"
      )

      Button(
        new IconItemStackBuilder(Material.BEACON)
          .title(s"$YELLOW$UNDERLINE${BOLD}メインワールドへワープ")
          .lore(buttonLore)
          .build(),
        LeftClickButtonEffect(
          closeInventoryEffect,
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          CommandEffect("spawn")
        )
      )
    }

    def achievementSystemButton(
      implicit ioCanOpenAchievementMenu: IO CanOpen AchievementMenu.type
    ): Button = {
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
          ioCanOpenAchievementMenu.open(AchievementMenu)
        )
      )
    }

    def secondPageButton(implicit ioCanOpenSecondPage: IO CanOpen SecondPage.type): Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowRight),
        "2ページ目へ",
        StickMenu.secondPage
      )

    val gachaPrizeExchangeButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.NOTE_BLOCK)
          .title(s"$YELLOW$UNDERLINE${BOLD}不要ガチャ景品交換システム")
          .lore(
            List(
              s"$RESET${GREEN}不必要な当たり、大当たり景品を",
              s"$RESET${GREEN}ガチャ券と交換出来ます",
              s"$RESET${GREEN}出てきたインベントリ―に",
              s"$RESET${GREEN}交換したい景品を入れて",
              s"$RESET${GREEN}escキーを押してください",
              s"$RESET${DARK_GRAY}たまにアイテムが消失するから",
              s"$RESET${DARK_GRAY}大事なものはいれないでネ",
              s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
            )
          )
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
          openInventoryEffect(
            InventoryUtil.createInventory(
              size = 6.chestRows,
              title = Some(s"$LIGHT_PURPLE${BOLD}交換したい景品を入れてください")
            )
          )
        )
      )
    }

    def homePointMenuButton(implicit ioCanOpenHomeMenu: IO CanOpen HomeMenu): Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.WHITE_BED)
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームメニューを開く")
          .lore(List(s"$RESET${GRAY}ホームポイントに関するメニュー", s"$RESET$DARK_RED${UNDERLINE}クリックで開く"))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 1.5f),
          ioCanOpenHomeMenu.open(new HomeMenu)
        )
      )
    }

    val fastCraftButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.CRAFTING_TABLE)
          .title(s"$YELLOW$UNDERLINE${BOLD}FastCraft機能")
          .lore(
            List(
              s"$RESET$DARK_RED${UNDERLINE}クリックで開く",
              s"$RESET${RED}ただの作業台じゃないんです…",
              s"$RESET${YELLOW}自動レシピ補完機能付きの",
              s"$RESET${YELLOW}最強な作業台はこちら",
              s"$RESET${DARK_GRAY}command=>[/fc craft]"
            )
          )
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
          CommandEffect("fc craft")
        )
      )
    }

    def passiveSkillBookButton(
      implicit ioCanOpenPassiveSkillMenu: IO CanOpen PassiveSkillMenu.type
    ): Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.ENCHANTED_BOOK)
          .enchanted()
          .title(s"$YELLOW$UNDERLINE${BOLD}パッシブスキルブック")
          .lore(
            List(s"$RESET${GRAY}整地に便利なスキルを使用できるゾ", s"$RESET$DARK_RED${UNDERLINE}クリックでスキル一覧を開く")
          )
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.8f),
          ioCanOpenPassiveSkillMenu.open(PassiveSkillMenu)
        )
      )
    }

    val oreExchangeButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.DIAMOND_ORE)
          .title(s"$YELLOW$UNDERLINE${BOLD}鉱石・交換券変換システム")
          .lore(
            List(
              s"$RESET${GREEN}不必要な各種鉱石を",
              s"$RESET${DARK_RED}交換券$RESET${GREEN}と交換できます",
              s"$RESET${GREEN}出てきたインベントリ―に",
              s"$RESET${GREEN}交換したい鉱石を入れて",
              s"$RESET${GREEN}escキーを押してください",
              s"$RESET${DARK_GRAY}たまにアイテムが消失するから",
              s"$RESET${DARK_GRAY}大事なものはいれないでネ",
              s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
            )
          )
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_CHEST_OPEN, 1.0f, 0.5f),
          // TODO メニューに置き換える
          openInventoryEffect(
            InventoryUtil.createInventory(
              size = 6.chestRows,
              title = Some(s"$LIGHT_PURPLE${BOLD}交換したい鉱石を入れてください")
            )
          )
        )
      )
    }

    def votePointMenuButton(implicit ioCanOpenVoteMenu: IO CanOpen VoteMenu.type): Button = {
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
          ioCanOpenVoteMenu.open(VoteMenu)
        )
      )
    }

    val mapCommandButton: Button =
      Button(
        new IconItemStackBuilder(Material.MAP)
          .title(s"${YELLOW}ウェブマップのURLを表示")
          .lore(
            List(
              s"$RESET${YELLOW}現在座標を示すウェブマップのURLを表示します！",
              s"$RESET$DARK_RED${UNDERLINE}クリックでURLを表示",
              s"${DARK_GRAY}command=>[/map]"
            )
          )
          .build(),
        LeftClickButtonEffect(closeInventoryEffect, CommandEffect("map"))
      )
  }

}
