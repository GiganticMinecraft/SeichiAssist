package com.github.unchama.seichiassist.menus

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{AbstractItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.Menu
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.ColorScheme.{clickResultDescription, navigation}
import com.github.unchama.seichiassist.menus.stickmenu.{FirstPage, StickMenu}
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import org.bukkit.entity.Player

/**
 * メニューUIに頻繁に現れるような[Button]を生成する、または定数として持っているオブジェクト.
 */
object CommonButtons {

  import com.github.unchama.targetedeffect._

  def transferButton[M <: Menu](
    partialBuilder: AbstractItemStackBuilder[Nothing],
    transferDescription: String,
    target: M,
    actionDescription: String = "クリックで移動"
  )(implicit canOpenM: CanOpen[IO, M]): Button =
    Button(
      partialBuilder
        .title(navigation(transferDescription))
        .lore(List(clickResultDescription(actionDescription)))
        .build(),
      action.LeftClickButtonEffect(
        SequentialEffect(CommonSoundEffects.menuTransitionFenceSound, canOpenM.open(target))
      )
    )

  def openStickMenu(
    implicit canOpenStickMenu: CanOpen[IO, FirstPage.type],
    playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
  ): Button = {
    transferButton(
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
      "木の棒メニューホームへ",
      StickMenu.firstPage
    )
  }
}
