package com.github.unchama.seichiassist.data.inventory.slot.handler;

import com.github.unchama.seichiassist.data.inventory.slot.button.Button;

/**
 * @author karayuu
 */
public class SlotActionHandlers {
    /**
     * {@link Button} をReadOnly(移動不可)に可能な {@link SlotActionHandler} です.
     * {@link Button} には常に付与されます.
     */
    public static SlotActionHandler READ_ONLY = new SlotActionHandler(
            event -> true,
            event -> event.setCancelled(true)
    );
}
