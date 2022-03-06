package com.github.unchama.seichiassist.menus

import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.subsystems.subhome.SubHomeReadAPI
import com.github.unchama.seichiassist.subsystems.subhome.domain.{SubHome, SubHomeId}
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.targetedeffect._
import com.github.unchama.targetedeffect.player.PlayerEffects._
import com.github.unchama.targetedeffect.player.{CommandEffect, FocusedSoundEffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

/**
 * ホームメニュー
 *
 * Created by karayuu on 2019/12/14
 */
object HomeMenu extends Menu {

  // TODO: SubHome -> Homeに名前を変更する

  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
  import eu.timepit.refined.auto._

  class Environment(
    implicit val ioCanOpenConfirmationMenu: IO CanOpen ConfirmationMenu,
    val ioCanReadSubHome: SubHomeReadAPI[IO]
  )

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(3.chestRows, s"$DARK_PURPLE${BOLD}ホームメニュー")

  /**
   * @return
   *   `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import eu.timepit.refined._
    import eu.timepit.refined.auto._
    import eu.timepit.refined.numeric._

    val buttonComputations = ButtonComputations(player)
    import buttonComputations._

    val subHomePointPart = for {
      subHomeNumber <- 1 to SeichiAssist.seichiAssistConfig.getSubHomeMax
    } yield {
      val column = refineV[Interval.ClosedOpen[0, 9]](subHomeNumber - 1)
      column match {
        case Right(value) =>
          Map(
            ChestSlotRef(0, value) -> ConstantButtons.warpToSubHomePointButton(subHomeNumber),
            ChestSlotRef(2, value) -> ConstantButtons.setSubHomeButton(subHomeNumber)
          )
        case Left(_) => throw new RuntimeException("This branch should not be reached.")
      }
    }

    import cats.implicits._
    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.asyncShift
    val dynamicPartComputation = (for {
      subHomeNumber <- 1 to SeichiAssist.seichiAssistConfig.getSubHomeMax
    } yield {
      val column = refineV[Interval.ClosedOpen[0, 9]](subHomeNumber - 1)
      implicit val ioCanReadSubHome: SubHomeReadAPI[IO] = environment.ioCanReadSubHome
      column match {
        case Right(value) => ChestSlotRef(1, value) -> setSubHomeNameButton[IO](subHomeNumber)
        case Left(_)      => throw new RuntimeException("This branch should not be reached.")
      }
    }.sequence).toList.sequence

    for {
      dynamicPart <- dynamicPartComputation
    } yield MenuSlotLayout(subHomePointPart.flatten ++ dynamicPart.toMap: _*)
  }

  private object ConstantButtons {
    def warpToSubHomePointButton(subHomeNumber: Int): Button =
      Button(
        new IconItemStackBuilder(Material.COMPASS)
          .title(s"$YELLOW$UNDERLINE${BOLD}サブホームポイント${subHomeNumber}にワープ")
          .lore(
            List(
              s"${GRAY}あらかじめ設定した",
              s"${GRAY}サブホームポイント${subHomeNumber}にワープします",
              s"${DARK_GRAY}うまく機能しない時は",
              s"${DARK_GRAY}再接続してみてください",
              s"$DARK_RED${UNDERLINE}クリックでワープ",
              s"${DARK_GRAY}command->[/subhome warp $subHomeNumber]"
            )
          )
          .build(),
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            CommandEffect(s"subhome warp $subHomeNumber")
          )
        }
      )

    def setSubHomeButton(subHomeNumber: Int)(implicit environment: Environment): Button =
      Button(
        new IconItemStackBuilder(Material.BED)
          .title(s"$YELLOW$UNDERLINE${BOLD}サブホームポイント${subHomeNumber}を設定")
          .lore(
            List(
              s"${GRAY}現在位置をサブホームポイント$subHomeNumber",
              s"${GRAY}として設定します",
              s"$DARK_GRAY※確認メニューが開きます",
              s"$DARK_RED${UNDERLINE}クリックで設定",
              s"${DARK_GRAY}command->[/subhome set $subHomeNumber]"
            )
          )
          .build(),
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
            environment.ioCanOpenConfirmationMenu.open(ConfirmationMenu(Some(subHomeNumber)))
          )
        }
      )
  }

  private case class ButtonComputations(player: Player) {
    def setSubHomeNameButton[F[_]: SubHomeReadAPI: ConcurrentEffect](
      subHomeNumber: Int
    ): IO[Button] = {
      import cats.implicits._

      val subHomeId = SubHomeId(subHomeNumber)

      val program = for {
        subhomeOpt <- SubHomeReadAPI[F].get(player.getUniqueId, subHomeId)
      } yield {
        val lore = subhomeOpt match {
          case None => List(s"${GRAY}サブホームポイント$subHomeId", s"${GRAY}ポイント未設定")
          case Some(SubHome(optionName, location)) =>
            val worldName = {
              ManagedWorld
                .fromName(location.worldName)
                .map(_.japaneseName)
                .getOrElse(location.worldName)
            }

            val nameStatus = optionName match {
              case Some(name) =>
                List(s"${GRAY}サブホームポイント${subHomeId}は", s"$GRAY$name", s"${GRAY}と名付けられています")
              case None => List(s"${GRAY}サブホームポイント${subHomeId}は", s"${GRAY}名前が未設定です")
            }

            val commandInfo = List(
              s"$DARK_RED${UNDERLINE}クリックで名称変更",
              s"${DARK_GRAY}command->[/subhome name $subHomeId]"
            )

            val coordinates = List(
              s"$GRAY$worldName x:${location.x} y:${location.y} z:${location.z}"
            )

            nameStatus ++ commandInfo ++ coordinates
        }

        Button(
          new IconItemStackBuilder(Material.PAPER)
            .title(s"$YELLOW$UNDERLINE${BOLD}サブホームポイント${subHomeNumber}の情報")
            .lore(lore)
            .build(),
          LeftClickButtonEffect {
            SequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              CommandEffect(s"subhome name $subHomeNumber"),
              closeInventoryEffect
            )
          }
        )
      }

      import cats.effect.implicits._

      program.toIO
    }
  }

  case class ConfirmationMenu(changeSubHomeNumber: Option[Int], subHomeName: String = "")
      extends Menu {
    override type Environment = ConfirmationMenu.Environment

    /**
     * メニューのサイズとタイトルに関する情報
     */
    override val frame: MenuFrame = MenuFrame(3.chestRows, s"$RED${BOLD}ホームポイントを変更しますか")

    /**
     * @return
     *   `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
     */
    override def computeMenuLayout(
      player: Player
    )(implicit environment: Environment): IO[MenuSlotLayout] = {
      val baseSlotMap =
        Map(ChestSlotRef(1, 2) -> changeButton, ChestSlotRef(1, 6) -> cancelButton)
      val slotMap = changeSubHomeNumber match {
        case None => baseSlotMap
        case _    => baseSlotMap ++ Map(ChestSlotRef(0, 4) -> informationButton)
      }
      IO.pure(MenuSlotLayout(slotMap))
    }

    val changeButton: Button =
      Button(
        new IconItemStackBuilder(Material.WOOL, durability = 5).title(s"${GREEN}変更する").build(),
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            changeSubHomeNumber match {
              case None             => CommandEffect("sethome")
              case Some(homeNumber) => CommandEffect(s"subhome set $homeNumber")
            },
            closeInventoryEffect
          )
        }
      )

    def cancelButton(implicit environment: Environment): Button =
      Button(
        new IconItemStackBuilder(Material.WOOL, durability = 14).title(s"${RED}変更しない").build(),
        LeftClickButtonEffect {
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          environment.ioCanOpenHomeMenu.open(HomeMenu)
        }
      )

    val informationButton: Button =
      Button(
        new IconItemStackBuilder(Material.PAPER)
          .title(s"${GREEN}設定するサブホームポイントの情報")
          .lore(
            List(s"${GRAY}No.${changeSubHomeNumber.getOrElse(0)}", s"${GRAY}名称：$subHomeName")
          )
          .build()
      )
  }

  object ConfirmationMenu {

    class Environment(implicit val ioCanOpenHomeMenu: IO CanOpen HomeMenu.type)

  }

}
