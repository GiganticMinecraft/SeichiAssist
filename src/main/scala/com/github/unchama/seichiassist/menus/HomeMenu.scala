package com.github.unchama.seichiassist.menus

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.targetedeffect._
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.player.PlayerEffects._
import com.github.unchama.targetedeffect.syntax._
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

/**
 * ホームメニュー
 *
 * Created by karayuu on 2019/12/14
 */
object HomeMenu extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}
  import eu.timepit.refined.auto._

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(3.chestRows, s"$DARK_PURPLE${BOLD}ホームメニュー")

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
    import eu.timepit.refined._
    import eu.timepit.refined.auto._
    import eu.timepit.refined.numeric._

    val buttonComputations = ButtonComputations(player)
    import buttonComputations._

    val homePointPart = Map(
      ChestSlotRef(0, 0) -> ConstantButtons.warpToHomePointButton,
      ChestSlotRef(2, 0) -> ConstantButtons.setHomeButtonButton
    )

    val subHomePointPart = for {
      subHomeNumber <- 1 to SeichiAssist.seichiAssistConfig.getSubHomeMax
    } yield {
      val column = refineV[Interval.ClosedOpen[0, 9]](subHomeNumber + 1)
      column match {
        case Right(value) => Map(
          ChestSlotRef(0, value) -> ConstantButtons.warpToSubHomePointButton(subHomeNumber),
          ChestSlotRef(2, value) -> ConstantButtons.setSubHomeButton(subHomeNumber)
        )
        case Left(_) => throw new RuntimeException("This branch should not be reached.")
      }
    }

    import cats.implicits._

    val dynamicPartComputation = (for {
      subHomeNumber <- 1 to SeichiAssist.seichiAssistConfig.getSubHomeMax
    } yield {
      val column = refineV[Interval.ClosedOpen[0, 9]](subHomeNumber + 1)
      column match {
        case Right(value) => ChestSlotRef(1, value) -> setSubHomeNameButton(subHomeNumber)
        case Left(_) => throw new RuntimeException("This branch should not be reached.")
      }
    }.sequence)
      .toList
      .sequence

    for {
      dynamicPart <- dynamicPartComputation
    } yield MenuSlotLayout(homePointPart ++ subHomePointPart.flatten ++ dynamicPart.toMap)
  }

  private object ConstantButtons {
    val warpToHomePointButton: Button = {
      Button(
        new IconItemStackBuilder(Material.COMPASS)
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームポイントにワープ")
          .lore(List(
            s"${GRAY}あらかじめ設定した", s"${GRAY}ホームポイントにワープします",
            s"${DARK_GRAY}うまく機能しないときは", s"${DARK_GRAY}再接続してみてください",
            s"$DARK_RED${UNDERLINE}クリックでワープ", s"${DARK_GRAY}command->[/home]"
          ))
          .build(),
        LeftClickButtonEffect {
          "home".asCommandEffect()
        }
      )
    }

    val setHomeButtonButton: Button = {
      Button(
        new IconItemStackBuilder(Material.BED)
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームポイントを設定")
          .lore(List(
            s"${GRAY}現在位置をホームポイント", s"${GRAY}として設定します",
            s"$DARK_GRAY※確認メニューが開きます", s"$DARK_RED${UNDERLINE}クリックで設定",
            s"${DARK_GRAY}command->[/sethome]"
          ))
          .build(),
        LeftClickButtonEffect {
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          ConfirmationMenu(None).open
        }
      )
    }

    def warpToSubHomePointButton(subHomeNumber: Int): Button =
      Button(
        new IconItemStackBuilder(Material.COMPASS)
          .title(s"$YELLOW$UNDERLINE${BOLD}サブホームポイント${subHomeNumber}にワープ")
          .lore(List(
            s"${GRAY}あらかじめ設定した", s"${GRAY}サブホームポイント${subHomeNumber}にワープします",
            s"${DARK_GRAY}うまく機能しない時は", s"${DARK_GRAY}再接続してみてください",
            s"$DARK_RED${UNDERLINE}クリックでワープ", s"${DARK_GRAY}command->[/subhome warp $subHomeNumber]"
          ))
          .build(),
        LeftClickButtonEffect {
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            s"subhome warp $subHomeNumber".asCommandEffect()
          )
        }
      )

    def setSubHomeButton(subHomeNumber: Int): Button =
      Button(
        new IconItemStackBuilder(Material.BED)
          .title(s"$YELLOW$UNDERLINE${BOLD}サブホームポイント${subHomeNumber}を設定")
          .lore(List(
            s"${GRAY}現在位置をサブホームポイント$subHomeNumber",
            s"${GRAY}として設定します",
            s"$DARK_GRAY※確認メニューが開きます",
            s"$DARK_RED${UNDERLINE}クリックで設定",
            s"${DARK_GRAY}command->[/subhome set $subHomeNumber]"
          ))
          .build(),
        LeftClickButtonEffect {
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
            ConfirmationMenu(Some(subHomeNumber)).open
          )
        }
      )
  }

  private case class ButtonComputations(player: Player) {

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.sync
    import player._

    def setSubHomeNameButton(subHomeNumber: Int): IO[Button] = IO {
      val openerData = SeichiAssist.playermap(getUniqueId)
      val maybeLocation = openerData.getSubHomeLocation(subHomeNumber - 1)
      val lore = maybeLocation match {
        case None => List(s"${GRAY}サブホームポイント$subHomeNumber", s"${GRAY}ポイント未設定")
        case Some(location) =>
          val worldName = ManagedWorld.fromBukkitWorld(location.getWorld).map(_.japaneseName)
            .getOrElse(location.getWorld.getName)
          List(
            s"${GRAY}サブホームポイント${subHomeNumber}は",
            s"$GRAY${openerData.getSubHomeName(subHomeNumber - 1)}",
            s"${GRAY}と名付けられています",
            s"$DARK_RED${UNDERLINE}クリックで名称変更",
            s"${DARK_GRAY}command->[/subhome name $subHomeNumber]",
            s"$GRAY$worldName x:${location.getBlockX} y:${location.getBlockY} z:${location.getBlockZ}"
          )
      }

      Button(
        new IconItemStackBuilder(Material.PAPER)
          .title(s"$YELLOW$UNDERLINE${BOLD}サブホームポイント${subHomeNumber}の情報")
          .lore(lore)
          .build(),
        LeftClickButtonEffect {
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            s"subhome name $subHomeNumber".asCommandEffect(),
            closeInventoryEffect
          )
        }
      )
    }
  }

  private case class ConfirmationMenu(changeSubHomeNumber: Option[Int], subHomeName: String = "") extends Menu {
    /**
     * メニューのサイズとタイトルに関する情報
     */
    override val frame: MenuFrame = MenuFrame(3.chestRows, s"$RED${BOLD}ホームポイントを変更しますか")

    /**
     * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
     */
    override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
      val baseSlotMap = Map(
        ChestSlotRef(1, 2) -> changeButton,
        ChestSlotRef(1, 6) -> cancelButton
      )
      val slotMap = changeSubHomeNumber match {
        case None => baseSlotMap
        case _ => baseSlotMap ++ Map(ChestSlotRef(0, 4) -> informationButton)
      }
      IO.pure(MenuSlotLayout(slotMap))
    }

    val changeButton: Button =
      Button(
        new IconItemStackBuilder(Material.WOOL, durability = 5)
          .title(s"${GREEN}変更する")
          .build(),
        LeftClickButtonEffect {
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            changeSubHomeNumber match {
              case None => "sethome".asCommandEffect()
              case Some(homeNumber) => s"subhome set $homeNumber".asCommandEffect()
            },
            closeInventoryEffect
          )
        }
      )

    val cancelButton: Button =
      Button(
        new IconItemStackBuilder(Material.WOOL, durability = 14)
          .title(s"${RED}変更しない")
          .build(),
        LeftClickButtonEffect {
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          HomeMenu.open
        }
      )

    val informationButton: Button =
      Button(
        new IconItemStackBuilder(Material.PAPER)
          .title(s"${GREEN}設定するサブホームポイントの情報")
          .lore(List(
            s"${GRAY}No.${changeSubHomeNumber.getOrElse(0)}",
            s"${GRAY}名称：$subHomeName"
          ))
          .build()
      )
  }

}
