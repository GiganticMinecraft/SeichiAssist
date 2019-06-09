package com.github.unchama.seichiassist.effect.breaking;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class SkeletonBreakTask extends BukkitRunnable {
    private int round = 0;
    public abstract void callFirst();

    public abstract void callSecond();

    public abstract void callAfterThird();

    @Override
    public void run() {
        round++;
        switch (round) {
            case 1:
                callFirst();
                break;
            case 2:
                callSecond();
                break;
            default:
                callAfterThird();
                break;
        }
    }
}
