package com.github.unchama.seichiassist.data;

/**
 * 釣りシステム移行用
 * 移行者:karayuu
 */
public class FishingLevel {
    private int level;
    // このlevelになるのに必要な経験値
    private double need_exp;
    // 次のlevelになるのに必要な経験値
    private double next_exp;

    public FishingLevel(int level_) {
        level = level_;
        need_exp = calcNeedExp(level);
        //Bukkit.getServer().getLogger().info("fishing" + level + " : " + need_exp);
        next_exp = calcNeedExp(level + 1);
    }

    private double calcNeedExp(int level) {
        return (Math.pow((level - 1), 3)) * 10;
    }

    public int getLevel() {
        return this.level;
    }

    public double getNeedExp() {
        return this.need_exp;
    }

    public double getNextExp() {
        return this.next_exp;
    }
}
