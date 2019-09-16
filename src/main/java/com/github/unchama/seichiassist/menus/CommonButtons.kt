package com.github.unchama.seichiassist.menus

import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.seichiassist.menus.stickmenu.firstPage
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.sequentialEffect
import org.bukkit.ChatColor
import org.bukkit.Sound

/**
 * メニューUIに頻繁に現れるような[Button]を生成する、または定数として持っているオブジェクト.
 */
object CommonButtons {
  val openStickMenu = run {
    val buttonEffect = sequentialEffect(
        FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
        StickMenu.firstPage.open
    )

    Button(
        SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
            .title("${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}ホームへ")
            .lore(listOf("${ChatColor.RESET}${ChatColor.DARK_RED}${ChatColor.UNDERLINE}クリックで移動"))
            .build(),
        FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE, buttonEffect)
    )
  }
}