package com.github.unchama.seichiassist.menus.achievement.group

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.generic.CachedFunction
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.achievement.NicknameMapping.NicknameCombination
import com.github.unchama.seichiassist.achievement.SeichiAchievement.{AutoUnlocked, Hidden, ManuallyUnlocked, Normal}
import com.github.unchama.seichiassist.achievement.{AchievementConditions, NicknameMapping, SeichiAchievement}
import com.github.unchama.seichiassist.menus.ColorScheme
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

object AchievementGroupMenuButtons {
  val achievementButton: ((SeichiAchievement, Boolean)) => Button =
    CachedFunction { case (achievement, hasUnlocked) =>
      val itemStack = {
        val material = if (hasUnlocked) Material.DIAMOND_BLOCK else Material.BEDROCK
        val title = {
          val displayTitleName =
            if (hasUnlocked) NicknameMapping.getTitleFor(achievement) else "???"

          s"$YELLOW$UNDERLINE${BOLD}No${achievement.id}「$displayTitleName」"
        }

        val lore = {
          val conditionDescriptions =
            achievement match {
              case normal: SeichiAchievement.Normal[_] =>
                List(normal.condition.parameterizedDescription)
              case hidden: SeichiAchievement.Hidden[_] =>
                val description =
                  if (hasUnlocked)
                    hidden.condition.underlying.parameterizedDescription
                  else
                    hidden.condition.maskedDescription
                List(description)
              case g: SeichiAchievement.GrantedByConsole =>
                List(g.condition) ++ g.explanation.getOrElse(Nil)
            }

          val unlockSchemeDescription =
            achievement match {
              case _: AutoUnlocked =>
                List(s"$RESET$RED※この実績は自動解禁式です。")
              case m: ManuallyUnlocked =>
                m match {
                  case _: Hidden[_] =>
                    List(s"$RESET$RED※この実績は手動解禁式です。")
                  case _ =>
                    if (hasUnlocked)
                      List()
                    else
                      List(s"$RESET$GREEN※クリックで実績に挑戦できます")
                }
              case _ =>
                List(s"$RESET$RED※この実績は配布解禁式です。")
            }

          val hiddenDescription =
            achievement match {
              case _: Hidden[_] => List(s"$RESET${AQUA}こちらは【隠し実績】となります")
              case _ => Nil
            }

          conditionDescriptions.map(s"$RESET$RED" + _) ++
            unlockSchemeDescription ++
            hiddenDescription
        }

        new IconItemStackBuilder(material)
          .title(title)
          .lore(lore)
          .build()
      }

      val clickEffect = {
        import com.github.unchama.targetedeffect._
        import com.github.unchama.targetedeffect.syntax._

        val clickSound = FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

        val effect =
          if (hasUnlocked) {
            def setNickname(player: Player): Unit = {
              val NicknameCombination(firstId, secondId, thirdId) =
                NicknameMapping.getNicknameCombinationFor(achievement)

              SeichiAssist
                .playermap(player.getUniqueId)
                .updateNickname(firstId.getOrElse(0), secondId.getOrElse(0), thirdId.getOrElse(0))
              player.sendMessage(s"二つ名「${NicknameMapping.getTitleFor(achievement)}」が設定されました。")
            }

            delay(setNickname)
          } else {
            achievement match {
              case _: AutoUnlocked =>
                s"${RED}この実績は自動解禁式です。毎分の処理をお待ちください。".asMessageEffect()
              case achievement: ManuallyUnlocked =>
                achievement match {
                  case achievement: Normal[_] =>
                    Kleisli { player: Player =>
                      for {
                        shouldUnlock <- achievement.condition.shouldUnlock(player)
                        _ <- if (shouldUnlock) IO {
                          SeichiAssist.playermap(player.getUniqueId).TitleFlags.addOne(achievement.id)
                          player.sendMessage(s"実績No${achievement.id}を解除しました！おめでとうございます！")
                        } else {
                          s"${RED}実績No${achievement.id}は条件を満たしていません。".asMessageEffect()(player)
                        }
                      } yield ()
                    }
                  case _ =>
                    s"$RESET$RED※この実績は手動解禁式です。".asMessageEffect()
                }
              case _ =>
                s"$RED※この実績は配布解禁式です。運営チームからの配布タイミングを逃さないようご注意ください。".asMessageEffect()
            }
          }

        sequentialEffect(clickSound, effect)
      }

      Button(itemStack, LeftClickButtonEffect(clickEffect))
    }

  import com.github.unchama.targetedeffect._
  import com.github.unchama.targetedeffect.syntax._

  // 実績8003を解除するためのボタン
  val unlock8003Button: Button = Button(
    new IconItemStackBuilder(Material.EMERALD_BLOCK)
      .title(ColorScheme.navigation("タイムカード、切りましょ？"))
      .lore(s"$RESET$RED※何かが起こります※")
      .build(),
    LeftClickButtonEffect(
      FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
      "お疲れ様でした！今日のお給料の代わりに二つ名をどうぞ！".asMessageEffect(),
      delay { player => SeichiAssist.playermap(player.getUniqueId).TitleFlags.addOne(8003) }
    )
  )

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

  def entryComputationFor(viewer: Player): GroupMenuEntry => IO[Button] = {
    case AchievementEntry(achievement) => RecomputedButton {
      for {
        hasObtained <- IO { SeichiAssist.playermap(viewer.getUniqueId).TitleFlags.contains(achievement.id) }
        shouldDisplayToUI <- achievement match {
          case hidden: Hidden[_] => hidden.condition.shouldDisplayToUI(viewer)
          case _ => IO.pure(true)
        }
      } yield if (hasObtained || shouldDisplayToUI) achievementButton(achievement, hasObtained) else Button.empty
    }

    case Achievement8003UnlockEntry => RecomputedButton {
      for {
        hasObtained8003 <- IO { SeichiAssist.playermap(viewer.getUniqueId).TitleFlags.contains(8003) }
        shouldDisplayToUI <-
          if (hasObtained8003) IO.pure(false)
          else AchievementConditions.SecretAchievementConditions.unlockConditionFor8003(viewer)
      } yield if (shouldDisplayToUI) unlock8003Button else Button.empty
    }
  }
}
