package com.github.unchama.seichiassist.data.slot.handler;

import com.github.unchama.seichiassist.data.slot.button.Button;

/**
 * @author karayuu
 */
public class SlotActionHandlers {
    /**
     * {@link Button} に対する操作をキャンセルする {@link SlotActionHandler} です.
     * {@link Button} には常に付与されます.
     */
    public static SlotActionHandler READ_ONLY = new SlotActionHandler(
            Trigger.ALWAYS_TRUE,
            event -> event.setCancelled(true)
    );
}
