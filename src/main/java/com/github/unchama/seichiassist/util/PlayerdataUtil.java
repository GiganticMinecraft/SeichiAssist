package com.github.unchama.seichiassist.util;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author karayuu
 */
public class PlayerdataUtil {
    public static PlayerData playerData(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            throw new IllegalStateException("InventoryClickEvent#getWhoClickedはPlayer型でなくてはなりません.");
        }
        final Player player = (Player) event.getWhoClicked();
        return SeichiAssist.playermap.get(player.getUniqueId());
    }
}
