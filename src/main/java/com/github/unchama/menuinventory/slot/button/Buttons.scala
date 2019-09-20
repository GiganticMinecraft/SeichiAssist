package com.github.unchama.menuinventory.slot.button

/**
 * クリックされるたびに[buttonComputation]に基づいてスロット自体が更新される[Button]を作成する.
 */
suspend def recomputedButton(buttonComputation: suspend () => Button): Button =
    buttonComputation().withAnotherEffect(
      ButtonEffect {
        deferredEffect {
          overwriteCurrentSlotBy(recomputedButton(buttonComputation))
        }
      }
    )
