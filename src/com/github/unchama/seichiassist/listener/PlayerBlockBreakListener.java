package com.github.unchama.seichiassist.listener;

import net.coreprotect.CoreProtectAPI;

import org.bukkit.ChatColor;
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
		if(SeichiAssist.DEBUG){
			player.sendMessage("ブロックブレイクイベントが呼び出されました");
		}

		Block block = event.getBlock();
		Material material = block.getType();
		if(!SeichiAssist.materiallist.contains(material)){
			return;
		}
		//壊されたブロックのみの処理
		expman.changeExp(calcExpDrop(playerdata));
		//パッシブスキル[dropexp]の処理
		if(!playerdata.activemineflag){
			return;
		}
		if(player.getLevel()==0 && !expman.hasExp(1)){
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
			breakblock.breakNaturally();
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND,breakblock.getType());
			for(int i = 1; i<2 ; i++){
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
			//expman.changeExp(calcExpDrop(playerdata));
			//orb.setExperience(calcExpDrop(blockexpdrop,playerdata));
			expman.changeExp(-1);

			short d = tool.getDurability();
			tool.setDurability((short)(d + calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY))));
			player.incrementStatistic(Statistic.MINE_BLOCK, material);
			Boolean success = CoreProtect.logRemoval(player.getName(), breakblock.getLocation(), blockstate.getType(),data);
			if(!success){
				player.sendMessage("coreprotectに保存できませんでした。");
			}
		}
	}

	public static int calcExpDrop(PlayerData playerdata) {
		double rand = Math.random();
		if(playerdata.level < SeichiAssist.config.getDropExplevel()){
			return 0;
		}else if (rand < 0.2){
			return 1;
		}
		return 0;
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
