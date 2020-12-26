package com.github.unchama.seichiassist.util.exp;

import org.bukkit.entity.Player;

public interface IExperienceManager {
    Player getPlayer();

    void changeExp(long amt);

    void changeExp(double amt);

    void setExp(long amt);

    void setExp(double amt);

    long getCurrentExp();

    boolean hasExp(long amt);

    boolean hasExp(double amt);

    long getLevelForExp(long exp);

    long getXpNeededToLevelUp(int level);

    long getXpForLevel(int level);
}
