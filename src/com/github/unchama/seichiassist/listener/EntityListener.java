package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class EntityListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;


	@EventHandler
	public void onEntityExplodeEvent(EntityExplodeEvent event){
		if(event.getEntity() instanceof LargeFireball){
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof LargeFireball){
			if(event.getEntity() instanceof Player){
				event.setCancelled(true);
			}
		}
	}

}
