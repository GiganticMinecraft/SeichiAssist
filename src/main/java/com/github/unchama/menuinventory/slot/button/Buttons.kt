package com.github.unchama.menuinventory.slot.button

import com.github.unchama.menuinventory.slot.button.action.ButtonEffect
import com.github.unchama.targetedeffect.deferredEffect

/**
 * クリックされるたびに[buttonComputation]に基づいてスロット自体が更新される[Button]を作成する.
 */
suspend fun recomputedButton(buttonComputation: suspend () -> Button): Button =
    buttonComputation().withAnotherEffect(
      ButtonEffect {
        deferredEffect {
          overwriteCurrentSlotBy(recomputedButton(buttonComputation))
        }
      }
    )
