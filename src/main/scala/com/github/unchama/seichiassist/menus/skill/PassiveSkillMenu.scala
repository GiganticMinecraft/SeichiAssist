package com.github.unchama.seichiassist.menus.skill

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton}
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.targetedeffect._
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.player.PlayerEffects._
import com.github.unchama.targetedeffect.syntax._
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

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}整地スキル切り替え")

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
    import cats.implicits._
    import eu.timepit.refined.auto._

    val buttonComputations = ButtonComputations(player)
    import buttonComputations._

    val constantPart = Map(
      ChestSlotRef(3, 0) -> CommonButtons.openStickMenu
    )

    val dynamicPartComputation = List(
      ChestSlotRef(0, 0) -> computeToggleMultipleBlockTypeDestructionButton,
      ChestSlotRef(0, 1) -> computeToggleChestBreakButton,
      ChestSlotRef(1, 0) -> computeGiganticBerserkButton
    )
      .map(_.sequence)
      .sequence

    for {
      dynamicPart <- dynamicPartComputation
    } yield MenuSlotLayout(constantPart ++ dynamicPart)
  }

  private case class ButtonComputations(player: Player) {

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}
    import player._

    import scala.util.chaining._

    val computeToggleMultipleBlockTypeDestructionButton: IO[Button] = RecomputedButton(IO {
      val openerData = SeichiAssist.playermap(getUniqueId)

      val baseLore = List(
        s"${GREEN}複数種類ブロック同時破壊",
        s"${GRAY}ブロックに対応するツールを無視してスキルで",
        s"${GRAY}破壊可能な全種類のブロックを同時に破壊します",
        s"${DARK_RED}整地ワールドではON/OFFに関わらず同時破壊されます")
      val statusLore = if (openerData.settings.multipleidbreakflag) {
        List(s"${GREEN}ON", s"${DARK_RED}クリックでOFF")
      } else {
        List(s"${RED}OFF", s"${DARK_GREEN}クリックでON")
      }

      Button(
        new IconItemStackBuilder(Material.DIAMOND_PICKAXE)
          .tap { builder => if (openerData.settings.multipleidbreakflag) builder.enchanted() }
          .title(s"$YELLOW$UNDERLINE${BOLD}複数種類同時破壊スキル切替")
          .lore(baseLore ++ statusLore)
          .build(),
        LeftClickButtonEffect {
          if (openerData.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel) {
            sequentialEffect(
              openerData.settings.toggleMultipleIdBreakFlag,
              deferredEffect(IO {
                if (openerData.settings.multipleidbreakflag) {
                  sequentialEffect(
                    s"${GREEN}複数種類同時破壊:ON".asMessageEffect(),
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
                  )
                } else {
                  sequentialEffect(
                    s"${RED}複数種類同時破壊:OFF".asMessageEffect(),
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
                  )
                }
              })
            )
          } else {
            sequentialEffect(
              "整地レベルが足りません".asMessageEffect(),
              FocusedSoundEffect(Sound.BLOCK_GRASS_PLACE, 1f, 0.1f),
            )
          }
        }
      )
    })

    val computeToggleChestBreakButton: IO[Button] = RecomputedButton(IO {
      val openerData = SeichiAssist.playermap(getUniqueId)

      val baseLore = List(
        s"${GREEN}スキルでチェストを破壊するスキル"
      )
      val statusLore = if (openerData.chestflag) {
        List(
          s"${RED}整地ワールドのみで発動中(デフォルト)",
          "",
          s"$DARK_GREEN${UNDERLINE}クリックで切り替え"
        )
      } else {
        List(
          s"${RED}発動しません",
          "",
          s"$DARK_GREEN${UNDERLINE}クリックで切り替え"
        )
      }
      val material = if (openerData.chestflag) Material.DIAMOND_AXE else Material.CHEST

      Button(
        new IconItemStackBuilder(material)
          .title(s"$YELLOW$UNDERLINE${BOLD}チェスト破壊スキル切り替え")
          .lore(baseLore ++ statusLore)
          .build(),
        LeftClickButtonEffect {
          sequentialEffect(
            openerData.toggleChestBreakFlag,
            deferredEffect(IO {
              if (openerData.chestflag) {
                sequentialEffect(
                  s"${GREEN}スキルでのチェスト破壊を有効化しました。".asMessageEffect(),
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
                )
              } else {
                sequentialEffect(
                  s"${RED}スキルでのチェスト破壊を無効化しました。".asMessageEffect(),
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
                )
              }
            })
          )
        })
    })

    val computeGiganticBerserkButton: IO[Button] = RecomputedButton(IO {
      val openerData = SeichiAssist.playermap(getUniqueId)

      val material = if (openerData.level < 10) Material.STICK else openerData.giganticBerserk.materialOnUI()
      val baseLore = if (openerData.level < 10) {
        List(s"${WHITE}このパッシブスキルは", s"${WHITE}整地レベルが10以上になると解放されます")
      } else {
        List(
          s"${RED}敵MOBを倒した時",
          s"${RED}その魂を吸収しマナへと変換するスキル",
          s"${DARK_GRAY}※成功率は高くなく",
          s"${DARK_GRAY}整地中でなければその効果を発揮しない",
          "",
          s"${DARK_GRAY}実装は試験的であり、変更される場合があります"
        )
      }
      val lengthInfoLore = if (openerData.giganticBerserk.reachedLimit()) {
        List(s"${GRAY}MOBの魂を極限まで吸収し最大限の力を発揮する")
      } else {
        List(
          s"${GRAY}MOBの魂を${openerData.giganticBerserk.requiredExpToNextLevel()}回吸収すると更なる力が得られる",
          s"$GRAY${openerData.giganticBerserk.exp}/${openerData.giganticBerserk.requiredExpToNextLevel()}"
        )
      }
      val levelInfoLore = List(s"${GRAY}現在 ${openerData.giganticBerserk.level + 1}レベル,回復率 ${100 * openerData.giganticBerserk.manaRegenerationProbability()}%")
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
          .tap(builder => if (openerData.giganticBerserk.canEvolve || openerData.giganticBerserk.reachedLimit()) {
            builder.enchanted()
          })
          .build(),
        LeftClickButtonEffect {
          if (openerData.level < 10) {
            sequentialEffect(
              (s"${WHITE}パッシブスキル$YELLOW$UNDERLINE${BOLD}" +
                s"Gigantic$RED$UNDERLINE${BOLD}Berserk${WHITE}はレベル10以上から使用可能です").asMessageEffect(),
              FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)
            )
          } else if (openerData.giganticBerserk.canEvolve) {
            //TODO: メニューに置き換える
            sequentialEffect(
              computedEffect(player => openInventoryEffect(MenuInventoryData.getGiganticBerserkEvolutionMenu(player))),
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
            )
          } else {
            s"${RED}進化条件を満たしていません".asMessageEffect()
          }
        }
      )
    })
  }

}
