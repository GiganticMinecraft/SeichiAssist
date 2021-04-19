package com.github.unchama.seichiassist.menus.giganticberserk

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.data.player.GiganticBerserk
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.{Material, Sound}
import org.bukkit.entity.Player

/**
 * GiganticBerserkの進化前確認画面
 */
object GiganticBerserkBeforeEvolutionMenu extends Menu {
  class Environment(implicit
                    val ioCanOpenGiganticBerserkAfterEvolutionMenu: IO CanOpen GiganticBerserkAfterEvolutionMenu.type)

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}スキルを進化させますか?")

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout] = IO {
    val pd = SeichiAssist.playermap(player.getUniqueId)
    val color: Short = pd.giganticBerserk.stage match {
      case 0 => 12
      case 1 => 15
      case 2 => 4
      case 3 => 0
      case 4 => 3
      case _ => throw new AssertionError("This statement shouldn't be reached!")
    }

    val is = new IconItemStackBuilder(Material.STAINED_GLASS_PANE, color)
      .title(" ")
      .build()

    val stick = new IconItemStackBuilder(Material.STICK)
      .title(" ")
      .lore()
      .build()

    val glasses = Seq(6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41).map(x => (x, new Button(is, List()))).toMap
    val sticks = Set(30, 39, 40, 47).map(x => (x, new Button(stick, List()))).toMap
    val executeButton = (
      31,
      new Button(
        new IconItemStackBuilder(Material.NETHER_STAR)
          .amount(1)
          .title(s"${WHITE}スキルを進化させる")
          .lore(
            s"$RESET${GREEN}進化することにより、スキルの秘めたる力を解放できますが",
            s"$RESET${GREEN}スキルは更に大量の魂を求めるようになり",
            s"$RESET${GREEN}レベル(回復確率)がリセットされます",
            s"$RESET${RED}本当に進化させますか?",
            s"$RESET$DARK_RED${UNDERLINE}クリックで進化させる"
          )
          .build(),
        List(
          new FilteredButtonEffect(
            ClickEventFilter.LEFT_CLICK,
            SequentialEffect(
              // GBのレベルを上げる
              UnfocusedEffect {
                pd.giganticBerserk = GiganticBerserk(0, 0, pd.giganticBerserk.stage + 1)
              },
              FocusedSoundEffect(Sound.BLOCK_END_GATEWAY_SPAWN, 1f, 0.5f),
              FocusedSoundEffect(Sound.ENTITY_ENDERDRAGON_AMBIENT, 1f, 0.8f),
              environment.ioCanOpenGiganticBerserkAfterEvolutionMenu.open(GiganticBerserkAfterEvolutionMenu)
            )
          )
        )
      )
    )

    new MenuSlotLayout(
      glasses ++ sticks + executeButton
    )
  }
}
