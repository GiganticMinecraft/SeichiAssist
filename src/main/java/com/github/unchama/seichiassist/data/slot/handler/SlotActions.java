package com.github.unchama.seichiassist.data.slot.handler;

import com.github.unchama.seichiassist.data.slot.button.Button;

/**
 * @author karayuu
 */
public class SlotActions {
    /**
     * {@link Button} に対する操作をキャンセルする {@link SlotAction} です.
     * {@link Button} には常に付与されます.
     */
    public static final SlotAction READ_ONLY = new SlotAction(
            ClickEventFilter.ALWAYS_TRUE,
            event -> event.setCancelled(true)
    );
}
