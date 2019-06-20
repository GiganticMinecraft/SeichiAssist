package com.github.unchama.seichiassist.menus

import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.sequentialEffect
import org.bukkit.ChatColor
import org.bukkit.Sound
import java.util.*

/**
 * メニューUIに頻繁に現れるような[Button]を生成する、または定数として持っているオブジェクト.
 */
object CommonButtons {
  val openStickMenu = run {
    // MHF_ArrowLeft
    val skullOwnerUUID = UUID.fromString("a68f0b64-8d14-4000-a95f-4b9ba14f8df9")

    val buttonEffect = sequentialEffect(
        FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
        StickMenu.open
    )

    Button(
        SkullItemStackBuilder(skullOwnerUUID)
            .title("${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}ホームへ")
            .lore(listOf("${ChatColor.RESET}${ChatColor.DARK_RED}${ChatColor.UNDERLINE}クリックで移動"))
            .build(),
        FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE, buttonEffect)
    )
  }
}