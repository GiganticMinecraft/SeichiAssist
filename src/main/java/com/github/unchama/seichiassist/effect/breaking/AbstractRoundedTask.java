package com.github.unchama.seichiassist.effect.breaking;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class AbstractRoundedTask extends BukkitRunnable {
    private int round = 0;

    public abstract void firstAction();

    public abstract void secondAction();

    public void otherwiseAction() {
        cancel();
    }

    @Override
    public void run() {
        round++;
        switch (round) {
            case 1:
                firstAction();
                break;
            case 2:
                secondAction();
                break;
            default:
                otherwiseAction();
                break;
        }
    }
}
