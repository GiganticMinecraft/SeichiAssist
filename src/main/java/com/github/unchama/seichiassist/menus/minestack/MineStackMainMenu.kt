package com.github.unchama.seichiassist.menus.minestack

import arrow.core.Left
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory.*
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.computedEffect
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.entity.Player

object MineStackMainMenu {
  private object ButtonComputations {
    val categoryButtonLayout = run {
      fun iconMaterialFor(category: MineStackObjectCategory): Material = when (category) {
        ORES -> Material.DIAMOND_ORE
        MOB_DROP -> Material.ENDER_PEARL
        AGRICULTURAL -> Material.SEEDS
        BUILDING -> Material.SMOOTH_BRICK
        REDSTONE_AND_TRANSPORTATION -> Material.REDSTONE
        GACHA_PRIZES -> Material.GOLDEN_APPLE
      }

      val layoutMap = MineStackObjectCategory.values().mapIndexed { index, category ->
        val slotIndex = index + 1 // 0には自動スタック機能トグルが入るので、1から入れ始める
        val iconItemStack = IconItemStackBuilder(iconMaterialFor(category))
            .lore(listOf("$BLUE$UNDERLINE$BOLD${category.uiLabel}"))
            .build()

        slotIndex to Button(iconItemStack) // TODO クリックで各カテゴリのUIを開く
      }.toMap()

      IndexedSlotLayout(layoutMap)
    }

    /**
     * メインメニュー内の「履歴」機能部分のレイアウトを計算する
     */
    suspend fun Player.computeHistoricalMineStackLayout(): IndexedSlotLayout {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val buttonMapping = playerData.hisotryData.usageHistory.mapIndexed { index, mineStackObject ->
        val slotIndex = 18 + index // 3行目から入れだす
        val button = with(MineStackButtons) { getMineStackItemButtonOf(mineStackObject) }

        slotIndex to button
      }.toMap()

      return IndexedSlotLayout(buttonMapping)
    }
  }

  private suspend fun Player.computeMineStackMainMenuLayout(): IndexedSlotLayout {
    return with(ButtonComputations) {
      IndexedSlotLayout(
          0 to with (MineStackButtons) { computeAutoMineStackToggleButton() },
          45 to CommonButtons.openStickMenu
      )
          .merge(categoryButtonLayout)
          .merge(computeHistoricalMineStackLayout())
    }
  }

  val openMainMenu: TargetedEffect<Player> = computedEffect { player ->
    val view = MenuInventoryView(
        Left(4 * 9),
        "$DARK_PURPLE${BOLD}MineStackメインメニュー",
        player.computeMineStackMainMenuLayout()
    )

    view.createNewSession().open
  }
}