package com.github.unchama.seichiassist.menus.minestack

import arrow.core.Left
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import com.github.unchama.seichiassist.minestack.category
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.computedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.sequentialEffect
import org.bukkit.ChatColor.*
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

object CategorizedMineStackMenu {
  private suspend fun Player.computeCategorizedMineStackMenuLayout(category: MineStackObjectCategory, page: Int): IndexedSlotLayout {
    val categoryItemList = MineStackObjectList.minestacklist!!.filter { it.category() == category }

    val autoStackToggleButtonSection = if (page == 0) {
      mapOf(0 to with (MineStackButtons) { computeAutoMineStackToggleButton() })
    } else {
      mapOf()
    }

    val categorizedItemSection = run {
      // 各カテゴリの最初のページには0インデックスにトグルボタンが入る
      val fillSlotFrom = if (page == 0) 1 else 0
      val fillSlotUntil = 45

      // `categoryItemList` で表示が初められるインデックス
      // 最初のページには44個しかアイテムが入らず, それ以降は45個入るため0, 44, 44 + 45, 44 + 45 * 2...の数列になる
      val itemListOffset = fillSlotFrom + 44 + (page - 1) * 45

      (fillSlotFrom until fillSlotUntil)
          .zip(categoryItemList.drop(itemListOffset))
          .map { (slotIndex, mineStackObject) ->
            val button = with (MineStackButtons) { getMineStackItemButtonOf(mineStackObject) }

            slotIndex to button
          }
          .toMap()
    }

    val pageTransitionButtonSection = run {
      val pageTransferSoundEffect = FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f)

      fun buttonToTransferTo(page: Int, skullOwnerUUID: UUID) = Button(
          SkullItemStackBuilder(skullOwnerUUID)
              .title("$YELLOW$UNDERLINE${BOLD}MineStack${page + 1}ページ目へ")
              .lore(listOf("$RESET$DARK_RED${UNDERLINE}クリックで移動"))
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                pageTransferSoundEffect,
                open(category, page)
            )
          }
      )

      val previousPageButtonSection = if (page > 0) {
        // MHF_ArrowUp
        val skullOwnerUUID = UUID.fromString("fef039ef-e6cd-4987-9c84-26a3e6134277")
        mapOf(9 * 5 + 7 to buttonToTransferTo(page - 1, skullOwnerUUID))
      } else {
        mapOf()
      }

      val nextPageButtonSection = run {
        // 1ページ目の0個目のスロットもトグルボタンで占領されている
        val totalSlotsOccupied = categoryItemList.size + 1
        val totalNumberOfPages = Math.ceil(totalSlotsOccupied / 45.0).toInt()

        if (page + 1 < totalNumberOfPages) {
          // MHF_ArrowDown
          val skullOwnerUUID = UUID.fromString("68f59b9b-5b0b-4b05-a9f2-e1d1405aa348")
          mapOf(9 * 5 + 8 to buttonToTransferTo(page + 1, skullOwnerUUID))
        } else {
          mapOf()
        }
      }

      mapOf((9 * 5) to CommonButtons.openStickMenu) +
          previousPageButtonSection +
          nextPageButtonSection
    }

    return IndexedSlotLayout(autoStackToggleButtonSection + categorizedItemSection + pageTransitionButtonSection)
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
