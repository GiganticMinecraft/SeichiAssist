package com.github.unchama.seichiassist.menus.giganticberserk

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.targetedeffect.SequentialEffect
import org.bukkit.ChatColor.{BOLD, DARK_RED, GREEN, LIGHT_PURPLE, RESET, WHITE}
import org.bukkit.Material
import org.bukkit.entity.Player

object GiganticBerserkAfterEvolutionMenu extends Menu {
  /**
   * メニューを開く操作に必要な環境情報の型。
   * 例えば、メニューが利用するAPIなどをここを通して渡すことができる。
   */
  override type Environment = Unit
  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(6.chestRows, s"$LIGHT_PURPLE${BOLD}スキルを進化させました")

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout] = IO {
    val pd = SeichiAssist.playermap(player.getUniqueId)
    val stage = pd.giganticBerserk.stage
    val color: Short = stage match {
      case 0 => 12
      case 1 => 15
      case 2 => 4
      case 3 => 0
      case 4 => 3
      case 5 => 12
      case _ => throw new AssertionError("This statement shouldn't be reached!")
    }

    val builder = new IconItemStackBuilder(Material.STAINED_GLASS_PANE, color)
      .title(" ")

    if (stage >= 4) {
      // TODO: Original: DAMAGE_ALL<1, Hidden>
      builder.enchanted()
    }

    val glass = builder.build()
    val glasses = Seq(6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41).map((_, new Button(glass, List()))).toMap
    val stick = new IconItemStackBuilder(Material.STICK)
      .amount(1)
      .title(" ")
      .lore()
      .build()

    val sticks = Seq(30, 39, 40, 47).map((_, new Button(stick, List())))
    val executedButton = (
      31,
      new Button(
        new IconItemStackBuilder(Material.NETHER_STAR)
          .title(s"${WHITE}スキルを進化させました！")
          .lore(
            s"$RESET${GREEN}スキルの秘めたる力を解放することで、マナ回復量が増加し",
            s"$RESET${DARK_RED}スキルはより魂を求めるようになりました"
          )
          .build(),
        List(
          new FilteredButtonEffect(
            ClickEventFilter.LEFT_CLICK,
            SequentialEffect(

            )
          )
        )
      )
    )

    new MenuSlotLayout(glasses ++ sticks + executedButton)
  }
}
