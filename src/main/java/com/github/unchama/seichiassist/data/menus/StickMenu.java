package com.github.unchama.seichiassist.data.menus;

import com.github.unchama.seichiassist.data.button.PlayerDataButtons;
import com.github.unchama.seichiassist.data.menu.Menu;
import com.github.unchama.seichiassist.data.menu.chest.ChestMenuBuilder;

import javax.annotation.Nonnull;

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
public class StickMenu {
    @Nonnull
    public static Menu stickMenu;

    private StickMenu() {
    }

    static {
        stickMenu = ChestMenuBuilder.of()
                .size(4 * 9)
                .slots(
                    PlayerDataButtons.playerInfo
                )
                .build();
    }
}
