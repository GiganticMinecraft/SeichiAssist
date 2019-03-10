package com.github.unchama.seichiassist.arroweffect;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.unchama.seichiassist.SeichiAssist;

public class ArrowBlizzardTaskRunnable extends BukkitRunnable {
    private long tick;
    private Snowball proj;

    public ArrowBlizzardTaskRunnable(Player player) {
        this.tick = 0;
        //プレイヤーの位置を取得
        Location ploc = player.getLocation();

        //発射する音を再生する.
        player.playSound(ploc, Sound.ENTITY_SNOWBALL_THROW, 1, (float) 1.3);

        //スキルを実行する処理
        Location loc = player.getLocation().clone();
        loc.add(loc.getDirection()).add(0, 1.6, 0);
        Vector vec = loc.getDirection();
        double k = 1.0;
        vec.setX(vec.getX() * k);
        vec.setY(vec.getY() * k);
        vec.setZ(vec.getZ() * k);

        //エンティティを生成し,記録する.
        proj = player.getWorld().spawn(loc, Snowball.class);
        SeichiAssist.entitylist.add(proj);

        proj.setShooter(player);
        proj.setGravity(false);
        proj.setMetadata("ArrowSkill", new FixedMetadataValue(SeichiAssist.plugin, true));
        proj.setVelocity(vec);
    }

    @Override
    public void run() {
        tick++;
        if (tick > 100) {
            proj.remove();
            SeichiAssist.entitylist.remove(proj);
            this.cancel();
            return;
        }
    }
}
