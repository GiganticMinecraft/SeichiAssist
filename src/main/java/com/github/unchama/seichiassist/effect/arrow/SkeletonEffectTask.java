package com.github.unchama.seichiassist.effect.arrow;

import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * このクラスは同期的非同期でエンティティを飛ばすのに便利です。
 * @param <E> クライアントで実際に見せるエンティティ。
 */
public abstract class SkeletonEffectTask<E extends Projectile> extends BukkitRunnable {
    protected E proj;

    /*
    public SkeletonEffectTask(Player player) {
        this.player = player;
        data = SeichiAssist.playermap.get(this.player.getUniqueId());
    }
    */

    /*
    public void initRun() {

    }
    */

    /**
     * super.runを実装クラスで呼ばないと {@link #initRun()}がよばれません。注意してください。
     */
    /*
    @Override
    public void run() {
        if (!triggered) {
            initRun();
            triggered = true;
        }
        proj.setShooter(player);
        proj.setGravity(false);
    }
     */
}
