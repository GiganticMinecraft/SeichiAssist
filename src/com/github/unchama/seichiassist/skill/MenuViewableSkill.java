package com.github.unchama.seichiassist.skill;

import org.bukkit.inventory.ItemStack;

/**
 * メニュー(Inventory)に表示することができるスキル全般を表します.
 *
 * @author karayuu
 */
public interface MenuViewableSkill extends Skill {
    /**
     * メニュー(Inventory)に表示するItemStackを取得します.
     *
     * @return
     */
    ItemStack getIcon();
}
