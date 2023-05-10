package com.github.unchama.seichiassist.menus.skill

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, ReloadingButton}
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillEffect.NoEffect
import com.github.unchama.seichiassist.seichiskill.effect.{
  ActiveSkillEffect,
  ActiveSkillNormalEffect,
  ActiveSkillPremiumEffect,
  UnlockableActiveSkillEffect
}
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.domain.EffectPoint
import com.github.unchama.seichiassist.subsystems.donate.DonatePremiumPointAPI
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import net.md_5.bungee.api.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

object ActiveSkillEffectMenu extends Menu {

  import cats.implicits._
  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
  import com.github.unchama.targetedeffect._

  class Environment(
    implicit val ioCanOpenActiveSkillEffectMenu: IO CanOpen ActiveSkillEffectMenu.type,
    val ioCanOpenActiveSkillMenu: IO CanOpen ActiveSkillMenu.type,
    val ioCanOpenTransactionHistoryMenu: IO CanOpen PremiumPointTransactionHistoryMenu,
    val ioOnMainThread: OnMinecraftServerThread[IO],
    val voteAPI: VoteAPI[IO, Player],
    val donateAPI: DonatePremiumPointAPI[IO]
  )

  override val frame: MenuFrame = MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}整地スキルエフェクト選択")

  private def setEffectSelectionTo(effect: ActiveSkillEffect)(player: Player): IO[Unit] = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)

    MessageEffect(s"${GREEN}エフェクト：${effect.nameOnUI} が選択されました")(player) >> IO {
      playerData.skillEffectState = playerData.skillEffectState.copy(selection = effect)
    }
  }

  private def unlockOrSet(effect: ActiveSkillEffect)(
    implicit voteAPI: VoteAPI[IO, Player],
    donateAPI: DonatePremiumPointAPI[IO]
  ): TargetedEffect[Player] = Kleisli { player =>
    val playerData = SeichiAssist.playermap(player.getUniqueId)

    def unlockNormalEffect(effect: ActiveSkillNormalEffect): IO[Unit] =
      for {
        effectPoint <- voteAPI.effectPoints(player)
        _ <-
          if (effectPoint.value < effect.usePoint) {
            SequentialEffect(
              MessageEffect(s"${DARK_RED}エフェクトポイントが足りません"),
              FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1.0f, 0.5f)
            ).apply(player)
          } else {
            voteAPI.decreaseEffectPoint(
              player.getUniqueId,
              EffectPoint(effect.usePoint)
            ) >> SequentialEffect(
              UnfocusedEffect {
                val state = playerData.skillEffectState
                playerData.skillEffectState =
                  state.copy(obtainedEffects = state.obtainedEffects + effect)
              },
              MessageEffect(
                s"${LIGHT_PURPLE}エフェクト：${effect.nameOnUI}$RESET$LIGHT_PURPLE${BOLD}を解除しました"
              ),
              FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f)
            ).apply(player)
          }
      } yield ()

    def unlockPremiumEffect(effect: ActiveSkillPremiumEffect): IO[Unit] =
      for {
        premiumEffectPoint <- donateAPI.currentPoint(player.getUniqueId)
        _ <-
          if (premiumEffectPoint.value < effect.usePoint) {
            SequentialEffect(
              MessageEffect(s"${DARK_RED}プレミアムエフェクトポイントが足りません"),
              FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1.0f, 0.5f)
            ).apply(player)
          } else {
            for {
              _ <- donateAPI.useDonatePremiumEffectPoint(player.getUniqueId, effect)
              _ <- IO {
                val state = playerData.skillEffectState
                playerData.skillEffectState =
                  state.copy(obtainedEffects = state.obtainedEffects + effect)
              }
              _ <- SequentialEffect(
                MessageEffect(
                  s"${LIGHT_PURPLE}プレミアムエフェクト：${effect.nameOnUI}$RESET$LIGHT_PURPLE${BOLD}を解除しました"
                ),
                FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f)
              ).apply(player)
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
            else
              effect match {
                // 所持していないため、開放しなければならない
                case effect: ActiveSkillNormalEffect  => unlockNormalEffect(effect)
                case effect: ActiveSkillPremiumEffect => unlockPremiumEffect(effect)
              }
        } yield ()
    }
  }

  private case class ButtonComputations(player: Player)(implicit environment: Environment) {

    import environment._
    import player._

    def effectButton(effect: UnlockableActiveSkillEffect): IO[Button] = {
      val itemStackComputation = IO {
        val kindOfPointToUse = effect match {
          case _: ActiveSkillNormalEffect  => "エフェクトポイント"
          case _: ActiveSkillPremiumEffect => "プレミアムエフェクトポイント"
        }

        val playerData = SeichiAssist.playermap(player.getUniqueId)
        if (playerData.skillEffectState.obtainedEffects.contains(effect)) {
          val partialBuilder = new IconItemStackBuilder(effect.materialOnUI)
            .title(effect.nameOnUI)
            .lore(
              List(s"$RESET$GREEN${effect.explanation}", s"$RESET$DARK_RED${UNDERLINE}クリックでセット")
            )

          if (playerData.skillEffectState.selection == effect) {
            partialBuilder.enchanted()
          }

          partialBuilder.build()
        } else {
          new IconItemStackBuilder(Material.BEDROCK)
            .title(effect.nameOnUI)
            .lore(
              List(
                s"$RESET$GREEN${effect.explanation}",
                s"$RESET${YELLOW}必要$kindOfPointToUse：${effect.usePoint}",
                s"$RESET$AQUA${UNDERLINE}クリックで解除"
              )
            )
            .build()
        }
      }

      itemStackComputation.map(itemStack =>
        ReloadingButton(ActiveSkillEffectMenu)(
          Button(itemStack, LeftClickButtonEffect(unlockOrSet(effect)))
        )
      )
    }

    val effectDataButton: IO[Button] =
      for {
        effectPoints <- voteAPI.effectPoints(player)
        premiumEffectPoint <- donateAPI.currentPoint(player.getUniqueId)
        button <-
          IO {
            val playerData = SeichiAssist.playermap(getUniqueId)

            ReloadingButton(ActiveSkillEffectMenu) {
              Button(
                new SkullItemStackBuilder(getUniqueId)
                  .title(s"$UNDERLINE$BOLD$YELLOW${getName}のスキルエフェクトデータ")
                  .lore(
                    List(
                      s"$RESET${GREEN}現在選択しているエフェクト：${playerData.skillEffectState.selection.nameOnUI}",
                      s"$RESET${YELLOW}使えるエフェクトポイント：${effectPoints.value}",
                      s"$RESET$DARK_GRAY※投票すると獲得できます",
                      s"$RESET${LIGHT_PURPLE}使えるプレミアムポイント${premiumEffectPoint.value}",
                      s"$RESET$DARK_GRAY※寄付をすると獲得できます"
                    )
                  )
                  .build(),
                Nil
              )
            }
          }
      } yield button
  }

  private object ConstantButtons {
    def resetEffectButton(implicit environment: Environment): Button = {
      import environment._
      ReloadingButton(ActiveSkillEffectMenu) {
        Button(
          new IconItemStackBuilder(Material.GLASS)
            .title(s"$UNDERLINE$BOLD${YELLOW}エフェクトを使用しない")
            .lore(s"$RESET$DARK_RED${UNDERLINE}クリックでセット")
            .build(),
          LeftClickButtonEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1f),
            Kleisli(setEffectSelectionTo(NoEffect))
          )
        )
      }
    }

    def effectPurchaseHistoryMenuButton(
      implicit ioCanOpenPremiumPointMenu: IO CanOpen PremiumPointTransactionHistoryMenu
    ): Button =
      Button(
        new IconItemStackBuilder(Material.BOOKSHELF)
          .title(s"$UNDERLINE$BOLD${BLUE}プレミアムエフェクト購入履歴")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで閲覧")
          .build(),
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1f),
          ioCanOpenPremiumPointMenu.open(PremiumPointTransactionHistoryMenu(1))
        )
      )

    def goBackToSkillMenuButton(
      implicit ioCanOpenActiveSkillMenu: IO CanOpen ActiveSkillMenu.type
    ): Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
        "スキルメニューへ",
        ActiveSkillMenu
      )
  }

  /**
   * @return
   *   `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    val c = ButtonComputations(player)

    import ConstantButtons._
    import c._
    import cats.implicits._
    import environment._
    import eu.timepit.refined.auto._

    val computeDynamicPart = {
      List(ChestSlotRef(0, 0) -> effectDataButton) ++ ActiveSkillNormalEffect
        .values
        .zipWithIndex
        .map {
          case (effect, index) =>
            ChestSlotRef(1, 0) + index -> effectButton(effect)
        } ++ ActiveSkillPremiumEffect.values.zipWithIndex.map {
        case (effect, index) =>
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
