package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.commands.legacy.GachaCommand;
import com.github.unchama.seichiassist.data.ItemData;
import com.github.unchama.seichiassist.event.SeichiLevelUpEvent;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;


/**
 * Created by karayuu on 2018/04/19
 */
public class PlayerSeichiLevelUpListener {
    @EventHandler
    public void onPlayerLevelUp(SeichiLevelUpEvent event) {
        Player p = event.getPlayer();

        switch (event.getLevelAfterLevelUp()) {
            case 10:
                Util.addItemToPlayerSafely(p, ItemData.getSuperPickaxe(5));
            case 20: {
                GachaCommand.Gachagive(p, 3, p.getName());
                GachaCommand.Gachagive(p, 10, p.getName());
            }
            case 30:
                Util.addItemToPlayerSafely(p, ItemData.getForLevelUpskull(p.getName(), 256));
            case 40:
                Util.addItemToPlayerSafely(p, ItemData.getGachaApple(256));
            case 50:
                GachaCommand.Gachagive(p, 27, p.getName());
            case 60:
                GachaCommand.Gachagive(p, 26, p.getName());
            case 70:
                GachaCommand.Gachagive(p, 25, p.getName());
            case 80:
                GachaCommand.Gachagive(p, 24, p.getName());
            case 90:
                GachaCommand.Gachagive(p, 20, p.getName());
            case 100: {
                GachaCommand.Gachagive(p, 21, p.getName());
                Util.addItemToPlayerSafely(p, ItemData.getElsa(1));
            }
        }
    }
}
