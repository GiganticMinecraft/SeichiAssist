package com.github.unchama.seichiassist.listener;

import net.coreprotect.CoreProtectAPI;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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

import com.github.unchama.seichiassist.Config;
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
		//メインハンドかオフハンドか取得
		ItemStack mainhanditem = inventory.getItemInMainHand();
		ItemStack offhanditem = inventory.getItemInOffHand();
		ItemStack tool;
		if(SeichiAssist.breakmateriallist.contains(mainhanditem.getType())){
			tool = mainhanditem;
		}else if(SeichiAssist.breakmateriallist.contains(offhanditem.getType())){
			tool = offhanditem;
		}else{
			return;
		}


		Block block = event.getBlock();
		Material material = block.getType();
		if(!SeichiAssist.materiallist.contains(material)){
			return;
		}
		//壊されたブロックのみの処理
		int blockexpdrop = event.getExpToDrop();
		//パッシブスキル[dropexp]の処理
		event.setExpToDrop(calcExpDrop(blockexpdrop,playerdata));
		if(!playerdata.activemineflag){
			return;
		}
		if(player.getLevel()==0 && !expman.hasExp(3)){
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
			}
			return;
		}
		//以下アクティブスキルで壊されるブロックの処理
		CoreProtectAPI CoreProtect = Util.getCoreProtect();
		Block breakblock = block.getRelative(0,1,0);
		BlockState blockstate = breakblock.getState();
		byte data = blockstate.getData().getData();
		if(SeichiAssist.DEBUG){
			player.sendMessage("blocktype"+block.getType().toString());
			player.sendMessage("breakblocktype"+breakblock.getType().toString());
		}
		if(breakblock.getType().equals(material)|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))){
			if(!Util.getWorldGuard().canBuild(player, breakblock.getLocation())){
				player.sendMessage(ChatColor.RED + "ワールドガードで保護されています。");
				return;
			}
			Location breakloc = breakblock.getLocation();
			ExperienceOrb orb = breakloc.getWorld().spawn(breakloc, ExperienceOrb.class);
			breakblock.breakNaturally();
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND,breakblock.getType());
			for(int i = 1; i<3 ; i++){
				breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.EXPLOSION, (byte)0);
			}
			// Effect.ENDER_SIGNALこれかっこいい
			// Effect.EXPLOSION 範囲でかい
			// Effect.WITCH_MAGIC 小さい　紫
			// Effect.SPELL かわいい
			// Effect.WITHER_SHOOT 音だけ、結構うるさい
			// Effect.WITHER_BREAK_BLOCK これまた音だけ　うるせえ
			// Effect.COLOURED_DUST エフェクトちっちゃすぎ
			// Effect.LARGE_SMOKE EXPLOSIONの黒版
			// Effect.MOBSPAWNER_FLAMES 火の演出　すき
			// Effect.SMOKE　黒いすすを噴き出してる
			// Effect.HAPPY_VILLAGER 緑のパーティクル　けっこう長く残る
			// Effect.INSTANT_SPELL かなりいい白いパーティクル



			orb.setExperience(calcExpDrop(blockexpdrop,playerdata));
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

	public static int calcExpDrop(int blockexpdrop,PlayerData playerdata) {

		if(playerdata.level < Config.getDropExplevel()){
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
