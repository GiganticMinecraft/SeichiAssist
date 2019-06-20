package com.github.unchama.seichiassist.menus

import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.computedEffect
import org.bukkit.entity.Player

object MineStackUI {
  private suspend fun Player.computeMineStackLayout(): IndexedSlotLayout {

    return IndexedSlotLayout(
        45 to CommonButtons.openStickMenu
    )
  }

  val open: TargetedEffect<Player> = computedEffect { player ->
    EmptyEffect
  }
}