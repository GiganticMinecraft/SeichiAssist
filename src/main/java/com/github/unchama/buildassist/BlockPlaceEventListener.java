package com.github.unchama.buildassist;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;


public class BlockPlaceEventListener implements Listener {
    private final HashMap<UUID, PlayerData> playermap = BuildAssist.playermap;

    //ブロックを設置した時のイベント
    @EventHandler
    public void onBlockPlaceEvent(final BlockPlaceEvent event) {
        //playerを取得
        final Player player = event.getPlayer();
        //カウント対象ワールドかチェック
        if (!Util.inTrackedWorld(player)) {
            return;
        }

        //プレイヤーのuuidを取得
        final UUID uuid = player.getUniqueId();
        //プレイヤーデータ取得
        final PlayerData playerdata = playermap.get(uuid);
        //プレイヤーデータが無い場合は処理終了
        if (playerdata == null) {
            return;
        }

        Util.addBuild1MinAmount(player, BigDecimal.ONE);
    }

}
