package com.github.unchama.buildassist;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

import com.github.unchama.seichiassist.PackagePrivate;

@PackagePrivate
class BuildBlock {
    private final int after;
    private final int before;
    private int increase;
    
    @PackagePrivate
    BuildBlock() {
        this.after = 0;
        this.before = 0;
        this.increase = 0;
    }

    @PackagePrivate
    static BigDecimal calcBuildBlock(final Player player) {
        BigDecimal sum = BigDecimal.ZERO;
        for (final Material m : BuildAssist.materiallist()) {
            sum = new BigDecimal(player.getStatistic(Statistic.USE_ITEM, m));
        }
        return sum;
    }

    public void setIncrease() {
        this.increase = (this.after - this.before);
    }
}
