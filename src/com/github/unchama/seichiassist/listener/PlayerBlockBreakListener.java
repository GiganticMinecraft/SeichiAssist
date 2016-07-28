package com.github.unchama.seichiassist.listener;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Util;

public class PlayerBlockBreakListener implements Listener {
	//ブロックが壊された時に実行
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerBlockBreakEvent(BlockBreakEvent event){
		Player player = event.getPlayer();
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return;
		}
		PlayerInventory inventory = player.getInventory();
		ItemStack tool = inventory.getItemInMainHand();
		Block block = event.getBlock();
		CoreProtectAPI CoreProtect = getCoreProtect();
		Block breakblock = block.getWorld().getBlockAt(block.getX(),block.getY() + 1, block.getZ());
		BlockState blockstate = breakblock.getState();
		byte data = blockstate.getData().getData();
		if(breakblock.getType().equals(Material.STONE)){
			breakblock.breakNaturally();
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND,Material.STONE);
			short d = tool.getDurability();
			tool.setDurability((short)(d + calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY))));
			player.incrementStatistic(Statistic.MINE_BLOCK, Material.STONE);
			CoreProtect.logRemoval(player.getName(), breakblock.getLocation(), blockstate.getType(),data);
		}
/*
		event.setExpToDrop(10);
		Location loc = block.getLocation();
		for(double y = loc.getY()-1.0;y<=loc.getY()+1;y++){
			for(double x = loc.getX()-1.0;x<=loc.getX()+1;x++){
				for(double z = loc.getZ() -1.0;z<=loc.getZ()+1;z++){
					Location breakloc = new Location(loc.getWorld(), x, y, z);
					Block breakblock = breakloc.getBlock();
					if(breakblock.getType().equals(Material.STONE)){
						breakblock.breakNaturally();
						breakblock.getWorld().playEffect(breakloc, Effect.STEP_SOUND,Material.STONE);
						@SuppressWarnings("deprecation")
						boolean success = CoreProtect.logRemoval(player.getName().toLowerCase(), breakloc, breakblock.getType(),breakblock.getState().getData().getData());
						if(success = false){
							player.sendMessage("保存に失敗しました。");
						}else{
							player.sendMessage("保存" + breakblock.getState().getData().getData());
						}
					}
				}
			}
		}
		*/

	}

	public static short calcDurability(int enchantmentLevel) {
		double rand = Math.random();
		double probability = 1.0 / (enchantmentLevel + 1.0);
		Util.sendEveryMessage("enchantmentLevel:"+enchantmentLevel);
		Util.sendEveryMessage("probability:"+probability);

		if(probability <=  rand ){
			return 0;
		}
		return 1;
	}

	private CoreProtectAPI getCoreProtect() {
		Plugin plugin = SeichiAssist.plugin.getServer().getPluginManager().getPlugin("CoreProtect");

		// Check that CoreProtect is loaded
		if (plugin == null || !(plugin instanceof CoreProtect)) {
		    return null;
		}

		// Check that the API is enabled
		CoreProtectAPI CoreProtect = ((CoreProtect)plugin).getAPI();
		if (CoreProtect.isEnabled()==false){
		    return null;
		}

		// Check that a compatible version of the API is loaded
		if (CoreProtect.APIVersion() < 4){
		    return null;
		}

		return CoreProtect;
		}
}
