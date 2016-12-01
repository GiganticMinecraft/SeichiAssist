package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerPickupItemListener implements Listener {
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Config config = SeichiAssist.config;
	@EventHandler
	public void MineStackEvent(PlayerPickupItemEvent event){
		//実行したプレイヤーを取得
		Player player = event.getPlayer();
		//もしサバイバルでなければ処理を終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return;
		}
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			return;
		}
		//レベルが足りない場合処理終了
		if(playerdata.level < config.getMineStacklevel(1)){
			return;
		}
		//minestackflagがfalseの時は処理を終了
		if(!playerdata.minestackflag){
			return;
		}

		Item item = event.getItem();
		ItemStack itemstack = item.getItemStack();
		int amount = itemstack.getAmount();
		Material material = itemstack.getType();

		int v1 = config.getMineStacklevel(1);
		int v2 = config.getMineStacklevel(2);
		int v3 = config.getMineStacklevel(3);
		int v4 = config.getMineStacklevel(4);
		int v5 = config.getMineStacklevel(5);
		int v6 = config.getMineStacklevel(6);
		int v7 = config.getMineStacklevel(7);
		int v8 = config.getMineStacklevel(8);
		int v9 = config.getMineStacklevel(9);
		int v10 = config.getMineStacklevel(10);
		int v11 = config.getMineStacklevel(11);//追加
		int v12 = config.getMineStacklevel(12);//追加


		switch(material){
			case DIRT:
				if(playerdata.level < v1){
					return;
				}
				playerdata.minestack.dirt += amount;
				break;
			case GRASS:
				if(playerdata.level < v1){
					return;
				}
				playerdata.minestack.grass += amount;
				break;
			case GRAVEL:
				if(playerdata.level < v2){
					return;
				}
				playerdata.minestack.gravel += amount;
				break;
			case COBBLESTONE:
				if(playerdata.level < v3){
					return;
				}
				playerdata.minestack.cobblestone += amount;
				break;
			case STONE:
				if(playerdata.level < v3){
					return;
				}
				playerdata.minestack.stone += amount;
				break;
			case SAND:
				if(playerdata.level < v4){
					return;
				}
				playerdata.minestack.sand += amount;
				break;
			case PACKED_ICE:
				if(playerdata.level < v4){
					return;
				}
				playerdata.minestack.packed_ice += amount;
				break;
			case SANDSTONE:
				if(playerdata.level < v4){
					return;
				}
				playerdata.minestack.sandstone += amount;
				break;
			case NETHERRACK:
				if(playerdata.level < v5){
					return;
				}
				playerdata.minestack.netherrack += amount;
				break;
			case SOUL_SAND:
				if(playerdata.level < v6){
					return;
				}
				playerdata.minestack.soul_sand += amount;
				break;
			case MAGMA:
				if(playerdata.level < v6){
					return;
				}
				playerdata.minestack.magma += amount;
				break;
			case ENDER_STONE:
				if(playerdata.level < v7){
					return;
				}
				playerdata.minestack.ender_stone += amount;
				break;
			case OBSIDIAN: //追加
				if(playerdata.level < v7){
					return;
				}
				playerdata.minestack.obsidian += amount;
				break;	
			case COAL:
				if(playerdata.level < v8){
					return;
				}
				playerdata.minestack.coal += amount;
				break;
			case COAL_ORE:
				if(playerdata.level < v8){
					return;
				}
				playerdata.minestack.coal_ore += amount;
				break;
			case IRON_ORE:
				if(playerdata.level < v9){
					return;
				}
				playerdata.minestack.iron_ore += amount;
				break;
			case INK_SACK:
				if(playerdata.level < v9 || itemstack.getDurability() == 4){
					return;
				}
				playerdata.minestack.lapis_lazuli += amount;
				break;
			case QUARTZ:
				if(playerdata.level < v10){
					return;
				}
				playerdata.minestack.quartz += amount;
				break;
			case QUARTZ_ORE:
				if(playerdata.level < v10){
					return;
				}
				playerdata.minestack.quartz_ore += amount;
				break;
			case GOLD_ORE: //追加
				if(playerdata.level < v11){
					return;
				}
				playerdata.minestack.gold_ore += amount;
				break;
			case LAPIS_ORE: //追加
				if(playerdata.level < v11){
					return;
				}
				playerdata.minestack.lapis_ore += amount;
				break;
			case EMERALD_ORE: //追加
				if(playerdata.level < v11){
					return;
				}
				playerdata.minestack.emerald_ore += amount;
				break;
			case REDSTONE_ORE: //追加
				if(playerdata.level < v12){
					return;
				}
				playerdata.minestack.redstone_ore += amount;
				break;
			case DIAMOND_ORE: //追加
				if(playerdata.level < v12){
					return;
				}
				playerdata.minestack.diamond_ore += amount;
				break;
			case LOG: //追加
				if(playerdata.level < v4){
					return;
				}
				playerdata.minestack.log += amount;
				break;
			case LOG_2: //追加
				if(playerdata.level < v4){
					return;
				}
				playerdata.minestack.log_2 += amount;
				break;
			case WOOD: //追加
				if(playerdata.level < v5){
					return;
				}
				playerdata.minestack.wood += amount;
				break;
			case HARD_CLAY: //追加
				if(playerdata.level < v5){
					return;
				}
				playerdata.minestack.hard_clay += amount;
				break;
			case STAINED_CLAY: //追加
				if(playerdata.level < v5){
					return;
				}
				playerdata.minestack.stained_clay += amount;
				break;
			case FENCE: //追加
				if(playerdata.level < v5){
					return;
				}
				playerdata.minestack.fence += amount;
				break;
			default:
				return;
		}
		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
		item.remove();
	}
}