package com.github.unchama.seichiassist.effect.arrow;

import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class AbstractEffectTask<P extends Projectile> extends BukkitRunnable {
    protected P projectile;

    public P getProjectile() {
        return projectile;
    }

    /*
    public abstract Vector getAddtionalVector();

    public abstract double getVectorMultipier();
     */
 }
