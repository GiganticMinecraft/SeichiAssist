package com.github.unchama.seichiassist.util.exp;

import org.bukkit.entity.Player;

public interface IExperienceManager {
    Player getPlayer();

    void changeExp(int amt);

    void changeExp(double amt);

    void setExp(int amt);

    void setExp(double amt);

    int getCurrentExp();

    boolean hasExp(int amt);

    boolean hasExp(double amt);

    int getLevelForExp(int exp);

    int getXpNeededToLevelUp(int level);

    int getXpForLevel(int level);
}
