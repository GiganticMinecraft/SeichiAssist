package com.github.unchama.seichiassist.data.slot.button.toggle;

import com.github.unchama.seichiassist.data.slot.button.Button;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author karayuu
 */
public class CircularButtonList {
    private final List<Button> buttons;

    public CircularButtonList(@Nonnull List<Button> buttons) {
        this.buttons = requireNonNull(buttons);
    }

    public void addLast(@Nonnull Button button) {
        buttons.add(button);
    }

    public Button next(@Nonnull Button before) {
        final int index = buttons.indexOf(before);
        final int max_index = buttons.size() - 1;
        if (index == max_index) {
            return buttons.get(0);
        } else {
            return buttons.get(index + 1);
        }
    }
}
