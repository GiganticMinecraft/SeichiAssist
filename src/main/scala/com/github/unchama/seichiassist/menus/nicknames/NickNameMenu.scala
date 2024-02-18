package com.github.unchama.seichiassist.menus.nicknames

import cats.effect.{IO, SyncIO}
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton}
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{ChestSlotRef, LayoutPreparationContext, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners}
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.menus.achievement.AchievementMenu
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.player.PlayerEffects.openInventoryEffect
import org.bukkit.{Material, Sound}
import org.bukkit.entity.Player
import org.bukkit.ChatColor._

object NickNameMenu extends Menu {

  class Environment(
    implicit val ioCanOpenAchievementMenu: IO CanOpen AchievementMenu.type,
    implicit val layoutPreparationContext: LayoutPreparationContext,
    implicit val onMinecraftServerThread: OnMinecraftServerThread[IO],
    implicit val voteAPI: VoteAPI[IO, Player],
    implicit val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
  )

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}二つ名組み合わせシステム")

  import eu.timepit.refined.auto._

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._

    val backToAchievementMenu = CommonButtons.transferButton(
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
      s"$YELLOW$BOLD${UNDERLINE}実績・二つ名メニューへ",
      AchievementMenu
    )

    val buttons = NickNameMenuButtons(player)
    import buttons._

    for {
      achievementPointsInformation <- achievementPointsInformation
      pointConvertButton <- pointConvertButton
    } yield {
      MenuSlotLayout(
        ChestSlotRef(0, 0) -> achievementPointsInformation,
        ChestSlotRef(0, 1) -> pointConvertButton,
        ChestSlotRef(0, 4) -> currentNickName,
        ChestSlotRef(1, 0) -> achievementPointShop,
        ChestSlotRef(1, 2) -> headPartsSelect,
        ChestSlotRef(1, 4) -> middlePartsSelect,
        ChestSlotRef(1, 6) -> tailPartsSelect,
        ChestSlotRef(3, 0) -> backToAchievementMenu
      )
    }
  }

  private case class NickNameMenuButtons(player: Player)(implicit environment: Environment) {

    import environment._
    import cats.implicits._

    private val playerData = SeichiAssist.playermap.apply(player.getUniqueId)

    val achievementPointsInformation: IO[Button] = RecomputedButton {
      IO {
        val itemStack = new IconItemStackBuilder(Material.EMERALD_ORE)
          .title(s"$YELLOW$UNDERLINE${BOLD}実績ポイント情報")
          .lore(
            List(
              s"${GREEN}クリックで情報を最新化",
              s"${RED}累計獲得量：${playerData.achievePoint.cumulativeTotal}",
              s"${RED}累計消費量：${playerData.achievePoint.used}",
              s"${RED}使用可能量：${playerData.achievePoint.left}"
            )
          )
          .build()

        Button(
          itemStack,
          LeftClickButtonEffect {
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
          }
        )
      }
    }

    val pointConvertButton: IO[Button] = RecomputedButton {
      for {
        effectPoint <- voteAPI.effectPoints(player).map(_.value)
        itemStack = new IconItemStackBuilder(Material.EMERALD)
          .title(s"$YELLOW$UNDERLINE${BOLD}ポイント変換ボタン")
          .lore(
            List(
              s"${RED}JMS投票で手に入るポイントを",
              s"${RED}実績ポイントに変換できます。",
              s"$YELLOW${BOLD}投票pt 10pt → 実績pt 3pt",
              s"${AQUA}クリックで変換を一回行います。",
              s"${GREEN}所有投票pt：$effectPoint",
              s"${GREEN}所有実績pt；${playerData.achievePoint.left}"
            )
          )
          .build()
        button = Button(
          itemStack,
          LeftClickButtonEffect {
            UnfocusedEffect {
              onMinecraftServerThread.runAction {
                for {
                  _ <- SyncIO {
                    if (effectPoint >= 10)
                      playerData.convertEffectPointToAchievePoint
                    else player.sendMessage("エフェクトポイントが不足しています。")
                  }.whenA(effectPoint >= 10)
                } yield ()
              }
            }
          }
        )
      } yield button
    }

    val currentNickName: Button = {
      val nickname = playerData.settings.nickname
      val playerTitle =
        Nicknames.getCombinedNicknameFor(nickname.id1, nickname.id2, nickname.id3).getOrElse("")

      Button(
        new IconItemStackBuilder(Material.BOOK)
          .title(s"$YELLOW$UNDERLINE${BOLD}現在の二つ名の確認")
          .lore(List(s"$RED「$playerTitle」"))
          .build()
      )
    }

    val achievementPointShop: Button = {
      val itemStack = new IconItemStackBuilder(Material.ITEM_FRAME)
        .title(s"$YELLOW$UNDERLINE${BOLD}実績ポイントショップ")
        .lore(List(s"${GREEN}クリックで開きます"))
        .build()

      Button(
        itemStack,
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 0.1f),
            openInventoryEffect(MenuInventoryData.computePartsShopMenu(player))
          )
        }
      )
    }

    val headPartsSelect: Button = {
      val itemStack = new IconItemStackBuilder(Material.WATER_BUCKET)
        .title(s"$YELLOW$UNDERLINE${BOLD}前パーツ選択画面")
        .lore(List(s"${RED}クリックで移動します。"))
        .build()

      Button(
        itemStack,
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 0.1f),
            openInventoryEffect(MenuInventoryData.computeHeadPartCustomMenu(player))
          )
        }
      )
    }

    val middlePartsSelect: Button = {
      val itemStack = new IconItemStackBuilder(Material.MILK_BUCKET)
        .title(s"$YELLOW$UNDERLINE${BOLD}中パーツ選択画面")
        .lore(List(s"${RED}クリックで移動します"))
        .build()

      Button(
        itemStack,
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 0.1f),
            openInventoryEffect(MenuInventoryData.computeMiddlePartCustomMenu(player))
          )
        }
      )
    }

    val tailPartsSelect: Button = {
      val itemStack = new IconItemStackBuilder(Material.LAVA_BUCKET)
        .title(s"$YELLOW$UNDERLINE${BOLD}後パーツ選択画面")
        .lore(List(s"${RED}クリックで移動します。"))
        .build()

      Button(
        itemStack,
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 0.1f),
            openInventoryEffect(MenuInventoryData.computeTailPartCustomMenu(player))
          )
        }
      )
    }

  }

}
