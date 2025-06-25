package com.github.unchama.seichiassist.menus.nicknames

import com.github.unchama.menuinventory.Menu
import com.github.unchama.menuinventory.MenuFrame
import cats.effect.IO
import com.github.unchama.menuinventory.MenuSlotLayout
import org.bukkit.entity.Player
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.seichiassist.achievement.NicknamesToBeUnlocked
import com.github.unchama.seichiassist.achievement.HeadTail
import com.github.unchama.seichiassist.achievement.MiddleOnly
import com.github.unchama.seichiassist.achievement.HeadOnly
import com.github.unchama.seichiassist.achievement.FullSet
import com.github.unchama.seichiassist.achievement.HeadMiddle
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.Material
import org.bukkit.ChatColor._
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.Sound
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.targetedeffect.DeferredEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.menuinventory.ChestSlotRef
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.generic.MapExtra
import com.github.unchama.seichiassist.menus.paging.PageCounter

object NicknameShopMenu {

  class Environment(
    implicit val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player],
    implicit val ioCanOpenNicknameShopMenu: IO CanOpen NicknameShopMenu,
    implicit val ioCanOpenNicknameMenu: IO CanOpen NickNameMenu.type
  )

}

case class NicknameShopMenu(val pageIndex: Int = 0) extends Menu {

  import com.github.unchama.menuinventory.syntax._

  private val MenuRowCount = 4

  override type Environment = NicknameShopMenu.Environment

  override val frame: MenuFrame = MenuFrame(MenuRowCount.chestRows, "実績ポイントショップ")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = IO {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val nicknameShopMenuButtons = NicknameShopMenuButtons(player)

    import nicknameShopMenuButtons._
    import eu.timepit.refined.auto._
    import eu.timepit.refined.numeric.GreaterEqual
    import eu.timepit.refined._

    val achievementRanges = Seq(9801 until 9834, 9911 until 9939)
    val achievementIdWithNicknamesToBeUnlocked =
      achievementRanges.flatten.filterNot(playerdata.TitleFlags.contains).flatMap {
        achievementId =>
          Nicknames.getNicknameFor(achievementId).map(nicknames => (achievementId, nicknames))
      }

    val nicknameButtonPerPage = (MenuRowCount - 1).chestRows.slotCount

    // NOTE: `length` 呼び出して 0 を下回ることはない
    val totalItems =
      refineV[GreaterEqual[0]].unsafeFrom(achievementIdWithNicknamesToBeUnlocked.length)

    val totalNumberOfPages =
      PageCounter.totalPage(totalItems, 26)

    val purchaseButtonMapping = achievementIdWithNicknamesToBeUnlocked
      .slice(
        nicknameButtonPerPage * pageIndex,
        nicknameButtonPerPage * pageIndex + nicknameButtonPerPage
      )
      .zipWithIndex
      .map {
        case ((archievementId, nicknamesToBeUnlocked), index) =>
          val button = purchaseButton(archievementId, nicknamesToBeUnlocked)

          (index + 1) -> button
      }

    val nextPageButtonMapping = MapExtra.when(pageIndex + 1 < totalNumberOfPages) {
      Map(ChestSlotRef(3, 8) -> nextPageButton)
    }

    val uiOperationButtonMapping =
      Map(
        ChestSlotRef(0, 0) -> informationButton,
        ChestSlotRef(3, 0) -> {
          if (pageIndex != 0) previousPageButton else toNicknameMenuButton
        },
        ChestSlotRef(3, 8) -> toNicknameMenuButton
      ) ++ nextPageButtonMapping

    MenuSlotLayout(purchaseButtonMapping ++ uiOperationButtonMapping: _*)
  }

  private case class NicknameShopMenuButtons(player: Player)(
    implicit val environment: Environment
  ) {

    import environment._

    private val playerdata = SeichiAssist.playermap(player.getUniqueId)

    val informationButton: Button = {
      val achievePoint = playerdata.achievePoint

      Button(
        new IconItemStackBuilder(Material.EMERALD_ORE)
          .title(s"$YELLOW$UNDERLINE${BOLD}実績ポイント 情報")
          .lore(
            s"${GREEN}クリックで情報を最新化",
            s"${RED}累計獲得量: ${achievePoint.cumulativeTotal}",
            s"${RED}累計消費量: ${achievePoint.used}",
            s"${AQUA}使用可能量: ${achievePoint.left}"
          )
          .build(),
        LeftClickButtonEffect(
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
            DeferredEffect(IO {
              playerdata.recalculateAchievePoint()
              ioCanOpenNicknameShopMenu.open(NicknameShopMenu(pageIndex))
            })
          )
        )
      )
    }

    def purchaseButton(
      archivementId: Int,
      nicknamesToBeUnlocked: NicknamesToBeUnlocked
    ): Button = {
      val displayNameWithNeedsPoint = nicknamesToBeUnlocked match {
        case HeadTail(_head, _tail)         => ("前・後", 20)
        case MiddleOnly(_middle)            => ("中", 35)
        case HeadOnly(_head)                => ("前", 35)
        case FullSet(_head, _middle, _tail) => ("前・中・後", 20)
        case HeadMiddle(_head, _middle)     => ("前・中", 20)
      }

      val partsName = nicknamesToBeUnlocked match {
        case HeadTail(head, _tail)         => head
        case MiddleOnly(middle)            => middle
        case HeadOnly(head)                => head
        case FullSet(head, _middle, _tail) => head
        case HeadMiddle(head, _middle)     => head
      }

      Button(
        new IconItemStackBuilder(Material.BEDROCK)
          .title(archivementId.toString)
          .lore(
            s"$RED${displayNameWithNeedsPoint._1}パーツ「$partsName」",
            s"${GREEN}必要ポイント: ${displayNameWithNeedsPoint._2}",
            s"${AQUA}クリックで購入できます"
          )
          .build(),
        LeftClickButtonEffect(
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
            DeferredEffect(IO {
              if (playerdata.achievePoint.left >= displayNameWithNeedsPoint._2) {
                playerdata.TitleFlags.addOne(archivementId)
                playerdata.consumeAchievePoint(displayNameWithNeedsPoint._2)
                SequentialEffect(
                  MessageEffect(s"パーツ「$partsName」を購入しました。"),
                  ioCanOpenNicknameShopMenu.open(NicknameShopMenu(pageIndex))
                )
              } else {
                MessageEffect("実績ポイントが不足しています。")
              }
            })
          )
        )
      )
    }

    val nextPageButton: Button = CommonButtons.transferButton(
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowRight),
      s"$YELLOW$BOLD${UNDERLINE}次ページへ",
      new NicknameShopMenu(pageIndex + 1)
    )

    val previousPageButton: Button = CommonButtons.transferButton(
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
      s"$YELLOW$BOLD${UNDERLINE}前ページへ",
      new NicknameShopMenu(pageIndex - 1)
    )

    val toNicknameMenuButton: Button = CommonButtons.transferButton(
      new IconItemStackBuilder(Material.BARRIER),
      s"$YELLOW$BOLD${UNDERLINE}二つ名組合せメインメニューへ",
      NickNameMenu
    )
  }

}
