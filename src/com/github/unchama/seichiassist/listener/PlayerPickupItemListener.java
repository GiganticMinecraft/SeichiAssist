package com.github.unchama.seichiassist.listener;

import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

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
		switch(item.getItemStack().getType()){
			case STONE:
				playerdata.minestack.stone += item.getItemStack().getAmount();
				break;
			case COBBLESTONE:
				playerdata.minestack.cobblestone += item.getItemStack().getAmount();
				break;
			case DIRT:
				playerdata.minestack.dirt += item.getItemStack().getAmount();
				break;
			case GRAVEL:
				playerdata.minestack.gravel += item.getItemStack().getAmount();
				break;
			case SAND:
				playerdata.minestack.sand += item.getItemStack().getAmount();
				break;
			case SANDSTONE:
				playerdata.minestack.sandstone += item.getItemStack().getAmount();
				break;
			case NETHERRACK:
				playerdata.minestack.netherrack += item.getItemStack().getAmount();
				break;
			case ENDER_STONE:
				playerdata.minestack.ender_stone += item.getItemStack().getAmount();
				break;
			case GRASS:
				playerdata.minestack.grass += item.getItemStack().getAmount();
				break;
			case QUARTZ:
				playerdata.minestack.quartz += item.getItemStack().getAmount();
				break;
			case QUARTZ_ORE:
				playerdata.minestack.quartz_ore += item.getItemStack().getAmount();
				break;
			case SOUL_SAND:
				playerdata.minestack.soul_sand += item.getItemStack().getAmount();
				break;
			case MAGMA:
				playerdata.minestack.magma += item.getItemStack().getAmount();
				break;
			default:
				return;
		}
		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
		item.remove();
	}
}
/*
stone = 0;
dirt = 0;
gravel = 0;
coal_ore = 0;
iron_ore = 0;
gord_ore = 0;
diamond_ore = 0;
lapis_ore = 0;
emerald_ore = 0;
sand = 0;
end_bricks = 0;
ender_stone = 0;
obsidian = 0;
*/