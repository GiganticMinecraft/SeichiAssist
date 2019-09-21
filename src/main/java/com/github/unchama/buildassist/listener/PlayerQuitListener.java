package com.github.unchama.buildassist.listener;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import scala.collection.mutable.HashMap;

import java.util.UUID;


public class PlayerQuitListener implements TypedEventListener<PlayerQuitEvent> {
    private final HashMap<UUID, PlayerData> playermap = BuildAssist.playermap();

    //プレイヤーがquitした時に実行
    //SeichiAssistより先に実行させるために優先付け
    @Override
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(final PlayerQuitEvent event) {
        //退出したplayerを取得
        final Player player = event.getPlayer();
        //プレイヤーのuuidを取得
        final UUID uuid = player.getUniqueId();
        //プレイヤーデータ取得
        final PlayerData playerdata = playermap.getOrElse(uuid, () -> null);

        //念のためエラー分岐
        if (playerdata == null) {
            Bukkit.getLogger().warning(player.getName() + " -> のplayerdataロスト");
            Bukkit.getLogger().warning("BuildAssist.PlayerQuitListener.onplayerQuitEvent");
            return;
        }

        //建築系データを保存
        playerdata.buildsave(player);
        //不要なplayerdataを削除
        playermap.remove(uuid);
    }

}