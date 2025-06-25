package com.github.unchama.seichiassist.menus.nicknames

import cats.effect.IO
import com.github.unchama.menuinventory.Menu
import com.github.unchama.menuinventory.MenuFrame
import com.github.unchama.menuinventory.MenuSlotLayout
import org.bukkit.entity.Player
import org.bukkit.ChatColor._
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.Material
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.Sound
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import cats.data.Kleisli
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.targetedeffect.DeferredEffect
import com.github.unchama.menuinventory.ChestSlotRef
import com.github.unchama.seichiassist.menus.nicknames.NicknameCombinationMenu.NicknamePart
import com.github.unchama.seichiassist.menus.nicknames.NicknameCombinationMenu.NicknamePart.Middle
import com.github.unchama.seichiassist.menus.nicknames.NicknameCombinationMenu.NicknamePart.Head
import com.github.unchama.seichiassist.menus.nicknames.NicknameCombinationMenu.NicknamePart.Tail
import com.github.unchama.generic.MapExtra
import com.github.unchama.seichiassist.menus.paging.PageCounter

object NicknameCombinationMenu {

  class Environment(
    implicit val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player],
    implicit val ioCanOpenNicknameCombinationMenu: IO CanOpen NicknameCombinationMenu,
    implicit val ioCanOpenNicknameMenu: IO CanOpen NickNameMenu.type
  )

  sealed trait NicknamePart {
    def displayName: String
  }

  object NicknamePart {

    case object Head extends NicknamePart {
      override def displayName: String = "前"
    }

    case object Middle extends NicknamePart {
      override def displayName: String = "中"
    }

    case object Tail extends NicknamePart {
      override def displayName: String = "後"
    }

  }

}

case class NicknameCombinationMenu(pageIndex: Int = 0, nicknamePart: NicknamePart)
    extends Menu {

  import com.github.unchama.menuinventory.syntax._

  private val MenuRowCount = 4

  override type Environment = NicknameCombinationMenu.Environment

  override val frame: MenuFrame =
    MenuFrame(MenuRowCount.chestRows, s"$DARK_PURPLE${BOLD}二つ名組合せ「${nicknamePart.displayName}」")

  private def liftedArchivementId: Seq[Int] = {
    nicknamePart match {
      case NicknamePart.Head => (1000 until 9900).filter(Nicknames.getNicknameFor(_).isDefined)
      case NicknamePart.Middle =>
        (9900 until 9999).filter(Nicknames.getNicknameFor(_).isDefined)
      case NicknamePart.Tail => (1000 until 9900).filter(Nicknames.getNicknameFor(_).isDefined)
    }
  }

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = IO {
    val nicknameButtonPerPage = (MenuRowCount - 1).chestRows.slotCount

    val nickNameCombinationMenuButtons = NickNameCombinationMenuButtons(player, nicknamePart)
    val playerdata = SeichiAssist.playermap(player.getUniqueId)

    import nickNameCombinationMenuButtons._

    val archivementButtonMapping = liftedArchivementId
      .slice(
        nicknameButtonPerPage * pageIndex,
        nicknameButtonPerPage * pageIndex + nicknameButtonPerPage
      )
      .zipWithIndex
      .flatMap {
        case (archivementId, index) =>
          if (playerdata.TitleFlags.contains(archivementId)) {
            val button = partButton(archivementId)

            button.map(index -> _)
          } else {
            Some(index -> lockedNicknameButton(archivementId))
          }
      }

    import eu.timepit.refined.auto._
    import eu.timepit.refined.numeric.GreaterEqual
    import eu.timepit.refined._

    // NOTE: `length` 呼び出して 0 を下回ることはない
    val totalItems =
      refineV[GreaterEqual[0]].unsafeFrom(liftedArchivementId.length)

    val totalNumberOfPages = PageCounter.totalPage(totalItems, 27)

    val nextPageButtonMapping =
      MapExtra.when(pageIndex + 1 < totalNumberOfPages)(
        Map(ChestSlotRef(3, 8) -> nextPageButton)
      )

    val uiOperationButtonMapping = Map(
      ChestSlotRef(3, 0) -> {
        if (pageIndex != 0) previousPageButton else toNicknameMenuButton
      },
      ChestSlotRef(3, 4) -> resetSelectionButton
    ) ++ nextPageButtonMapping

    MenuSlotLayout(archivementButtonMapping ++ uiOperationButtonMapping: _*)
  }

  private case class NickNameCombinationMenuButtons(player: Player, nicknamePart: NicknamePart)(
    implicit val environment: Environment
  ) {

    import environment._

    private val playerdata = SeichiAssist.playermap(player.getUniqueId)

    def partButton(archivementId: Int): Option[Button] = {
      val nickname = (nicknamePart match {
        case NicknamePart.Head   => Nicknames.getHeadPartFor _
        case NicknamePart.Middle => Nicknames.getMiddlePartFor _
        case NicknamePart.Tail   => Nicknames.getTailPartFor _
      })(archivementId)

      val icon = nicknamePart match {
        case NicknamePart.Head   => Material.WATER_BUCKET
        case NicknamePart.Middle => Material.MILK_BUCKET
        case NicknamePart.Tail   => Material.LAVA_BUCKET
      }

      nickname.map { nickname =>
        val itemStack = new IconItemStackBuilder(icon)
          .title(archivementId.toString())
          .lore(s"$RED${nicknamePart.displayName}パーツ「${nickname}」")
          .build()

        Button(
          itemStack,
          LeftClickButtonEffect(
            SequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
              DeferredEffect(IO {
                nicknamePart match {
                  case Head   => playerdata.updateNickname(id1 = archivementId)
                  case Middle => playerdata.updateNickname(id2 = archivementId)
                  case Tail   => playerdata.updateNickname(id3 = archivementId)
                }
                MessageEffect(s"${nicknamePart.displayName}パーツ「${nickname}」をセットしました。")
              })
            )
          )
        )
      }
    }

    def lockedNicknameButton(archivementId: Int): Button = {
      val itemStack = new IconItemStackBuilder(Material.BEDROCK)
        .title(archivementId.toString())
        .lore(s"$RED${archivementId.toString()}は解禁されていません。")
        .build()

      Button(
        itemStack,
        LeftClickButtonEffect(
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
            MessageEffect(s"$RED${archivementId.toString()}は解禁されていません。")
          )
        )
      )
    }

    val resetSelectionButton: Button = {
      val itemStack = new IconItemStackBuilder(Material.GRASS)
        .title(s"$YELLOW$UNDERLINE$BOLD${nicknamePart.displayName}パーツを未選択状態にする")
        .lore(s"$DARK_RED${UNDERLINE}クリックで実行")
        .build()

      Button(
        itemStack,
        LeftClickButtonEffect(
          SequentialEffect(
            Kleisli.liftF(IO {
              nicknamePart match {
                case Head   => playerdata.updateNickname(id1 = 0)
                case Middle => playerdata.updateNickname(id2 = 0)
                case Tail   => playerdata.updateNickname(id3 = 0)
              }
            }),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
            MessageEffect(s"${nicknamePart.displayName}パーツの選択を解除しました。")
          )
        )
      )
    }

    val nextPageButton: Button = CommonButtons.transferButton(
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowRight),
      s"$YELLOW$BOLD${UNDERLINE}次ページへ",
      new NicknameCombinationMenu(pageIndex + 1, nicknamePart)
    )

    val previousPageButton: Button = CommonButtons.transferButton(
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
      s"$YELLOW$BOLD${UNDERLINE}前ページへ",
      new NicknameCombinationMenu(pageIndex - 1, nicknamePart)
    )

    val toNicknameMenuButton: Button = CommonButtons.transferButton(
      new IconItemStackBuilder(Material.BARRIER),
      s"$YELLOW$BOLD${UNDERLINE}二つ名組合せメインメニューへ",
      NickNameMenu
    )
  }

}
