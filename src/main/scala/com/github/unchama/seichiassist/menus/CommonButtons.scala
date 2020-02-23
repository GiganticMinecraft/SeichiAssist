package com.github.unchama.seichiassist.menus

import com.github.unchama.concurrent.BukkitSyncIOShift
import com.github.unchama.itemstackbuilder.{AbstractItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.{LayoutPreparationContext, Menu}
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.seichiassist.menus.ColorScheme.{clickResultDescription, navigation}
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.seichiassist.{CommonSoundEffects, SkullOwners}

/**
 * メニューUIに頻繁に現れるような[Button]を生成する、または定数として持っているオブジェクト.
 */
object CommonButtons {

  import com.github.unchama.targetedeffect._

  def transferButton(partialBuilder: AbstractItemStackBuilder[Nothing],
                     transferDescription: String,
                     target: Menu,
                     actionDescription: String = "クリックで移動")
                    (implicit layoutPreparationContext: LayoutPreparationContext, syncCtx: BukkitSyncIOShift): Button =
    Button(
      partialBuilder
        .title(navigation(transferDescription))
        .lore(List(clickResultDescription(actionDescription)))
        .build(),
      action.LeftClickButtonEffect(
        sequentialEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          target.open
        )
      )
    )

  val openStickMenu: Button = {
    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}

    transferButton(new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft), "木の棒メニューホームへ", StickMenu.firstPage)
  }
}