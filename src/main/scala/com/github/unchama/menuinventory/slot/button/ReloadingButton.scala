package com.github.unchama.menuinventory.slot.button

import cats.data.Kleisli
import com.github.unchama.menuinventory.slot.button.action.ButtonEffect
import com.github.unchama.menuinventory.{LayoutPreparationContext, Menu}
import org.bukkit.entity.Player

object ReloadingButton {
  /**
   * クリックされるたびに[buttonComputation]に基づいてスロット自体が更新される[Button]を作成する.
   */
  def apply(menu: Menu)(button: Button)(implicit ctx: LayoutPreparationContext): Button = {
    button.withAnotherEffect(
      ButtonEffect(scope => {
        val clicker = scope.event.getWhoClicked.asInstanceOf[Player]
        Kleisli.liftF(
          for {
            newLayout <- menu.computeMenuLayout(clicker)
            _ <- scope.overwriteCurrentViewBy(newLayout)
          } yield ()
        )
      })
    )
  }
}
