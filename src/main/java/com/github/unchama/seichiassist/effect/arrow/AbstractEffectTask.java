package com.github.unchama.seichiassist.effect.arrow;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.effect.FixedMetadataValueHolder;
import com.github.unchama.seichiassist.task.AsyncEntityRemover;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public abstract class AbstractEffectTask<P extends Projectile> extends BukkitRunnable {
    protected P projectile;

    public P getProjectile() {
        return projectile;
    }

    public final BukkitRunnable abort = new BukkitRunnable() {
        @Override
        public void run() {
            new AsyncEntityRemover(projectile).run();
            cancel();
        }
    };

    public abstract Vector getAdditionalVector();

    public abstract double getVectorMultiplier();

    public void launchProjectile(Vector vec) {
        projectile.setMetadata("ArrowSkill", FixedMetadataValueHolder.TRUE);
        projectile.setVelocity(vec);
        abort.runTaskLater(SeichiAssist.instance, 100L);
    }
 }
