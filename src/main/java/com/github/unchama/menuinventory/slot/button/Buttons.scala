package com.github.unchama.menuinventory.slot.button

import com.github.unchama.util.kotlin2scala.SuspendingMethod

object RecomputedButton {
  /**
   * クリックされるたびに[buttonComputation]に基づいてスロット自体が更新される[Button]を作成する.
   */
  @SuspendingMethod def recomputedButton(buttonComputation: suspend () => Button): Button =
    buttonComputation().withAnotherEffect(
      action.ButtonEffect {
        deferredEffect {
          overwriteCurrentSlotBy(recomputedButton(buttonComputation))
        }
      }
    )
}
