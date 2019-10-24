package com.github.unchama.buildassist.listener;

import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityListener implements Listener {

    @EventHandler
    public void onEntityExplodeEvent(final EntityExplodeEvent event) {
        if ((event.getEntity() instanceof LargeFireball)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
        if (((event.getDamager() instanceof LargeFireball))
                && ((event.getEntity() instanceof Player))) {
            event.setCancelled(true);
        }
    }
}
