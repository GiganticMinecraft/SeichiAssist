package com.github.unchama.seichiassist.data.menus;

import com.github.unchama.seichiassist.data.button.PlayerDataButtons;
import com.github.unchama.seichiassist.data.inventory.menu.Menu;
import com.github.unchama.seichiassist.data.inventory.menu.chest.ChestMenuBuilder;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.ChatColor;

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
                                    .title(Text.of("木の棒メニュー", ChatColor.DARK_PURPLE, ChatColor.BOLD))
                                    .slots(
                                        PlayerDataButtons.playerInfo
                                    )
                                    .build();
    }
}
