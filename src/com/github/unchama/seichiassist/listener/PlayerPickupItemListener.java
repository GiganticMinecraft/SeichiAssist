package com.github.unchama.seichiassist.listener;

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

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerPickupItemListener implements Listener {
	@EventHandler
	public void MineStackEvent(PlayerPickupItemEvent event){
		//実行したプレイヤーを取得
		Player player = event.getPlayer();
		//もしサバイバルでなければ処理を終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return;
		}
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//レベルが足りない場合処理終了
		if(playerdata.level < SeichiAssist.config.getMineStacklevel()){
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


		switch(material){
			case STONE:
				playerdata.minestack.stone += amount;
				break;
			case COBBLESTONE:
				playerdata.minestack.cobblestone += amount;
				break;
			case DIRT:
				playerdata.minestack.dirt += amount;
				break;
			case GRAVEL:
				playerdata.minestack.gravel += amount;
				break;
			case SAND:
				playerdata.minestack.sand += amount;
				break;
			case SANDSTONE:
				playerdata.minestack.sandstone += amount;
				break;
			case NETHERRACK:
				playerdata.minestack.netherrack += amount;
				break;
			case ENDER_STONE:
				playerdata.minestack.ender_stone += amount;
				break;
			case GRASS:
				playerdata.minestack.grass += amount;
				break;
			case QUARTZ:
				playerdata.minestack.quartz += amount;
				break;
			case QUARTZ_ORE:
				playerdata.minestack.quartz_ore += amount;
				break;
			case SOUL_SAND:
				playerdata.minestack.soul_sand += amount;
				break;
			case MAGMA:
				playerdata.minestack.magma += amount;
				break;
			default:
				return;
		}
		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
		item.remove();
	}
}