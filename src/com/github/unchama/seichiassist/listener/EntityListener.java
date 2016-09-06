package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
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
	SeichiAssist plugin = SeichiAssist.plugin;
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
			//念のためエラー分岐
			if(playerdata_damager == null){
				damager.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[PvP処理]でエラー発生");
				plugin.getLogger().warning(damager.getName()+ "のplayerdataがありません。開発者に報告してください");
				return;
			}
			if(!playerdata_damager.pvpflag){
				event.setCancelled(true);
				return;
			}

			UUID uuid_entity = entity.getUniqueId();
			PlayerData playerdata_entity = playermap.get(uuid_entity);
			//念のためエラー分岐
			if(playerdata_entity == null){
				entity.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[PvP処理]でエラー発生");
				plugin.getLogger().warning(entity.getName()+ "のplayerdataがありません。開発者に報告してください");
				return;
			}
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
