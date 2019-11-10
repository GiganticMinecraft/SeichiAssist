package com.github.unchama.seichiassist.menus.achievement

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementGroup
import com.github.unchama.seichiassist.menus.CommonButtons
import org.bukkit.entity.Player

object AchievementGroupMenu {
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

  def apply(group: AchievementGroup, pageNumber: Int = 1): Menu = {
    val displayIndexRange = (3 * 9 * (pageNumber - 1)) until (3 * 9 * pageNumber)
    val displayAchievements = group.achievements.zipWithIndex
      .filter { case (_, index) => displayIndexRange.contains(index) }
      .map(_._1)

    val groupAchievementsCount = group.achievements.size
    val maxPageNumber = Math.ceil(groupAchievementsCount / 27.0).toInt

    if (displayAchievements.isEmpty) {
      if (groupAchievementsCount == 0) {
        AchievementCategoryMenu(group.parent)
      } else {
        apply(group, maxPageNumber)
      }
    } else {
      def buttonToTransferTo(pageIndex: Int, skullOwnerReference: SkullOwnerReference): Button =
        CommonButtons.transferButton(
          new SkullItemStackBuilder(skullOwnerReference),
          s"MineStack${pageIndex + 1}ページ目へ",
          AchievementGroupMenu(group, pageNumber)
        )

      val toCategoryMenuButtonSection = Map(
        9 * 3 -> CommonButtons.transferButton(
          new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
          s"「${group.parent.name}」カテゴリメニューへ",
          AchievementCategoryMenu(group.parent)
        )
      )

      val previousPageButtonSection =
        if (pageNumber > 1) {
          Map(9 * 3 + 7 -> buttonToTransferTo(pageNumber - 1, SkullOwners.MHF_ArrowLeft))
        } else {
          Map()
        }

      val nextPageButtonSection =
        if (pageNumber < maxPageNumber) {
          Map(9 * 3 + 8 -> buttonToTransferTo(pageNumber + 1, SkullOwners.MHF_ArrowRight))
        } else {
          Map()
        }

      new Menu {
        import com.github.unchama.menuinventory.InventoryRowSize._

        /**
         * メニューのサイズとタイトルに関する情報
         */
        override val frame: MenuFrame = MenuFrame(4.rows(), s"実績「${group.name}」")

        /**
         * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
         */
        override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
          import cats.implicits._

          val dynamicPartComputation =
            displayAchievements.toList
              .traverse(AchievementGroupMenuButtons(player).computeButtonFor)
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