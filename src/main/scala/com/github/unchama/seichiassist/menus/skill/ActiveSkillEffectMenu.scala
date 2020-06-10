package com.github.unchama.seichiassist.menus.skill

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, ReloadingButton}
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillEffect.NoEffect
import com.github.unchama.seichiassist.seichiskill.effect.{ActiveSkillEffect, ActiveSkillNormalEffect, ActiveSkillPremiumEffect, UnlockableActiveSkillEffect}
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners}
import com.github.unchama.targetedeffect.player.{FocusedSoundEffect, MessageEffect}
import com.github.unchama.util.ActionStatus
import net.md_5.bungee.api.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

object ActiveSkillEffectMenu extends Menu {
  import cats.implicits._
  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}
  import com.github.unchama.targetedeffect._
override val frame: MenuFrame = MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}整地スキルエフェクト選択")

  def setEffectSelectionTo(effect: ActiveSkillEffect)(player: Player): IO[Unit] = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)

    MessageEffect(s"${GREEN}エフェクト：${effect.nameOnUI} が選択されました")(player) >> IO {
      playerData.skillEffectState = playerData.skillEffectState.copy(selection = effect)
    }
  }

  def unlockOrSet(effect: ActiveSkillEffect): TargetedEffect[Player] = Kleisli { player =>
    val playerData = SeichiAssist.playermap(player.getUniqueId)

    def unlockNormalEffect(effect: ActiveSkillNormalEffect): IO[Unit] =
      for {
        effectPoint <- IO { playerData.effectPoint }
        _ <-
          if (effectPoint < effect.usePoint) {
            sequentialEffect(
              MessageEffect(s"${DARK_RED}エフェクトポイントが足りません"),
              FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1.0f, 0.5f)
            )(player)
          } else {
            IO {
              playerData.effectPoint -= effect.usePoint
              val state = playerData.skillEffectState
              playerData.skillEffectState = state.copy(obtainedEffects = state.obtainedEffects + effect)
            } >> sequentialEffect(
              MessageEffect(s"${LIGHT_PURPLE}エフェクト：${effect.nameOnUI}$RESET$LIGHT_PURPLE${BOLD}を解除しました"),
              FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f)
            )(player)
          }
      } yield ()

    def unlockPremiumEffect(effect: ActiveSkillPremiumEffect): IO[Unit] =
      for {
        premiumEffectPoint <- SeichiAssist.databaseGateway.donateDataManipulator.currentPremiumPointFor(player)
        _ <-
          if (premiumEffectPoint < effect.usePoint) {
            sequentialEffect(
              MessageEffect(s"${DARK_RED}プレミアムエフェクトポイントが足りません"),
              FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1.0f, 0.5f)
            )(player)
          } else {
            for {
              transactionResult <- SeichiAssist.databaseGateway.donateDataManipulator.recordPremiumEffectPurchase(player, effect)
              _ <- transactionResult match {
                case ActionStatus.Ok =>
                  IO {
                    val state = playerData.skillEffectState
                    playerData.skillEffectState = state.copy(obtainedEffects = state.obtainedEffects + effect)
                  } >> sequentialEffect(
                    MessageEffect(s"${LIGHT_PURPLE}プレミアムエフェクト：${effect.nameOnUI}$RESET$LIGHT_PURPLE${BOLD}を解除しました"),
                    FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f)
                  )(player)
                case ActionStatus.Fail =>
                  MessageEffect("購入履歴が正しく記録されませんでした。管理者に報告してください。")(player)
              }
            } yield ()
          }
      } yield ()

    effect match {
      case ActiveSkillEffect.NoEffect => setEffectSelectionTo(effect)(player)
      case effect: UnlockableActiveSkillEffect =>
        for {
          unlocked <- IO { playerData.skillEffectState.obtainedEffects.contains(effect) }
          _ <-
            if (unlocked)
              setEffectSelectionTo(effect)(player)
            else effect match {
              // 所持していないため、開放しなければならない
              case effect: ActiveSkillNormalEffect => unlockNormalEffect(effect)
              case effect: ActiveSkillPremiumEffect => unlockPremiumEffect(effect)
            }
        } yield ()
    }
  }

  private case class ButtonComputations(player: Player) {
    import player._

    def effectButton(effect: UnlockableActiveSkillEffect): IO[Button] = {
      val itemStackComputation = IO {
        val kindOfPointToUse = effect match {
          case _: ActiveSkillNormalEffect => "エフェクトポイント"
          case _: ActiveSkillPremiumEffect => "プレミアムエフェクトポイント"
        }

        val playerData = SeichiAssist.playermap(player.getUniqueId)
        if (playerData.skillEffectState.obtainedEffects.contains(effect)) {
          val partialBuilder = new IconItemStackBuilder(effect.materialOnUI)
            .title(effect.nameOnUI)
            .lore(List(
              s"$RESET$GREEN${effect.explanation}",
              s"$RESET$DARK_RED${UNDERLINE}クリックでセット"
            ))

          if (playerData.skillEffectState.selection == effect) {
            partialBuilder.enchanted()
          }

          partialBuilder.build()
        } else {
          new IconItemStackBuilder(Material.BEDROCK)
            .title(effect.nameOnUI)
            .lore(List(
              s"$RESET$GREEN${effect.explanation}",
              s"$RESET${YELLOW}必要$kindOfPointToUse：${effect.usePoint}",
              s"$RESET$AQUA${UNDERLINE}クリックで解除"
            ))
            .build()
        }
      }

      itemStackComputation.map(itemStack =>
        ReloadingButton(ActiveSkillEffectMenu)(
          Button(
            itemStack,
            LeftClickButtonEffect(unlockOrSet(effect))
          )
        )
      )
    }

    val effectDataButton: IO[Button] =
      for {
        premiumEffectPoint <- SeichiAssist.databaseGateway.donateDataManipulator.currentPremiumPointFor(player)
        button <-
          IO {
            val playerData = SeichiAssist.playermap(getUniqueId)

            ReloadingButton(ActiveSkillEffectMenu) {
              Button(
                new SkullItemStackBuilder(getUniqueId)
                  .title(s"$UNDERLINE$BOLD$YELLOW${getName}のスキルエフェクトデータ")
                  .lore(List(
                    s"$RESET${GREEN}現在選択しているエフェクト：${playerData.skillEffectState.selection.nameOnUI}",
                    s"$RESET${YELLOW}使えるエフェクトポイント：${playerData.effectPoint}",
                    s"$RESET$DARK_GRAY※投票すると獲得できます",
                    s"$RESET${LIGHT_PURPLE}使えるプレミアムポイント$premiumEffectPoint",
                    s"$RESET$DARK_GRAY※寄付をすると獲得できます"
                  ))
                  .build(),
                Nil
              )
            }
          }
      } yield button
  }

  private object ConstantButtons {
    val resetEffectButton: Button =
      ReloadingButton(ActiveSkillEffectMenu) {
        Button(
          new IconItemStackBuilder(Material.GLASS)
            .title(s"$UNDERLINE$BOLD${YELLOW}エフェクトを使用しない")
            .lore(s"$RESET$DARK_RED${UNDERLINE}クリックでセット")
            .build(),
          LeftClickButtonEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1f),
            Kleisli(setEffectSelectionTo(NoEffect)),
          )
        )
      }

    val effectPurchaseHistoryMenuButton: Button =
      Button(
        new IconItemStackBuilder(Material.BOOKSHELF)
          .title(s"$UNDERLINE$BOLD${BLUE}プレミアムエフェクト購入履歴")
          .lore(
            s"$RESET$DARK_RED${UNDERLINE}クリックで閲覧",
          )
          .build(),
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1f),
          PremiumPointTransactionHistoryMenu(1).open
        )
      )

    val goBackToSkillMenuButton: Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
        "スキルメニューへ",
        ActiveSkillMenu
      )
  }

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
    val c = ButtonComputations(player)

    import ConstantButtons._
    import c._
    import cats.implicits._
    import eu.timepit.refined.auto._

    val computeDynamicPart = {
      List(
        ChestSlotRef(0, 0) -> effectDataButton
      ) ++ ActiveSkillNormalEffect.values.zipWithIndex.map { case (effect, index) =>
        ChestSlotRef(1, 0) + index -> effectButton(effect)
      } ++ ActiveSkillPremiumEffect.values.zipWithIndex.map { case (effect, index) =>
        ChestSlotRef(3, 0) + index -> effectButton(effect)
      }
    }.traverse(_.sequence)

    val constantPart = {
      List(
        ChestSlotRef(0, 1) -> resetEffectButton,
        ChestSlotRef(0, 2) -> effectPurchaseHistoryMenuButton,
        ChestSlotRef(5, 0) -> goBackToSkillMenuButton
      )
    }.toMap

    for {
      dynamicPart <- computeDynamicPart
    } yield MenuSlotLayout(constantPart ++ dynamicPart)
  }
}
