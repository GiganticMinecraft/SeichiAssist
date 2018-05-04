package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.commands.*;
import com.github.unchama.seichiassist.data.*;
import com.github.unchama.seichiassist.event.*;
import com.github.unchama.seichiassist.util.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;


/**
 * Created by karayuu on 2018/04/19
 */
public class PlayerSeichiLevelUpListener {
    @EventHandler
    public void onPlayerLevelUp(SeichiLevelUpEvent event) {
        Player p = event.getPlayer();

        switch (event.getLevelAfterLevelUp()) {
            case 10: Util.addItemToPlayerSafely(p, ItemData.getSuperPickaxe(5));
            case 20: {
                gachaCommand.Gachagive(p, 3, p.getName());
                gachaCommand.Gachagive(p, 10, p.getName());
            }
            case 30: Util.addItemToPlayerSafely(p, ItemData.getForLevelUpskull(p.getName(), 256));
            case 40: Util.addItemToPlayerSafely(p, ItemData.getGachaApple(256));
            case 50: gachaCommand.Gachagive(p, 27, p.getName());
            case 60: gachaCommand.Gachagive(p, 26, p.getName());
            case 70: gachaCommand.Gachagive(p, 25, p.getName());
            case 80: gachaCommand.Gachagive(p, 24, p.getName());
            case 90: gachaCommand.Gachagive(p, 20, p.getName());
            case 100: {
                gachaCommand.Gachagive(p, 21, p.getName());
                Util.addItemToPlayerSafely(p, ItemData.getElsa(1));
            }
        }
    }
}
