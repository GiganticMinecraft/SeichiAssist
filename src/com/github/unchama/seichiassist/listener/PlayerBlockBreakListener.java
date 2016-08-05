package com.github.unchama.seichiassist.listener;

import java.util.UUID;

import net.coreprotect.CoreProtectAPI;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
import org.bukkit.material.Dye;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;

public class PlayerBlockBreakListener implements Listener {
	Player player;
	String name;
	UUID uuid;
	PlayerData playerdata;


	//アクティブスキルの実行
	@EventHandler
	public void onPlayerActiveSkillEvent(BlockBreakEvent event){
		//実行したプレイヤーを取得
		player = event.getPlayer();
		//プレイヤー名を取得
		name = Util.getName(player);
		//UUIDを取得
		uuid = player.getUniqueId();

		//経験値変更用のクラスを設定
		ExperienceManager expman = new ExperienceManager(player);
		//もしサバイバルでなければ処理を終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return;
		}

		//デバッグ用
		if(SeichiAssist.DEBUG){
			player.sendMessage("ブロックブレイクイベントが呼び出されました");
		}

		//壊されるブロックを取得
		Block block = event.getBlock();
		Location centerofblock = block.getLocation().add(0.5, 0.5, 0.5);
		//ブロックのタイプを取得
		Material material = block.getType();
		//ブロックタイプがmateriallistに登録されていなければ処理終了
		if(!SeichiAssist.materiallist.contains(material)){
			return;
		}

		playerdata = SeichiAssist.playermap.get(uuid);
		//passiveskill[追加経験値獲得]処理実行
		expman.changeExp(calcExpDrop());

		//アクティブスキルフラグがオフの時処理を終了
		if(!playerdata.activemineflag){
			return;
		}
		//プレイヤーインベントリを取得
		PlayerInventory inventory = player.getInventory();
		//メインハンドとオフハンドを取得
		ItemStack mainhanditem = inventory.getItemInMainHand();
		ItemStack offhanditem = inventory.getItemInOffHand();
		//実際に使用するツールを格納する
		ItemStack tool = null;
		//メインハンドにツールがあるか
		boolean mainhandtoolflag = SeichiAssist.breakmateriallist.contains(mainhanditem.getType());
		//オフハンドにツールがあるか
		boolean offhandtoolflag = SeichiAssist.breakmateriallist.contains(offhanditem.getType());
		//壊されるブロックの取得
		Block breakblock = block.getRelative(0,1,0);
		//壊されるブロックの状態を取得
		BlockState blockstate = breakblock.getState();
		//壊されるブロックのデータを取得
		@SuppressWarnings("deprecation")
		byte data = blockstate.getData().getData();




		//デバッグ用
		if(SeichiAssist.DEBUG){
			player.sendMessage("blocktype"+block.getType().toString());
			player.sendMessage("breakblocktype"+breakblock.getType().toString());
		}

		//もし壊されるブロックがもともとのブロックと同じ種類だった場合アクティブスキル発動
		if(breakblock.getType().equals(material)|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))){
			//両手の時処理を終了
			if(mainhandtoolflag){
				//メインハンドの時
				tool = mainhanditem;
			}else if(offhandtoolflag){
				//サブハンドの時
				tool = offhanditem;
			}else{
				//どちらにももっていない時処理を終了
				return;
			}
			//アクティブスキルを発動するとき、プレイヤーの経験値レベルが０で経験値を１ももっていない場合処理を終了
			if(player.getLevel()==0 && !expman.hasExp(1)){
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
				}
				return;
			}
			//壊されるブロックがワールドガード範囲だった場合処理を終了
			if(!Util.getWorldGuard().canBuild(player, breakblock.getLocation())){
				player.sendMessage(ChatColor.RED + "ワールドガードで保護されています。");
				return;
			}
			//コアプロテクトのクラスを取得
			CoreProtectAPI CoreProtect = Util.getCoreProtect();
			//破壊ログを設定
			Boolean success = CoreProtect.logRemoval(player.getName(), breakblock.getLocation(), blockstate.getType(),data);
			//もし失敗したらプレイヤーに報告し処理を終了
			if(!success){
				player.sendMessage(ChatColor.RED + "coreprotectに保存できませんでした。管理者に報告してください。");
				return;
			}
			//アイテムをドロップさせる
			breakblock.getWorld().dropItemNaturally(centerofblock,dropItemOnTool(breakblock,tool));
			//ブロックを空気に変える
			breakblock.setType(Material.AIR);


			//あたかもプレイヤーが壊したかのようなエフェクトを表示させる、壊した時の音を再生させる
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND,breakblock.getType());
			//壊した時に白いエフェクトが出るように設定
			for(int i = 1; i<2 ; i++){
				breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.EXPLOSION, (byte)0);
			}
			// Effect.ENDER_SIGNALこれかっこいい
			// Effect.EXPLOSION 範囲でかい
			// Effect.WITCH_MAGIC 小さい 紫
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

			//アクティブスキル発動のために経験値消費
			expman.changeExp(-1);

			//ツールの耐久値を取得
			short d = tool.getDurability();
			//耐久力エンチャントに応じて耐久値を減らす
			tool.setDurability((short)(d + calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY))));
			//todo:幸運エンチャントに応じてドロップ量を設定

			//todo:シルクエンチャントに応じてドロップするアイテムを設定

			//プレイヤーの統計を１増やす
			player.incrementStatistic(Statistic.MINE_BLOCK, material);
		}
	}

	@SuppressWarnings("deprecation")
	private ItemStack dropItemOnTool(Block breakblock, ItemStack tool) {
		ItemStack dropitem = null;
		Material dropmaterial;
		Material breakmaterial = breakblock.getType();
		int fortunelevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        int bonus = (int) (Math.random() * ((fortunelevel + 2)) - 1);
        if (bonus <= 1) {
            bonus = 1;
        }
        if(SeichiAssist.DEBUG){
        	 Util.sendEveryMessage("bonus値:" + bonus);
        }

		int silktouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH);
		if(silktouch > 0){
			//シルクタッチの処理
			dropitem = new ItemStack(breakmaterial);
		}else if(fortunelevel > 0 && SeichiAssist.luckmateriallist.contains(breakmaterial)){
			//幸運の処理
			switch(breakmaterial){
				case COAL_ORE:
					dropmaterial = Material.COAL;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case DIAMOND_ORE:
					dropmaterial = Material.DIAMOND;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case LAPIS_ORE:
					Dye dye = new Dye();
					dye.setColor(DyeColor.BLUE);
					dropitem = dye.toItemStack(bonus);
					break;
				case EMERALD_ORE:
					dropmaterial = Material.EMERALD;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case QUARTZ_ORE:
					dropmaterial = Material.QUARTZ;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				default:
					break;
			}
		}else{
			//シルク幸運なしの処理
			switch(breakmaterial){
				case COAL_ORE:
					dropmaterial = Material.COAL;
					dropitem = new ItemStack(dropmaterial);
					break;
				case DIAMOND_ORE:
					dropmaterial = Material.DIAMOND;
					dropitem = new ItemStack(dropmaterial);
					break;
				case LAPIS_ORE:
					Dye dye = new Dye();
					dye.setColor(DyeColor.BLUE);
					dropitem = dye.toItemStack();
					break;
				case EMERALD_ORE:
					dropmaterial = Material.EMERALD;
					dropitem = new ItemStack(dropmaterial);
					break;
				case REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial);
					break;
				case QUARTZ_ORE:
					dropmaterial = Material.QUARTZ;
					dropitem = new ItemStack(dropmaterial);
					break;
				case STONE:
					//Material.STONEの処理
					if(breakblock.getData() == 0){
						//焼き石の処理
						dropmaterial = Material.COBBLESTONE;
						dropitem = new ItemStack(dropmaterial);
					}else{
						//他の石の処理
						dropitem = new ItemStack(breakmaterial);
					}
					break;
				default:
					//breakblcokのままのアイテムスタックを保存
					dropitem = new ItemStack(breakmaterial);
					break;
			}
		}
		return dropitem;
	}

	//追加経験値の設定
	private int calcExpDrop() {
		//０～１のランダムな値を取得
		double rand = Math.random();
		//もし追加経験値を獲得できるレベルまで達していない時は０を返す
		if(playerdata.level < SeichiAssist.config.getDropExplevel()){
			return 0;
		}else if (rand < 0.2){
			//２０％の確率で１を返す
			return 1;
		}//８０％の確率で０を返す
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
