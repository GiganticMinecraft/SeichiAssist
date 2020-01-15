package com.github.unchama.seichiassist.menus.achievement.group

import cats.effect.IO
import com.github.unchama.generic.CachedFunction
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementGroup
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementGroup._
import com.github.unchama.seichiassist.menus.achievement.AchievementCategoryMenu
import com.github.unchama.seichiassist.menus.{ColorScheme, CommonButtons}
import org.bukkit.entity.Player

object AchievementGroupMenu {
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}
  import eu.timepit.refined.auto._

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
      AchievementEntry.within(9001 to 9036)

    case MebiusBreeder =>
      AchievementEntry.within(0 until 0)

    case StarLevel =>
      AchievementEntry.within(0 until 0)
  }

  private val apply_ : ((AchievementGroup, Int)) => Menu = CachedFunction { case (group, pageNumber) =>
    val groupEntries = sequentialEntriesIn(group)
    val entriesToDisplay = {
      import com.github.unchama.menuinventory.syntax._

      val displayPerPage = 3.chestRows.slotCount
      val displayFromIndex = displayPerPage * (pageNumber - 1)
      val displayUptoIndex = displayFromIndex + displayPerPage

      groupEntries.slice(displayFromIndex, displayUptoIndex)
    }

    val groupAchievementsCount = groupEntries.size
    val maxPageNumber = Math.ceil(groupAchievementsCount / 27.0).toInt

    if (entriesToDisplay.isEmpty) {
      if (groupAchievementsCount == 0)
        AchievementCategoryMenu(group.parent)
      else
        apply_(group, maxPageNumber)
    } else {
      val menuFrame = {
        import com.github.unchama.menuinventory.syntax._
        MenuFrame(4.chestRows, ColorScheme.purpleBold(s"実績「${group.name}」"))
      }

      def buttonToTransferTo(newPageNumber: Int, skullOwnerReference: SkullOwnerReference): Button =
        CommonButtons.transferButton(
          new SkullItemStackBuilder(skullOwnerReference),
          s"${newPageNumber}ページ目へ",
          AchievementGroupMenu(group, newPageNumber)
        )

      /**
       *  上位メニューはこのメニューを参照していて、
       *  このセクションのボタンは上位メニューを参照するので、
       *  貪欲に計算すると計算が再帰する。
       *
       *  `computeMenuLayout`にて初めて参照されるため、
       *  lazyにすることで実際にプレーヤーがこのメニューを開くまで評価されず、
       *  再帰自体は回避される。
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
          Map(ChestSlotRef(3, 8) -> buttonToTransferTo(pageNumber + 1, SkullOwners.MHF_ArrowRight))
        } else {
          Map()
        }

      new Menu {
        override val frame: MenuFrame = menuFrame

        override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
          import cats.implicits._

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
    }
  }

  def apply(group: AchievementGroup, pageNumber: Int = 1): Menu = apply_(group, pageNumber)
}
