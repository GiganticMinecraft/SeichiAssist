package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import net.coreprotect.CoreProtectAPI;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.BlizzardTaskRunnable;
import com.github.unchama.seichiassist.task.MeteoTaskRunnable;
import com.github.unchama.seichiassist.task.ThunderStormTaskRunnable;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;

public class PlayerBlockBreakListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private SeichiAssist plugin = SeichiAssist.plugin;
	//アクティブスキルの実行
	@EventHandler
	public void onPlayerActiveSkillEvent(BlockBreakEvent event){
		//実行したプレイヤーを取得
		Player player = event.getPlayer();
		//もしサバイバルでなければ処理を終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return;
		}

		//壊されるブロックを取得
		Block block = event.getBlock();
		//他人の保護がかかっている場合は処理を終了
		if(!Util.getWorldGuard().canBuild(player, block.getLocation())){
			return;
		}
		//ブロックのタイプを取得
		Material material = block.getType();
		//ブロックタイプがmateriallistに登録されていなければ処理終了
		if(!SeichiAssist.materiallist.contains(material)){
			return;
		}

		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//UUIDを基にプレイヤーデータ取得
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//特定スキル発動中は処理を終了
		if(playerdata.skillflag){
			//ブロック破壊もさせない
			event.setCancelled(true);
			return;
		}

		//デバッグ用
		if(SeichiAssist.DEBUG){
			player.sendMessage("ブロックブレイクイベントが呼び出されました");
		}

		//これ以前の終了処理はパッシブの追加経験値はもらえません
		//経験値変更用のクラスを設定
		ExperienceManager expman = new ExperienceManager(player);
		//passiveskill[追加経験値獲得]処理実行
		int exp = calcExpDrop(playerdata);
		expman.changeExp(exp);
		//これ以降の終了処理は経験値はもらえます

		//アクティブスキルフラグがオフの時処理を終了
		if(playerdata.activemineflagnum == 0){
			return;
		}

		//クールダウンタイム中は処理を終了
		if(!playerdata.skillcanbreakflag){
			//SEを再生
			player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, (float)0.5, 1);
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

		//場合分け
		if(mainhandtoolflag){
			//メインハンドの時
			tool = mainhanditem;
		}else if(offhandtoolflag){
			//サブハンドの時
			return;
		}else{
			//どちらにももっていない時処理を終了
			return;
		}

		//耐久値がマイナスかつ耐久無限ツールでない時処理を終了
		if(tool.getDurability() > tool.getType().getMaxDurability() && !tool.getItemMeta().spigot().isUnbreakable()){
			return;
		}


		//アクティブスキルを発動させる処理
		if(playerdata.activenum == ActiveSkill.DUALBREAK.getNum()){
			DualBreak(player,block,tool,expman);
		}else if(playerdata.activenum == ActiveSkill.TRIALBREAK.getNum()){
			TrialBreak(player,block,tool,expman);
		}else if(playerdata.activenum == ActiveSkill.EXPLOSION.getNum()){
			Explosion(player,block,tool,expman);
		}else if(playerdata.activenum == ActiveSkill.THUNDERSTORM.getNum()){
			new ThunderStormTaskRunnable(player, block,tool,expman).runTaskTimer(plugin,0,7);
		}else if(playerdata.activenum == ActiveSkill.BLIZZARD.getNum()){
			new BlizzardTaskRunnable(player, block,tool,expman).runTaskTimer(plugin,0,10);
		}else if(playerdata.activenum == ActiveSkill.METEO.getNum()){
			new MeteoTaskRunnable(player, block,tool,expman).runTaskTimer(plugin,10,10);
		}
	}


	//スキル「エクスプロージョン」
	private void Explosion(Player player,Block block,ItemStack tool,ExperienceManager expman) {
		//プレイヤーの足のy座標を取得
		int playerlocy = player.getLocation().getBlockY() - 1 ;
		//プレイヤーの向いている方角を取得
		String dir = getCardinalDirection(player);
		//元ブロックのマテリアルを取得
		Material material = block.getType();
		//元ブロックの真ん中の位置を取得
		Location centerofblock = block.getLocation().add(0.5, 0.5, 0.5);

		//壊されるブロックの宣言
		Block breakblock;
		int startx = 0;
		int starty = -1;
		int startz = 0;
		int endx = 0;
		int endy = +1;
		int endz = 0;
		Location explosionloc = null;

		switch (dir){
			case "N":
				//北を向いているとき
				startx = -1;
				startz = -2;
				endx = 1;
				endz = 0;
				explosionloc = centerofblock.add(0, 0, -1);
				break;
			case "E":
				//東を向いているとき
				startx = 0;
				startz = -1;
				endx = 2;
				endz = 1;
				explosionloc = centerofblock.add(1, 0, 0);
				break;
			case "S":
				//南を向いているとき
				startx = -1;
				startz = 0;
				endx = 1;
				endz = 2;
				explosionloc = centerofblock.add(0, 0, 1);
				break;
			case "W":
				//西を向いているとき
				startx = -2;
				startz = -1;
				endx = 0;
				endz = 1;
				explosionloc = centerofblock.add(-1, 0, 0);
				break;
			case "U":
				//上を向いているとき
				startx = -1;
				starty = 0;
				startz = -1;
				endx = 1;
				endy = 2;
				endz = 1;
				explosionloc = centerofblock.add(0, 1, 0);
				break;
			case "D":
				//下を向いているとき
				startx = -1;
				starty = -2;
				startz = -1;
				endx = 1;
				endy = 0;
				endz = 1;
				explosionloc = centerofblock.add(0, -1, 0);
				break;
		}

		if(player.getLevel() == 0 && !expman.hasExp(15)){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
			}
			return;
		}

		int count = 0;
		for(int x = startx ; x <= endx ; x++){
			for(int z = startz ; z <= endz ; z++){
				for(int y = starty; y <= endy ; y++){
					if(x==0&&y==0&&z==0){
						continue;
					}
					breakblock = block.getRelative(x, y, z);
					//もし壊されるブロックがもともとのブロックと同じ種類だった場合
					if(breakblock.getType().equals(material)
							|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
							|| (block.getType().equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))
							|| breakblock.getType().equals(Material.LAVA)
							){
						if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking()){
							if(canBreak(player, breakblock)){
								if(breakblock.getType().equals(Material.LAVA)){
									breakblock.setType(Material.AIR);
								}else{
									//アクティブスキル発動
									BreakBlock(player, breakblock, centerofblock, tool,false);
									count ++;
								}
							}
						}
					}
				}

			}
		}
		if(count>0){
			block.getWorld().createExplosion(explosionloc, 0, false);
		}

		if(count>22){
			expman.changeExp(-15);
		}else if(count>17){
			expman.changeExp(-12);
		}else if(count>12){
			expman.changeExp(-9);
		}else if(count>7){
			expman.changeExp(-6);
		}else if(count>2){
			expman.changeExp(-3);
		}else if(count>0){
		}
	}

	private void TrialBreak(Player player,Block block,ItemStack tool,ExperienceManager expman) {
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = playermap.get(uuid);
		//プレイヤーの足のy座標を取得
		int playerlocy = player.getLocation().getBlockY() - 1 ;
		//プレイヤーの向いている方角を取得
		String dir = getCardinalDirection(player);
		//元ブロックのマテリアルを取得
		Material material = block.getType();
		//元ブロックの真ん中の位置を取得
		Location centerofblock = block.getLocation().add(0.5, 0.5, 0.5);

		//壊されるブロックの宣言
		Block breakblock;
		int startx = 0;
		int starty = playerdata.activemineflagnum == 1 ? 1:-1;
		int startz = 0;
		int endx = 0;
		int endy = 0;
		int endz = 0;

		switch (dir){
			case "N":
				//北を向いているとき
				startx = -1;
				startz = 0;
				endx = 1;
				endz = 0;
				break;
			case "E":
				//東を向いているとき
				startx = 0;
				startz = -1;
				endx = 0;
				endz = 1;
				break;
			case "S":
				//南を向いているとき
				startx = -1;
				startz = 0;
				endx = 1;
				endz = 0;
				break;
			case "W":
				//西を向いているとき
				startx = 0;
				startz = -1;
				endx = 0;
				endz = 1;
				break;
		}

		if(player.getLevel() == 0 && !expman.hasExp(3)){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
			}
			return;
		}

		int count = 0;
		for(int x = startx ; x <= endx ; x++){
			for(int z = startz ; z <= endz ; z++){
				//startyの処理
				breakblock = block.getRelative(x, starty, z);

				//もし壊されるブロックがもともとのブロックと同じ種類だった場合アクティブスキル発動
				if(breakblock.getType().equals(material)
						|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
						|| (block.getType().equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))){

					if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking()){
						if(canBreak(player, breakblock)){
							//アクティブスキル発動
							BreakBlock(player, breakblock, centerofblock, tool,true);
							//壊した時に白いエフェクトが出るように設定
							for(int i = 1; i<3 ; i++){
								breakblock.getWorld().playEffect(breakblock.getLocation().add(0.5,0.5,0.5), Effect.INSTANT_SPELL, (byte)0);
							}
							count ++;
						}
					}

				}
				//endyの処理
				if(x==0&&z==0){
					continue;
				}

				breakblock = block.getRelative(x, endy, z);
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage("blocktype"+block.getType().toString());
					player.sendMessage("breakblocktype"+breakblock.getType().toString());
				}

				//もし壊されるブロックがもともとのブロックと同じ種類だった場合アクティブスキル発動
				if(breakblock.getType().equals(material)
						|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
						|| (block.getType().equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))){

					if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking()){
						if(canBreak(player, breakblock)){
							//アクティブスキル発動
							BreakBlock(player, breakblock, centerofblock, tool,true);
							//壊した時に黒いエフェクトが出るように設定
							for(int i = 1; i<4 ; i++){
								breakblock.getWorld().playEffect(breakblock.getLocation().add(0.5,0.5,0.5), Effect.INSTANT_SPELL, (byte)0);
							}
							count ++;
						}
					}

				}
			}
		}
		if(count>3){
			expman.changeExp(-3);
		}else if(count>0){
			expman.changeExp(-2);

		}
	}

	private void DualBreak(Player player,Block block,ItemStack tool,ExperienceManager expman) {
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = playermap.get(uuid);
		//プレイヤーの足元のy座標を取得
		int playerlocy = player.getLocation().getBlockY() - 1 ;

		//元ブロックのマテリアルを取得
		Material material = block.getType();
		//元ブロックの真ん中の位置を取得
		Location centerofblock = block.getLocation().add(0.5, 0.5, 0.5);
		//壊されるブロックの取得
		Block breakblock = null;
		if(playerdata.activemineflagnum == 1){
			breakblock = block.getRelative(0,1,0);
		}else if(playerdata.activemineflagnum == 2){
			breakblock = block.getRelative(0,-1,0);
		}else {
			return;
		}

		//デバッグ用
		if(SeichiAssist.DEBUG){
			player.sendMessage("blocktype"+block.getType().toString());
			player.sendMessage("breakblocktype"+breakblock.getType().toString());
		}

		//もし壊されるブロックがもともとのブロックと同じ種類だった場合アクティブスキル発動
		if(breakblock.getType().equals(material)
				|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
				|| (block.getType().equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))){
			//アクティブスキルを発動するとき、プレイヤーの経験値レベルが０で経験値を１ももっていない場合処理を終了
			if(player.getLevel()==0 && !expman.hasExp(1)){
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
				}
				return;
			}

			if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking()){
				if(canBreak(player, breakblock)){
					//アクティブスキル発動
					BreakBlock(player, breakblock, centerofblock, tool,true);
					//壊した時に白いエフェクトが出るように設定
					for(int i = 1; i<2 ; i++){
						breakblock.getWorld().playEffect(breakblock.getLocation().add(0.5,0.5,0.5), Effect.EXPLOSION, (byte)0);
					}
					//アクティブスキル発動のために経験値消費
					expman.changeExp(-1);
				}
			}
		}
	}
	//他のプラグインの影響があってもブロックを破壊できるのか
	public static boolean canBreak(Player player ,Block breakblock) {
		//壊されるブロックの状態を取得
		BlockState blockstate = breakblock.getState();
		//壊されるブロックのデータを取得
		@SuppressWarnings("deprecation")
		byte data = blockstate.getData().getData();


		//壊されるブロックがワールドガード範囲だった場合処理を終了
		if(!Util.getWorldGuard().canBuild(player, breakblock.getLocation())){
			player.sendMessage(ChatColor.RED + "ワールドガードで保護されています。");
			return false;
		}
		//コアプロテクトのクラスを取得
		CoreProtectAPI CoreProtect = Util.getCoreProtect();
		//破壊ログを設定
		Boolean success = CoreProtect.logRemoval(player.getName(), breakblock.getLocation(), blockstate.getType(),data);
		//もし失敗したらプレイヤーに報告し処理を終了
		if(!success){
			player.sendMessage(ChatColor.RED + "coreprotectに保存できませんでした。管理者に報告してください。");
			return false;
		}
		return true;
	}

	public static void BreakBlock(Player player,Block breakblock,Location centerofblock,ItemStack tool,Boolean stepflag) {

		Material material = breakblock.getType();


		//アイテムをドロップさせる
		breakblock.getWorld().dropItemNaturally(centerofblock,dropItemOnTool(breakblock,tool));

		//ブロックを空気に変える
		breakblock.setType(Material.AIR);

		if(stepflag){
			//あたかもプレイヤーが壊したかのようなエフェクトを表示させる、壊した時の音を再生させる
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND,material);
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

		//ツールの耐久値を取得
		short d = tool.getDurability();
		//耐久力エンチャントに応じて耐久値を減らす
		tool.setDurability((short)(d + calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY))));

		//プレイヤーの統計を１増やす
		player.incrementStatistic(Statistic.MINE_BLOCK, material);

	}

	@SuppressWarnings("deprecation")
	public static ItemStack dropItemOnTool(Block breakblock, ItemStack tool) {
		ItemStack dropitem = null;
		Material dropmaterial;
		Material breakmaterial = breakblock.getType();
		int fortunelevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        int bonus = (int) (Math.random() * ((fortunelevel + 2)) - 1);
        if (bonus <= 1) {
            bonus = 1;
        }
        byte b = breakblock.getData();
        b &= 0x03;


		int silktouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH);
		if(silktouch > 0){
			//シルクタッチの処理
			dropitem = new ItemStack(breakmaterial,1,b);
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
				case GRAVEL:
					dropmaterial = Material.FLINT;
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
						dropitem = new ItemStack(breakmaterial,1,b);
					}
					break;
				case GRASS:
					//芝生の処理
					dropmaterial = Material.DIRT;
					dropitem = new ItemStack(dropmaterial);
					break;
				default:
					//breakblcokのままのアイテムスタックを保存
					dropitem = new ItemStack(breakmaterial,1,b);
					break;
			}
		}
		return dropitem;
	}

	//追加経験値の設定
	public static int calcExpDrop(PlayerData playerdata) {
		//０～１のランダムな値を取得
		double rand = Math.random();
		//10%の確率で経験値付与
		if(rand < 0.1){
			//Lv8未満は獲得経験値ゼロ、それ以上はレベルに応じて経験値付与
			if(playerdata.level < 8){
				return 0;
			}else if (playerdata.level < 18){
				return SeichiAssist.config.getDropExplevel(1);
			}else if (playerdata.level < 28){
				return SeichiAssist.config.getDropExplevel(2);
			}else if (playerdata.level < 38){
				return SeichiAssist.config.getDropExplevel(3);
			}else if (playerdata.level < 48){
				return SeichiAssist.config.getDropExplevel(4);
			}else if (playerdata.level < 58){
				return SeichiAssist.config.getDropExplevel(5);
			}else if (playerdata.level < 68){
				return SeichiAssist.config.getDropExplevel(6);
			}else if (playerdata.level < 78){
				return SeichiAssist.config.getDropExplevel(7);
			}else if (playerdata.level < 88){
				return SeichiAssist.config.getDropExplevel(8);
			}else if (playerdata.level < 98){
				return SeichiAssist.config.getDropExplevel(9);
			}else{
				return SeichiAssist.config.getDropExplevel(10);
			}
		}else{
			return 0;
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

	public static String getCardinalDirection(Player player) {
		double rotation = (player.getLocation().getYaw() + 180) % 360;
		Location loc = player.getLocation();
		float pitch = loc.getPitch();
		if (rotation < 0) {
		rotation += 360.0;
		}

		if(pitch <= -30){
			return "U";
		}else if(pitch >= 25){
			return "D";
		}else if (0 <= rotation && rotation < 45.0) {
			return "N";
		}else if (45.0 <= rotation && rotation < 135.0) {
			return "E";
		}else if (135.0 <= rotation && rotation < 225.0) {
			return "S";
		}else if (225.0 <= rotation && rotation < 315.0) {
			return "W";
		}else if (315.0 <= rotation && rotation < 360.0) {
		return "N";
		} else {
		return null;
		}
	}
}
