package com.github.unchama.seichiassist.menus

import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
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
            .title(s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}ホームへ")
            .lore(listOf(s"${ChatColor.RESET}${ChatColor.DARK_RED}${ChatColor.UNDERLINE}クリックで移動"))
            .build(),
        FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE, buttonEffect)
    )
  }
}