package com.github.unchama.seichiassist.menus.minestack

import arrow.core.Left
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.*
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.UUIDs
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import com.github.unchama.seichiassist.minestack.category
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.computedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.sequentialEffect
import com.github.unchama.util.collection.mapValues
import org.bukkit.ChatColor.*
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

object CategorizedMineStackMenu {
  private const val mineStackObjectPerPage = 9 * 5

  private suspend fun Player.computeCategorizedMineStackMenuLayout(category: MineStackObjectCategory, page: Int): IndexedSlotLayout {
    val categoryItemList = MineStackObjectList.minestacklist!!.filter { it.category() == category }
    val totalNumberOfPages = Math.ceil(categoryItemList.size / 45.0).toInt()

    /**
     * オブジェクトリストが更新されるなどの理由でpageが最大値を超えてしまった場合、最後のページを計算する
     */
    if (page >= totalNumberOfPages) return computeCategorizedMineStackMenuLayout(category, totalNumberOfPages - 1)

    val categorizedItemSection =
        IndexedSlotLayout(
            categoryItemList.drop(mineStackObjectPerPage * page).take(mineStackObjectPerPage)
                .withIndex()
                .mapValues { with (MineStackButtons) { getMineStackItemButtonOf(it) } }
        )

    val uiOperationSection = run {
      fun buttonToTransferTo(page: Int, skullOwnerUUID: UUID) = Button(
          SkullItemStackBuilder(skullOwnerUUID)
              .title("$YELLOW$UNDERLINE${BOLD}MineStack${page + 1}ページ目へ")
              .lore(listOf("$RESET$DARK_RED${UNDERLINE}クリックで移動"))
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
                open(category, page)
            )
          }
      )

      val stickMenuButtonSection = singleSlotLayout { (9 * 5) to CommonButtons.openStickMenu }

      val previousPageButtonSection = if (page > 0) {
        singleSlotLayout { 9 * 5 + 7 to buttonToTransferTo(page - 1, UUIDs.MHFArrowUp) }
      } else emptyLayout

      val nextPageButtonSection = if (page + 1 < totalNumberOfPages) {
        singleSlotLayout { 9 * 5 + 8 to buttonToTransferTo(page + 1, UUIDs.MHFArrowDown) }
      } else emptyLayout

      combinedLayout(
          stickMenuButtonSection,
          previousPageButtonSection,
          nextPageButtonSection
      )
    }

    val autoMineStackToggleButtonSection = singleSlotLayout {
      (9 * 5 + 4) to with(MineStackButtons) { computeAutoMineStackToggleButton() }
    }

    return combinedLayout(
        categorizedItemSection,
        uiOperationSection,
        autoMineStackToggleButtonSection
    )
  }

  fun open(category: MineStackObjectCategory, page: Int = 0): TargetedEffect<Player> = computedEffect { player ->
    val view = MenuInventoryView(
        Left(6 * 9),
        "$DARK_BLUE${BOLD}MineStack - ${category.uiLabel}",
        player.computeCategorizedMineStackMenuLayout(category, page)
    )

    view.createNewSession().open
  }
}
