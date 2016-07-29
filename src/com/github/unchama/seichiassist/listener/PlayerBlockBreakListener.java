package com.github.unchama.seichiassist.listener;

import net.coreprotect.CoreProtectAPI;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.github.unchama.seichiassist.ExperienceManager;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Util;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerBlockBreakListener implements Listener {

	//ブロックが壊された時に実行
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerBlockBreakEvent(BlockBreakEvent event){
		Player player = event.getPlayer();
		ExperienceManager expman = new ExperienceManager(player);
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return;
		}
		PlayerData playerdata = SeichiAssist.playermap.get(Util.getName(player));

		PlayerInventory inventory = player.getInventory();
		ItemStack tool = inventory.getItemInMainHand();
		Block block = event.getBlock();
		Material material = block.getType();
		if(!SeichiAssist.materiallist.contains(material)){
			return;
		}
		//壊されたブロックのみの処理
		int blockexpdrop = event.getExpToDrop();
		//int expdrop = (int)(blockexpdrop + (blockexpdrop * 0.3) + 1);
		event.setExpToDrop(calcExpDrop(blockexpdrop,playerdata.dropexpprobability));
		if(!playerdata.activemineflag){
			return;
		}
		if(player.getLevel()==0 && player.getExp()<3){
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
			}
			return;
		}
		//以下アクティブスキルで壊されるブロックの処理
		CoreProtectAPI CoreProtect = Util.getCoreProtect();
		Block breakblock = block.getWorld().getBlockAt(block.getX(),block.getY() + 1, block.getZ());
		BlockState blockstate = breakblock.getState();
		byte data = blockstate.getData().getData();
		if(breakblock.getType().equals(material)){
			Location breakloc = breakblock.getLocation();
			ExperienceOrb orb = breakloc.getWorld().spawn(breakloc, ExperienceOrb.class);
			breakblock.breakNaturally();
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND,Material.STONE);
			breakblock.getWorld().playSound(breakblock.getLocation(), Sound.ENTITY_IRONGOLEM_ATTACK,1,1);
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.WITCH_MAGIC, (byte)0);
			orb.setExperience(calcExpDrop(blockexpdrop,playerdata.dropexpprobability));
			expman.changeExp(-3);

			short d = tool.getDurability();
			tool.setDurability((short)(d + calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY))));
			player.incrementStatistic(Statistic.MINE_BLOCK, Material.STONE);
			Boolean success = CoreProtect.logRemoval(player.getName(), breakblock.getLocation(), blockstate.getType(),data);
			if(!success){
				player.sendMessage("coreprotectに保存できませんでした。");
			}
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

	public static int calcExpDrop(int blockexpdrop,double probability) {
		double rand = Math.random();
		if(probability <=  rand){
			return blockexpdrop;
		}else{
			if(blockexpdrop == 0 ){
				return 1;
			}else{
				return (int)(blockexpdrop * 1.3);
			}
		}

	}

	public static short calcDurability(int enchantmentLevel) {
		double rand = Math.random();
		double probability = 1.0 / (enchantmentLevel + 1.0);
		if(probability <=  rand ){
			return 0;
		}
		return 1;
	}


}
