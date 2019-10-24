package com.github.unchama.seichiassist.menus

import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.slot.button
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.Sound

/**
 * メニューUIに頻繁に現れるような[Button]を生成する、または定数として持っているオブジェクト.
 */
object CommonButtons {

  import com.github.unchama.targetedeffect.TargetedEffects._

  val openStickMenu: Button = {
    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

    val buttonEffect = sequentialEffect(
      FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
      StickMenu.firstPage.open
    )

    button.Button(
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
        .title(s"${YELLOW}${UNDERLINE}${BOLD}ホームへ")
        .lore(List(s"${RESET}${DARK_RED}${UNDERLINE}クリックで移動"))
        .build(),
      FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE)(_ => buttonEffect)
    )
  }
}