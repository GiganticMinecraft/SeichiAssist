package com.github.unchama.seichiassist.menus.home

import cats.effect.IO
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.MapExtra
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import com.github.unchama.seichiassist.subsystems.home.HomeReadAPI
import com.github.unchama.seichiassist.subsystems.home.domain.{Home, HomeId}
import com.github.unchama.seichiassist.{ManagedWorld, SkullOwners}
import com.github.unchama.targetedeffect._
import com.github.unchama.targetedeffect.player.PlayerEffects._
import com.github.unchama.targetedeffect.player.{CommandEffect, FocusedSoundEffect}
import eu.timepit.refined.auto._
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

object HomeMenu {

  class Environment(
    implicit val ioCanOpenConfirmationMenu: IO CanOpen HomeChangeConfirmationMenu,
    implicit val ioCanOpenFirstPage: IO CanOpen FirstPage.type,
    implicit val ioCanOpenHome: IO CanOpen HomeMenu,
    val ioCanOpenHomeRemoveConfirmationMenu: IO CanOpen HomeRemoveConfirmationMenu,
    implicit val homeReadAPI: HomeReadAPI[IO],
    implicit val asyncShift: NonServerThreadContextShift[IO]
  )

}
case class HomeMenu(pageIndex: Int = 0) extends Menu {

  private val pageIndexMax = 0 max (HomeId.maxNumber - 1) / 9
  override type Environment = HomeMenu.Environment

  override val frame: MenuFrame =
    MenuFrame(5.chestRows, s"$DARK_PURPLE${BOLD}ホームメニュー ${pageIndex + 1}/${pageIndexMax + 1}")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import eu.timepit.refined._
    import eu.timepit.refined.auto._
    import eu.timepit.refined.numeric._

    val buttonComputations = HomeMenuButtonComputations(player)
    import buttonComputations._
    import cats.implicits._
    import environment._

    val homeNumberRange =
      1 + (9 * pageIndex) to HomeId.maxNumber - 9 * (pageIndexMax - pageIndex)

    // ボタンの構築に副作用がない箇所のメニュー定義
    val homePointPart = for {
      homeNumber <- homeNumberRange
    } yield {
      val columnEither = refineV[Interval.ClosedOpen[0, 9]](homeNumber - 9 * pageIndex - 1)
      columnEither.fold(
        _ => throw new RuntimeException("This branch should not be reached."),
        column =>
          Map(ChestSlotRef(0, column) -> ConstantButtons.warpToHomePointButton(homeNumber))
      )
    }

    // ボタンの構築に副作用がある箇所のメニュー定義
    val dynamicPartComputation = homeNumberRange.toList.flatTraverse { homeNumber =>
      val columnEither = refineV[Interval.ClosedOpen[0, 9]](homeNumber - 9 * pageIndex - 1)
      columnEither.fold(
        _ => throw new RuntimeException("This branch should not be reached."),
        column => {
          List(
            ChestSlotRef(1, column) -> setHomeNameButton(homeNumber),
            ChestSlotRef(2, column) -> buttonComputations.setHomeButton(homeNumber),
            ChestSlotRef(3, column) -> buttonComputations.removeHomeButton(homeNumber)
          ).traverse(_.sequence)
        }
      )
    }

    // 5スロット目のページ遷移メニュー定義
    val paginationPartMap = {
      val prevTransferButton = CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
        s"${pageIndex}ページ目へ",
        HomeMenu(pageIndex - 1)
      )
      val nextTransferButton = CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowRight),
        s"${pageIndex + 2}ページ目へ",
        HomeMenu(pageIndex + 1)
      )
      val stickButtonMap = Map(ChestSlotRef(4, 0) -> CommonButtons.openStickMenu)
      val prevButtonMap =
        MapExtra.when(pageIndex >= 1)(Map(ChestSlotRef(4, 7) -> prevTransferButton))
      val nextButtonMap =
        MapExtra.when(pageIndex + 1 <= pageIndexMax)(
          Map(ChestSlotRef(4, 8) -> nextTransferButton)
        )
      stickButtonMap ++ prevButtonMap ++ nextButtonMap
    }

    for {
      dynamicPart <- dynamicPartComputation
      paginationPart = paginationPartMap
    } yield MenuSlotLayout(homePointPart.flatten ++ dynamicPart.toMap ++ paginationPart: _*)
  }

  private object ConstantButtons {
    def warpToHomePointButton(homeNumber: Int): Button =
      Button(
        new IconItemStackBuilder(Material.COMPASS)
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームポイント${homeNumber}にワープ")
          .lore(
            List(
              s"${GRAY}あらかじめ設定した",
              s"${GRAY}ホームポイント${homeNumber}にワープします",
              s"${DARK_GRAY}うまく機能しない時は",
              s"${DARK_GRAY}再接続してみてください",
              s"$DARK_RED${UNDERLINE}クリックでワープ",
              s"${DARK_GRAY}command->[/home warp $homeNumber]"
            )
          )
          .build(),
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            CommandEffect(s"home warp $homeNumber")
          )
        }
      )
  }
}
case class HomeMenuButtonComputations(player: Player)(
  private implicit val environment: HomeMenu.Environment
) {
  import cats.effect.implicits._
  import environment._

  def setHomeNameButton(homeNumber: Int): IO[Button] = {

    val homeId = HomeId(homeNumber)

    val program = for {
      homeOpt <- homeReadAPI.get(player.getUniqueId, homeId)
    } yield {
      val lore = homeOpt.fold(List(s"${GRAY}ホームポイント$homeId", s"${GRAY}ポイント未設定"))(home => {
        val location = home.location
        val optionName = home.name
        val worldName =
          ManagedWorld
            .fromName(location.worldName)
            .map(_.japaneseName)
            .getOrElse(location.worldName)

        val nameStatus =
          optionName.fold(List(s"${GRAY}ホームポイント${homeId}は", s"${GRAY}名前が未設定です"))(name =>
            List(s"${GRAY}ホームポイント${homeId}は", s"$GRAY$name", s"${GRAY}と名付けられています")
          )

        val commandInfo =
          List(s"$DARK_RED${UNDERLINE}クリックで名称変更", s"${DARK_GRAY}command->[/home name $homeId]")

        val coordinates = List(s"$GRAY$worldName x:${Math.floor(location.x)} y:${Math
            .floor(location.y)} z:${Math.floor(location.z)}")
        nameStatus ++ commandInfo ++ coordinates
      })
      Button(
        new IconItemStackBuilder(Material.PAPER)
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームポイント${homeNumber}の情報")
          .lore(lore)
          .build(),
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            CommandEffect(s"home name $homeNumber"),
            closeInventoryEffect
          )
        }
      )
    }

    program.toIO
  }

  private def homeNameForConfirmMenu(homeOpt: Option[Home]): String =
    homeOpt.fold("ホームポイント未設定")(_.name.getOrElse("名称未設定"))

  def setHomeButton(homeNumber: Int): IO[Button] = {
    val homeId = HomeId(homeNumber)

    val program = for {
      homeOpt <- homeReadAPI.get(player.getUniqueId, homeId)
    } yield {
      Button(
        new IconItemStackBuilder(Material.BED)
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームポイント${homeNumber}を設定")
          .lore(
            List(
              s"${GRAY}現在位置をホームポイント$homeNumber",
              s"${GRAY}として設定します",
              s"$DARK_GRAY※確認メニューが開きます",
              s"$DARK_RED${UNDERLINE}クリックで設定",
              s"${DARK_GRAY}command->[/home set $homeNumber]"
            )
          )
          .build(),
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
            environment
              .ioCanOpenConfirmationMenu
              .open(HomeChangeConfirmationMenu(homeNumber, homeNameForConfirmMenu(homeOpt)))
          )
        }
      )
    }
    program.toIO
  }

  def removeHomeButton(homeNumber: Int): IO[Button] = {
    val homeId = HomeId(homeNumber)
    val program = for {
      homeOpt <- homeReadAPI.get(player.getUniqueId, homeId)
    } yield {
      Button(
        new IconItemStackBuilder(Material.WOOL, 14)
          .title(s"$RED$UNDERLINE${BOLD}ホームポイント${homeNumber}を削除")
          .lore(
            List(
              s"${GRAY}ホームポイント${homeNumber}を削除します。",
              s"$DARK_GRAY※確認メニューが開きます。",
              s"$DARK_RED${UNDERLINE}クリックで設定"
            )
          )
          .build(),
        LeftClickButtonEffect {
          FocusedSoundEffect(Sound.BLOCK_ENDERCHEST_CLOSE, 1f, 0.1f)
          SequentialEffect(
            environment
              .ioCanOpenHomeRemoveConfirmationMenu
              .open(HomeRemoveConfirmationMenu(homeNumber, homeNameForConfirmMenu(homeOpt)))
          )
        }
      )
    }
    program.toIO
  }
}

case class HomeChangeConfirmationMenu(changeHomeNumber: Int, homeName: String = "")
    extends Menu {
  override type Environment = ConfirmationMenuEnvironment.Environment

  override val frame: MenuFrame = MenuFrame(3.chestRows, s"$RED${BOLD}ホームポイントを変更しますか")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    val baseSlotMap =
      Map(ChestSlotRef(1, 2) -> changeButton, ChestSlotRef(1, 6) -> cancelButton)
    val slotMap = baseSlotMap ++ Map(ChestSlotRef(0, 4) -> informationButton)
    IO.pure(MenuSlotLayout(slotMap))
  }

  val changeButton: Button =
    Button(
      new IconItemStackBuilder(Material.WOOL, durability = 5).title(s"${GREEN}変更する").build(),
      LeftClickButtonEffect {
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          CommandEffect(s"home set $changeHomeNumber"),
          closeInventoryEffect
        )
      }
    )

  def cancelButton(implicit environment: Environment): Button =
    Button(
      new IconItemStackBuilder(Material.WOOL, durability = 14).title(s"${RED}変更しない").build(),
      LeftClickButtonEffect {
        FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        environment.ioCanOpenHomeMenu.open(new HomeMenu)
      }
    )

  val informationButton: Button =
    Button(
      new IconItemStackBuilder(Material.PAPER)
        .title(s"${GREEN}設定するホームポイントの情報")
        .lore(List(s"${GRAY}No.$changeHomeNumber", s"${GRAY}名称：$homeName"))
        .build()
    )
}

object ConfirmationMenuEnvironment {

  class Environment(implicit val ioCanOpenHomeMenu: IO CanOpen HomeMenu)

}

case class HomeRemoveConfirmationMenu(removeHomeNumber: Int, homeName: String = "")
    extends Menu {

  override type Environment = ConfirmationMenuEnvironment.Environment

  override val frame: MenuFrame = MenuFrame(3.chestRows, s"$RED${BOLD}ホームポイントを削除しますか")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    val baseSlotMap =
      Map(ChestSlotRef(1, 2) -> removeButton, ChestSlotRef(1, 6) -> cancelButton)
    val slotMap = baseSlotMap ++ Map(ChestSlotRef(0, 4) -> informationButton)
    IO.pure(MenuSlotLayout(slotMap))
  }

  val removeButton: Button =
    Button(
      new IconItemStackBuilder(Material.WOOL, durability = 5).title(s"${GREEN}削除する").build(),
      LeftClickButtonEffect {
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          CommandEffect(s"home remove $removeHomeNumber"),
          closeInventoryEffect
        )
      }
    )

  def cancelButton(implicit environment: Environment): Button =
    Button(
      new IconItemStackBuilder(Material.WOOL, durability = 14).title(s"${RED}変更しない").build(),
      LeftClickButtonEffect {
        FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        environment.ioCanOpenHomeMenu.open(new HomeMenu)
      }
    )

  val informationButton: Button =
    Button(
      new IconItemStackBuilder(Material.PAPER)
        .title(s"${GREEN}設定するホームポイントの情報")
        .lore(List(s"${GRAY}No.$removeHomeNumber", s"${GRAY}名称：$homeName"))
        .build()
    )

}
