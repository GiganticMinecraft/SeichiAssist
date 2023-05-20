package com.github.unchama.seichiassist.menus.achievement.group

import cats.effect.IO
import com.github.unchama.generic.CachedFunction
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory._
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementGroup
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementGroup._
import com.github.unchama.seichiassist.menus.achievement.AchievementCategoryMenu
import com.github.unchama.seichiassist.menus.achievement.group.AchievementGroupMenu.sequentialEntriesIn
import com.github.unchama.seichiassist.menus.{ColorScheme, CommonButtons}
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

object AchievementGroupMenu {

  class Environment(
    implicit val ioCanOpenGroupMenu: IO CanOpen AchievementGroupMenu,
    val ioCanOpenCategoryMenu: IO CanOpen AchievementCategoryMenu
  )

  val sequentialEntriesIn: AchievementGroup => List[GroupMenuEntry] = CachedFunction {

    case BrokenBlockRanking =>
      AchievementEntry.within(1001 to 1012)

    case PlacedBlockAmount =>
      AchievementEntry.within(2001 to 2014)

    case BrokenBlockAmount =>
      AchievementEntry.within(3001 to 3019)

    case PlayTime =>
      AchievementEntry.within(4001 to 4023) :+
        Achievement8003UnlockEntry

    case TotalLogins =>
      AchievementEntry.within(5101 to 5125)

    case ConsecutiveLogins =>
      AchievementEntry.within(5001 to 5008)

    case VoteCounts =>
      AchievementEntry.within(6001 to 6008)

    case OfficialEvent =>
      AchievementEntry.within(7001 to 7027) ++
        AchievementEntry.within(7901 to 7906)

    case Secrets =>
      AchievementEntry.within(8001 to 8003)

    case Anniversaries =>
      AchievementEntry.within(9001 to 9047)

    case MebiusBreeder =>
      AchievementEntry.within(0 until 0)

    case StarLevel =>
      AchievementEntry.within(0 until 0)
  }
}

case class AchievementGroupMenu(group: AchievementGroup, pageNumber: Int = 1) extends Menu {

  import com.github.unchama.menuinventory.syntax._

  override type Environment = AchievementGroupMenu.Environment
  override val frame: MenuFrame =
    MenuFrame(4.chestRows, ColorScheme.purpleBold(s"実績「${group.name}」"))

  private val groupEntries = sequentialEntriesIn(group)
  private val entriesToDisplay = {
    import com.github.unchama.menuinventory.syntax._

    val displayPerPage = 3.chestRows.slotCount
    val displayFromIndex = displayPerPage * (pageNumber - 1)
    val displayUptoIndex = displayFromIndex + displayPerPage

    groupEntries.slice(displayFromIndex, displayUptoIndex)
  }

  private val groupAchievementsCount = groupEntries.size
  private val maxPageNumber = Math.ceil(groupAchievementsCount / 27.0).toInt

  override def open(
    implicit environment: AchievementGroupMenu.Environment,
    ctx: LayoutPreparationContext,
    onMainThread: OnMinecraftServerThread[IO]
  ): TargetedEffect[Player] = {
    // redirect
    if (entriesToDisplay.isEmpty) {
      if (groupAchievementsCount == 0) {
        environment.ioCanOpenCategoryMenu.open(AchievementCategoryMenu(group.parent))
      } else {
        environment.ioCanOpenGroupMenu.open(AchievementGroupMenu(group, maxPageNumber))
      }
    } else super.open
  }

  override def computeMenuLayout(
    player: Player
  )(implicit environment: AchievementGroupMenu.Environment): IO[MenuSlotLayout] = {
    import cats.implicits._
    import environment._
    import eu.timepit.refined.auto._

    def buttonToTransferTo(
      newPageNumber: Int,
      skullOwnerReference: SkullOwnerReference
    ): Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(skullOwnerReference),
        s"${newPageNumber}ページ目へ",
        AchievementGroupMenu(group, newPageNumber)
      )

    /**
     * 上位メニューはこのメニューを参照していて、 このセクションのボタンは上位メニューを参照するので、 貪欲に計算すると計算が再帰する。
     *
     * `computeMenuLayout`にて初めて参照されるため、 lazyにすることで実際にプレーヤーがこのメニューを開くまで評価されず、 再帰自体は回避される。
     */
    lazy val toCategoryMenuButtonSection = Map(
      ChestSlotRef(3, 0) -> CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
        s"「${group.parent.name}」カテゴリメニューへ",
        AchievementCategoryMenu(group.parent)
      )
    )

    // 同じ階層のメニューを参照しているので貪欲に計算すると計算が再帰する
    lazy val previousPageButtonSection =
      if (pageNumber > 1) {
        Map(ChestSlotRef(3, 7) -> buttonToTransferTo(pageNumber - 1, SkullOwners.MHF_ArrowLeft))
      } else {
        Map()
      }

    lazy val nextPageButtonSection =
      if (pageNumber < maxPageNumber) {
        Map(
          ChestSlotRef(3, 8) -> buttonToTransferTo(pageNumber + 1, SkullOwners.MHF_ArrowRight)
        )
      } else {
        Map()
      }

    val dynamicPartComputation =
      entriesToDisplay
        .traverse(AchievementGroupMenuButtons.entryComputationFor(player))
        .map(_.zipWithIndex.map(_.swap))

    for {
      dynamicPart <- dynamicPartComputation
      combinedLayout =
        toCategoryMenuButtonSection ++
          previousPageButtonSection ++
          nextPageButtonSection ++
          dynamicPart
    } yield MenuSlotLayout(combinedLayout)
  }
}
