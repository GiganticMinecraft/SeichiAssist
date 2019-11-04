package com.github.unchama.buildassist.listener;

import com.github.unchama.buildassist.BuildAssist$;
import com.github.unchama.buildassist.LoadPlayerDataTaskRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements TypedEventListener<PlayerJoinEvent> {
    @Override
    @EventHandler(priority = EventPriority.HIGH)
    public void onEvent(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        //DBからデータを読み込むのを待ってから初期化
        new LoadPlayerDataTaskRunnable(player).runTaskTimerAsynchronously(BuildAssist$.MODULE$.plugin(), 0, 20);

    }
}
