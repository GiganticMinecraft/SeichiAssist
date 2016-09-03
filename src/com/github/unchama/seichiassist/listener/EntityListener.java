package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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

	@EventHandler
	public void PvPToggleEvent(EntityDamageByEntityEvent event){
		Entity damager = getAttacker(event);
		Entity entity = event.getEntity();
		if(damager instanceof Player && entity instanceof Player){
			UUID uuid_damager = damager.getUniqueId();
			PlayerData playerdata_damager = playermap.get(uuid_damager);
			if(!playerdata_damager.pvpflag){
				event.setCancelled(true);
				return;
			}

			UUID uuid_entity = entity.getUniqueId();
			PlayerData playerdata_entity = playermap.get(uuid_entity);
			if(!playerdata_entity.pvpflag){
				event.setCancelled(true);
				return;
			}

		}
	}


	private Entity getAttacker(final EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile)
			return (Entity) ((Projectile) event.getDamager()).getShooter();
		return event.getDamager();
	}

}
