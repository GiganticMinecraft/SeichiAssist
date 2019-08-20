package com.github.unchama.buildassist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;


public class PlayerQuitListener implements Listener {
    Plugin plugin = BuildAssist.plugin;
    HashMap<UUID, PlayerData> playermap = BuildAssist.playermap;

    //プレイヤーがquitした時に実行
    //SeichiAssistより先に実行させるために優先付け
    @EventHandler(priority = EventPriority.LOWEST)
    public void onplayerQuitEvent(PlayerQuitEvent event) {
        //退出したplayerを取得
        Player player = event.getPlayer();
        //プレイヤーのuuidを取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ取得
        PlayerData playerdata = playermap.get(uuid);


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