package com.github.unchama.seichiassist.arroweffect;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.breakeffect.BladeTaskRunnable;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

/**
 * 遠距離スキルのブレイドエフェクトを表現するエンティティを管理するクラスです.
 * @see BladeTaskRunnable 範囲採掘系スキルのエフェクト処理.
 * @deprecated エフェクトが有効化していないため.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class ArrowBladeTaskRunnable extends BukkitRunnable {
    /**
     * ブレイドの遠距離スキルのエフェクトを表現するエンティティを生成します.<br>
     * オブジェクト生成によりエンティティが生成されます.
     * @param player スキルを発動したプレイヤー.{@code null} は許容されません.
     */
    public ArrowBladeTaskRunnable(@Nonnull Player player) {
        //プレイヤーの位置を取得
        Location playerLocation = player.getLocation();
        //発射する音を再生する.
        player.playSound(playerLocation, Sound.ENTITY_GHAST_SHOOT, 1, (float) 1.3);

        //スキルを実行する処理
        Location loc = player.getLocation().clone();
        loc.add(loc.getDirection()).add(0, 1.6, 0);
        Vector vec = loc.getDirection();
        double k = 0.4;
        vec.setX(vec.getX() * k);
        vec.setY(vec.getY() * k);
        vec.setZ(vec.getZ() * k);
        SmallFireball entity = player.getWorld().spawn(loc, SmallFireball.class);
        SeichiAssist.entitylist.add(entity);
        entity.setShooter(player);
        entity.setGravity(false);
        entity.setMetadata("ArrowSkill", new FixedMetadataValue(SeichiAssist.plugin, true));
        entity.setVelocity(vec);
    }

    @Override
    public void run() {

    }
}
