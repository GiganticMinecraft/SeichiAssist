package com.github.unchama.seichiassist.menus.achievement.group

import cats.effect.IO
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
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
  import eu.timepit.refined.auto._

  def sequentialEntriesIn(group: AchievementGroup): List[GroupMenuEntry] =
    group match {
      case BrokenBlockAmount =>
        AchievementEntry.within(3001 to 3019)

      case BrokenBlockRanking =>
        AchievementEntry.within(1001 to 1012)

      case PlayTime =>
        AchievementEntry.within(4001 to 4023) :+
          Achievement8003UnlockEntry

      case TotalLogins =>
        AchievementEntry.within(5001 to 5008)

      case ConsecutiveLogins =>
        AchievementEntry.within(5101 to 5120)

      case Anniversaries =>
        AchievementEntry.within(9001 to 9036)

      case MebiusBreeder =>
        AchievementEntry.within(0 until 0)

      case StarLevel =>
        AchievementEntry.within(0 until 0)

      case OfficialEvent =>
        AchievementEntry.within(7001 to 7027) ++
          AchievementEntry.within(7901 to 7906)

      case VoteCounts =>
        AchievementEntry.within(6001 to 6008)

      case Secrets =>
        AchievementEntry.within(8001 to 8003)
    }

  def apply(group: AchievementGroup, pageNumber: Int = 1): Menu = {
    val entriesToDisplay = {
      import com.github.unchama.menuinventory.syntax._

      val displayPerPage = 3.chestRows.slotCount
      val displayFromIndex = displayPerPage * (pageNumber - 1)
      val displayUptoIndex = displayFromIndex + displayPerPage

      sequentialEntriesIn(group).slice(displayFromIndex, displayUptoIndex)
    }

    val groupAchievementsCount = sequentialEntriesIn(group).size
    val maxPageNumber = Math.ceil(groupAchievementsCount / 27.0).toInt

    if (entriesToDisplay.isEmpty) {
      if (groupAchievementsCount == 0)
        AchievementCategoryMenu(group.parent)
      else
        apply(group, maxPageNumber)
    } else {
      new Menu {
        import com.github.unchama.menuinventory.syntax._

        override val frame: MenuFrame = MenuFrame(4.chestRows, ColorScheme.navigation(s"実績「${group.name}」"))

        override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
          val toCategoryMenuButtonSection = Map(
            ChestSlotRef(3, 0) -> CommonButtons.transferButton(
              new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
              s"「${group.parent.name}」カテゴリメニューへ",
              AchievementCategoryMenu(group.parent)
            )
          )

          def buttonToTransferTo(newPageNumber: Int, skullOwnerReference: SkullOwnerReference): Button =
            CommonButtons.transferButton(
              new SkullItemStackBuilder(skullOwnerReference),
              s"${newPageNumber}ページ目へ",
              AchievementGroupMenu(group, newPageNumber)
            )

          val previousPageButtonSection =
            if (pageNumber > 1) {
              Map(ChestSlotRef(3, 7) -> buttonToTransferTo(pageNumber - 1, SkullOwners.MHF_ArrowLeft))
            } else {
              Map()
            }

          val nextPageButtonSection =
            if (pageNumber < maxPageNumber) {
              Map(ChestSlotRef(3, 8) -> buttonToTransferTo(pageNumber + 1, SkullOwners.MHF_ArrowRight))
            } else {
              Map()
            }

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
}
