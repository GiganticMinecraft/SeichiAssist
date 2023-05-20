package com.github.unchama.menuinventory.slot.button

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.slot.button.action.ButtonEffect
import com.github.unchama.minecraft.actions.OnMinecraftServerThread

object RecomputedButton {

  /**
   * クリックされるたびに[buttonComputation]に基づいてスロット自体が更新される[Button]を作成する.
   */
  def apply(buttonComputation: IO[Button])(
    implicit ctx: LayoutPreparationContext,
    onMainThread: OnMinecraftServerThread[IO]
  ): IO[Button] =
    buttonComputation.map { computedButton =>
      val recomputation =
        ButtonEffect(scope =>
          Kleisli.liftF(this(buttonComputation).flatMap(scope.overwriteCurrentSlotBy))
        )

      computedButton.withAnotherEffect(recomputation)
    }
}
