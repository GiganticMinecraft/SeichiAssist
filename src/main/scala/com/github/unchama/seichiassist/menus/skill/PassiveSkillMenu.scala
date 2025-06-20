package com.github.unchama.seichiassist.menus.skill

import cats.effect.{IO, SyncIO}
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton}
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.BreakSkillTargetConfigAPI
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.BreakSkillTargetConfigKey
import com.github.unchama.seichiassist.subsystems.breaksuppressionpreference.BreakSuppressionPreferenceAPI
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.targetedeffect._
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.player.PlayerEffects._
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

/**
 * パッシブスキル選択メニュー
 *
 * Created by karayuu on 2019/12/05
 */
object PassiveSkillMenu extends Menu {

  import com.github.unchama.menuinventory.syntax._

  class Environment(
    implicit val breakCountApi: BreakCountAPI[IO, SyncIO, Player],
    implicit val breakSkillTargetConfigAPI: BreakSkillTargetConfigAPI[IO, Player],
    implicit val breakSuppressionPreferenceAPI: BreakSuppressionPreferenceAPI[IO, Player],
    val ioCanOpenFirstPage: IO CanOpen FirstPage.type,
    implicit val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
  )

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}整地スキル切り替え")

  /**
   * @return
   *   `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import cats.implicits._
    import environment._
    import eu.timepit.refined.auto._

    val buttonComputations = new ButtonComputations(player)
    import buttonComputations._

    val constantPart = Map(ChestSlotRef(3, 0) -> CommonButtons.openStickMenu)

    val dynamicPartComputation = List(
      ChestSlotRef(0, 0) -> computeToggleMultipleBlockTypeDestructionButton,
      ChestSlotRef(0, 1) -> computeToggleChestBreakButton,
      ChestSlotRef(0, 2) -> computeToggleManaFullyConsumedBreakStopButton,
      ChestSlotRef(1, 0) -> computeGiganticBerserkButton,
      ChestSlotRef(1, 1) -> computeToggleNetherQuartzBlockButton
    ).traverse(_.sequence)

    for {
      dynamicPart <- dynamicPartComputation
    } yield MenuSlotLayout(constantPart ++ dynamicPart)
  }

  private class ButtonComputations(player: Player)(implicit environment: Environment) {

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{
      layoutPreparationContext,
      onMainThread
    }
    import player._

    import scala.util.chaining._

    val computeToggleMultipleBlockTypeDestructionButton: IO[Button] = RecomputedButton {
      environment
        .breakCountApi
        .seichiAmountDataRepository(player)
        .read
        .toIO
        .flatMap(amountData =>
          IO {
            val level = amountData.levelCorrespondingToExp.level
            val openerData = SeichiAssist.playermap(getUniqueId)

            val baseLore = List(
              s"${GREEN}複数種類ブロック同時破壊",
              s"${GRAY}ブロックに対応するツールを無視してスキルで",
              s"${GRAY}破壊可能な全種類のブロックを同時に破壊します",
              s"${DARK_RED}整地ワールドではON/OFFに関わらず同時破壊されます"
            )
            val statusLore =
              if (openerData.settings.performMultipleIDBlockBreakWhenOutsideSeichiWorld) {
                List(s"${GREEN}ON", s"${DARK_RED}クリックでOFF")
              } else {
                List(s"${RED}OFF", s"${DARK_GREEN}クリックでON")
              }

            Button(
              new IconItemStackBuilder(Material.DIAMOND_PICKAXE)
                .tap { builder =>
                  if (openerData.settings.performMultipleIDBlockBreakWhenOutsideSeichiWorld)
                    builder.enchanted()
                }
                .title(s"$YELLOW$UNDERLINE${BOLD}複数種類同時破壊スキル切替")
                .lore(baseLore ++ statusLore)
                .build(),
              LeftClickButtonEffect {
                if (level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreakLevel) {
                  SequentialEffect(
                    openerData.settings.toggleMultipleIdBreakFlag,
                    DeferredEffect(IO {
                      if (
                        openerData.settings.performMultipleIDBlockBreakWhenOutsideSeichiWorld
                      ) {
                        SequentialEffect(
                          MessageEffect(s"${GREEN}複数種類同時破壊:ON"),
                          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
                        )
                      } else {
                        SequentialEffect(
                          MessageEffect(s"${RED}複数種類同時破壊:OFF"),
                          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
                        )
                      }
                    })
                  )
                } else {
                  SequentialEffect(
                    MessageEffect("整地Lvが足りません"),
                    FocusedSoundEffect(Sound.BLOCK_GRASS_PLACE, 1f, 0.1f)
                  )
                }
              }
            )
          }
        )
    }

    import environment._

    val computeToggleChestBreakButton: IO[Button] = RecomputedButton(for {
      originalBreakChestConfig <- breakSkillTargetConfigAPI
        .breakSkillTargetConfig(player, BreakSkillTargetConfigKey.Chest)
    } yield {
      val baseLore = List(s"${GREEN}スキルでチェストを破壊するスキル")
      val statusLore = if (originalBreakChestConfig) {
        List(s"${RED}整地ワールドのみで発動中(デフォルト)", "", s"$DARK_GREEN${UNDERLINE}クリックで切り替え")
      } else {
        List(s"${RED}発動しません", "", s"$DARK_GREEN${UNDERLINE}クリックで切り替え")
      }
      val material = if (originalBreakChestConfig) Material.DIAMOND_AXE else Material.CHEST

      Button(
        new IconItemStackBuilder(material)
          .title(s"$YELLOW$UNDERLINE${BOLD}チェスト破壊スキル切り替え")
          .lore(baseLore ++ statusLore)
          .build(),
        LeftClickButtonEffect {
          SequentialEffect(
            breakSkillTargetConfigAPI.toggleBreakSkillTargetConfig(
              BreakSkillTargetConfigKey.Chest
            ),
            DeferredEffect(IO {
              if (!originalBreakChestConfig) {
                SequentialEffect(
                  MessageEffect(s"${GREEN}スキルでのチェスト破壊を有効化しました。"),
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
                )
              } else {
                SequentialEffect(
                  MessageEffect(s"${RED}スキルでのチェスト破壊を無効化しました。"),
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
                )
              }
            })
          )
        }
      )
    })

    val computeToggleNetherQuartzBlockButton: IO[Button] = RecomputedButton(for {
      originalBreakQuartz <- breakSkillTargetConfigAPI
        .breakSkillTargetConfig(player, BreakSkillTargetConfigKey.MadeFromNetherQuartz)
    } yield {
      val baseLore = List(s"${YELLOW}スキルでネザー水晶類ブロックを破壊するスキル")
      val statusLore = if (originalBreakQuartz) {
        List(s"${GREEN}ON (スキルでネザー水晶類ブロックを破壊します。)", s"${DARK_RED}クリックでOFF")
      } else {
        List(s"${RED}OFF (スキルでネザー水晶類ブロックを破壊しません。)", s"${DARK_GREEN}クリックでON")
      }

      Button(
        new IconItemStackBuilder(Material.QUARTZ_BLOCK)
          .tap { builder =>
            if (originalBreakQuartz)
              builder.enchanted()
          }
          .title(s"$WHITE$UNDERLINE${BOLD}ネザー水晶類ブロック破壊スキル切り替え")
          .lore(baseLore ++ statusLore)
          .build(),
        LeftClickButtonEffect {
          SequentialEffect(
            breakSkillTargetConfigAPI.toggleBreakSkillTargetConfig(
              BreakSkillTargetConfigKey.MadeFromNetherQuartz
            ),
            DeferredEffect(IO {
              if (!originalBreakQuartz) {
                SequentialEffect(
                  MessageEffect(s"${GREEN}スキルでのネザー水晶類ブロック破壊を有効化しました。"),
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
                )
              } else {
                SequentialEffect(
                  MessageEffect(s"${RED}スキルでのネザー水晶類ブロック破壊を無効化しました。"),
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
                )
              }
            })
          )
        }
      )
    })

    val computeToggleManaFullyConsumedBreakStopButton: IO[Button] = RecomputedButton(for {
      isBreakSuppressionEnabled <- breakSuppressionPreferenceAPI.isBreakSuppressionEnabled(
        player
      )

    } yield {
      val baseLore = List(s"${YELLOW}マナ切れでブロック破壊を止めるスキル")
      val statusLore = if (isBreakSuppressionEnabled) {
        List(s"${GREEN}ON (マナが切れるとブロック破壊を止めます。)", s"${DARK_RED}クリックでOFF")
      } else {
        List(s"${RED}OFF (マナが切れてもブロック破壊を続けます。)", s"${DARK_GREEN}クリックでON")
      }

      Button(
        new IconItemStackBuilder(Material.LAPIS_LAZULI)
          .tap { builder =>
            if (isBreakSuppressionEnabled)
              builder.enchanted()
          }
          .title(s"$WHITE$UNDERLINE${BOLD}マナ切れでブロック破壊を止めるスキル切り替え")
          .lore(baseLore ++ statusLore)
          .build(),
        LeftClickButtonEffect {
          SequentialEffect(
            breakSuppressionPreferenceAPI.toggleBreakSuppression,
            DeferredEffect(IO {
              if (isBreakSuppressionEnabled) {
                SequentialEffect(
                  MessageEffect(s"${RED}マナが切れたらブロック破壊を止めるスキルを無効化しました。"),
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
                )
              } else {
                SequentialEffect(
                  MessageEffect(s"${GREEN}マナが切れたらブロック破壊を止めるスキルを有効化しました。"),
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
                )
              }
            })
          )
        }
      )
    })

    val computeGiganticBerserkButton: IO[Button] = RecomputedButton {
      environment
        .breakCountApi
        .seichiAmountDataRepository(player)
        .read
        .toIO
        .flatMap(amountData =>
          IO {
            val level = amountData.levelCorrespondingToExp.level

            val openerData = SeichiAssist.playermap(getUniqueId)

            val material =
              if (level < 10) Material.STICK else openerData.giganticBerserk.materialOnUI()
            val baseLore = if (level < 10) {
              List(s"${WHITE}このパッシブスキルは", s"${WHITE}整地Lvが10以上になると解放されます")
            } else {
              List(
                s"${RED}敵MOBを倒した時",
                s"${RED}その魂を吸収しマナへと変換するスキル",
                s"$DARK_GRAY※成功率は高くなく",
                s"${DARK_GRAY}整地中でなければその効果を発揮しない",
                "",
                s"${DARK_GRAY}実装は試験的であり、変更される場合があります"
              )
            }
            val lengthInfoLore = if (openerData.giganticBerserk.reachedLimit()) {
              List(
                s"${GRAY}MOBの魂を極限まで吸収し最大限の力を発揮する",
                s"${GRAY}MOB討伐総数:${openerData.giganticBerserk.totalNumberOfKilledEnemies}"
              )
            } else {
              List(
                s"${GRAY}MOBの魂を${openerData.giganticBerserk.requiredExpToNextLevel()}回吸収すると更なる力が得られる",
                s"$GRAY${openerData.giganticBerserk.exp}/${openerData.giganticBerserk.requiredExpToNextLevel()}",
                s"${GRAY}MOB討伐総数:${openerData.giganticBerserk.totalNumberOfKilledEnemies}"
              )
            }
            val probability = 100 * openerData.giganticBerserk.manaRegenerationProbability()
            val formatted = f"$probability%2.0f"
            // 細かい数字が表示されないようにする
            val levelInfoLore =
              List(s"${GRAY}現在 ${openerData.giganticBerserk.level + 1}レベル,回復率 $formatted%")
            val evolutionLore = if (openerData.giganticBerserk.canEvolve) {
              List(
                "",
                s"${DARK_RED}沢山の魂を吸収したことで",
                s"${DARK_RED}スキルの秘めたる力を解放できそうだ…!",
                s"$DARK_RED${UNDERLINE}クリックで開放する"
              )
            } else {
              List()
            }

            Button(
              new IconItemStackBuilder(material)
                .title(s"$YELLOW$UNDERLINE${BOLD}Gigantic$RED$UNDERLINE${BOLD}Berserk")
                .lore(baseLore ++ lengthInfoLore ++ levelInfoLore ++ evolutionLore)
                .tap(builder =>
                  if (
                    openerData
                      .giganticBerserk
                      .canEvolve || openerData.giganticBerserk.reachedLimit()
                  ) {
                    builder.enchanted()
                  }
                )
                .build(),
              LeftClickButtonEffect {
                if (level < 10) {
                  val message =
                    s"${WHITE}パッシブスキル$YELLOW$UNDERLINE${BOLD}Gigantic$RED$UNDERLINE${BOLD}Berserk${WHITE}はレベル10以上から使用可能です"
                  SequentialEffect(
                    MessageEffect(message),
                    FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)
                  )
                } else if (openerData.giganticBerserk.canEvolve) {
                  // TODO: メニューに置き換える
                  SequentialEffect(
                    ComputedEffect(player =>
                      openInventoryEffect(
                        MenuInventoryData.getGiganticBerserkBeforeEvolutionMenu(player)
                      )
                    ),
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
                  )
                } else {
                  MessageEffect(s"${RED}進化条件を満たしていません")
                }
              }
            )
          }
        )
    }
  }

}
