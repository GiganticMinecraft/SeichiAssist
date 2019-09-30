package com.github.unchama.buildassist.listener;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.PlayerData;
import com.github.unchama.buildassist.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import scala.collection.mutable.HashMap;

import java.math.BigDecimal;
import java.util.UUID;


public class BlockPlaceEventListener implements TypedEventListener<BlockPlaceEvent> {
    private final HashMap<UUID, PlayerData> playermap = BuildAssist.playermap();

    @Override
    @EventHandler
    public void onEvent(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if (!Util.inTrackedWorld(player)) {
            return;
        }

        final UUID uuid = player.getUniqueId();
        final PlayerData playerdata = playermap.getOrElse(uuid, () -> null);

        //プレイヤーデータが無い場合は処理終了
        if (playerdata == null) return;

        Util.addBuild1MinAmount(player, BigDecimal.ONE);
    }
}
