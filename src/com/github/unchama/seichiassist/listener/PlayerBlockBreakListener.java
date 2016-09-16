package com.github.unchama.seichiassist.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
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
import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.CoolDownTaskRunnable;
import com.github.unchama.seichiassist.task.MultiBreakTaskRunnable;
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
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[blockbreaklistener処理]でエラー発生");
			plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return;
		}
		//スキルで破壊されるブロックの時処理を終了
		if(playerdata.activeskilldata.blocklist.contains(block)){
			event.setCancelled(true);
			if(SeichiAssist.DEBUG){
				player.sendMessage("スキルで使用中のブロックです。");
			}
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
		if(playerdata.activeskilldata.mineflagnum == 0){
			return;
		}

		//クールダウンタイム中は処理を終了
		if(!playerdata.activeskilldata.skillcanbreakflag){
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

		if(playerdata.activeskilldata.skilltype == ActiveSkill.ARROW.gettypenum()){

		}else if(playerdata.activeskilldata.skilltype == ActiveSkill.MULTI.gettypenum()){
			runMultiSkill(player, playerdata.activeskilldata.skillnum, block, tool, expman);
		}else if(playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum()){
			runBreakSkill(player, playerdata.activeskilldata.skillnum, block, tool, expman);
		}else if(playerdata.activeskilldata.skilltype == ActiveSkill.CONDENSE.gettypenum()){
			//runCondenSkill(player,playerdata.activeskilldata.skillnum, block, tool, expman);
		}
		/*
		//アクティブスキルを発動させる処理
		if(playerdata.activenum == ActiveSkill.DUALBREAK.getNum()){
			DualBreak(player,block,tool,expman);
		}else if(playerdata.activenum == ActiveSkill.TRIALBREAK.getNum()){
			TrialBreak(player,block,tool,expman);
		}else if(playerdata.activenum == ActiveSkill.EXPLOSION.getNum()){
			Explosion(player,block,tool,expman);
		}else if(playerdata.activenum == ActiveSkill.THUNDERSTORM.getNum()){
			new ThunderStormTaskRunnable(player, block,tool,expman).runTaskTimer(plugin,0,4);
		}else if(playerdata.activenum == ActiveSkill.BLIZZARD.getNum()){
			new BlizzardTaskRunnable(player, block,tool,expman).runTaskTimer(plugin,0,10);
		}else if(playerdata.activenum == ActiveSkill.METEO.getNum()){
			new MeteoTaskRunnable(player, block,tool,expman).runTaskTimer(plugin,10,10);
		}
		*/
	}
	//複数範囲破壊
	private void runMultiSkill(Player player, int skilllevel, Block block,
			ItemStack tool, ExperienceManager expman) {
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = playermap.get(uuid);
		//blocklistをリセット
		playerdata.activeskilldata.blocklist.clear();
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
		Coordinate start = new Coordinate();
		Coordinate end = new Coordinate();

		//エフェクト用に壊されるブロック全てのリストデータ
		List<List<Block>> multibreaklist = new ArrayList<List<Block>>();

		//壊される溶岩の全てのリストデータ
		List<List<Block>> multilavalist = new ArrayList<List<Block>>();

		//壊されるブロック範囲の全てのリストデータ
		List<Coordinate> startlist = new ArrayList<Coordinate>();
		List<Coordinate> endlist = new ArrayList<Coordinate>();

		//エフェクト用に壊されるブロック全てのリストデータ
		List<Block> breaklist = new ArrayList<Block>();

		//壊される溶岩のリストデータ
		List<Block> lavalist = new ArrayList<Block>();

		//繰り返す回数
		int breaknum = ActiveSkill.MULTI.getRepeatTimes(skilllevel);
		//一回の破壊の範囲
		Coordinate breaklength = ActiveSkill.MULTI.getBreakLength(skilllevel);


		//繰り返し回数だけ繰り返す
		for(int i = 1; i <= breaknum ; i++){
			switch (dir){
			case "N":
				start = new Coordinate(-((breaklength.x - 1)/2),-1,-((breaklength.z * i - 1)));
				end = new Coordinate(((breaklength.x - 1)/2),((breaklength.y - 1)-1),-((breaklength.z * (i - 1))));
				break;
			case "E"://xとzが逆
				start = new Coordinate((breaklength.z * (i - 1)),-1,-((breaklength.x - 1)/2));
				end = new Coordinate(((breaklength.z * i - 1)),((breaklength.y - 1)-1),((breaklength.x - 1)/2));
				break;
			case "S":
				start = new Coordinate(-((breaklength.x - 1)/2),-1,(breaklength.z * (i - 1)));
				end = new Coordinate(((breaklength.x - 1)/2),((breaklength.y - 1)-1),((breaklength.z * i - 1)));
				break;
			case "W"://xとzが逆
				start = new Coordinate(-((breaklength.z * i - 1)),-1,-((breaklength.x - 1)/2));
				end = new Coordinate(-((breaklength.z * (i - 1))),((breaklength.y - 1)-1),((breaklength.x - 1)/2));
				break;
			case "U":
				start = new Coordinate(-((breaklength.x - 1)/2),(breaklength.y * (i - 1)),-((breaklength.z - 1)/2));
				end = new Coordinate(((breaklength.x - 1)/2),((breaklength.y * i - 1)),((breaklength.z - 1)/2));
				break;
			case "D":
				start = new Coordinate(-((breaklength.x - 1)/2),-((breaklength.y * i - 1)),-((breaklength.z - 1)/2));
				end = new Coordinate(((breaklength.x - 1)/2),-((breaklength.y * (i - 1))),((breaklength.z - 1)/2));
				break;
			}


			for(int x = start.x ; x <= end.x ; x++){
				for(int z = start.z ; z <= end.z ; z++){
					for(int y = start.y; y <= end.y ; y++){
						if(x==0&&y==0&&z==0){
							continue;
						}
						breakblock = block.getRelative(x, y, z);
						//もし壊されるブロックがもともとのブロックと同じ種類だった場合
						if(breakblock.getType().equals(material)
								|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
								|| (block.getType().equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))
								|| (block.getType().equals(Material.GLOWING_REDSTONE_ORE)&&breakblock.getType().equals(Material.REDSTONE_ORE))
								|| (block.getType().equals(Material.REDSTONE_ORE)&&breakblock.getType().equals(Material.GLOWING_REDSTONE_ORE))
								|| breakblock.getType().equals(Material.LAVA)
								){
							if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking()){
								if(canBreak(player, breakblock)){
									if(breakblock.getType().equals(Material.LAVA)){
										lavalist.add(breakblock);
									}else{
										breaklist.add(breakblock);
									}
								}
							}
						}
					}
				}
			}

			//減る経験値計算

			//消費経験値の最大値は範囲破壊を行う回数で割る。
			//実際に破壊するブロック数  * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数
			double useExp = (double) (breaklist.size())
					* ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum)
					/((end.x - start.x + 1) * (end.z - start.z + 1) * (end.y - start.y + 1) * breaknum) ;


			//減る耐久値の計算
			short durability = (short) (tool.getDurability() + calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),breaklist.size()));
			//１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
			if(lavalist.size() == 1){
				durability += calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),10);
			}

			//実際に経験値を減らせるか判定
			if(!expman.hasExp(useExp)){
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
				}
				break;
			}
			//実際に耐久値を減らせるか判定
			if(tool.getType().getMaxDurability() <= durability && !tool.getItemMeta().spigot().isUnbreakable()){
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません");
				}
				break;
			}


			//経験値を減らす
			expman.changeExp(-useExp);

			//耐久値を減らす
			tool.setDurability(durability);

			//選択されたブロックを破壊せずに保存する処理
			multibreaklist.add(new ArrayList<Block>(breaklist));
			multilavalist.add(new ArrayList<Block>(lavalist));
			startlist.add(start);
			endlist.add(end);
			playerdata.activeskilldata.blocklist.addAll(breaklist);
		}


		//エフェクトが選択されていない時
		if(playerdata.activeskilldata.effectnum == 0){
			new MultiBreakTaskRunnable(player,block,tool,multibreaklist,multilavalist,startlist,endlist).runTaskTimer(plugin,0,4);
		}
		//エフェクトが選択されているとき
		else{
			ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
			skilleffect[playerdata.activeskilldata.effectnum - 1].runMultiEffect(multibreaklist, startlist, endlist);
		}
	}

	//範囲破壊実行処理
	private void runBreakSkill(Player player,int skillnum,Block block,ItemStack tool,ExperienceManager expman) {
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
		Coordinate start = new Coordinate();
		Coordinate end = new Coordinate();

		//エフェクト用に壊されるブロック全てのリストデータ
		List<Block> breaklist = new ArrayList<Block>();

		//壊される溶岩のリストデータ
		List<Block> lavalist = new ArrayList<Block>();

		switch (dir){
		case "N":
			//北を向いているとき
			if(skillnum < 3){
				if(playerdata.activeskilldata.mineflagnum == 1){
					start = new Coordinate(-(skillnum-1),0,0);
					end = new Coordinate(skillnum-1,1,0);
				}else if(playerdata.activeskilldata.mineflagnum == 2){
					start = new Coordinate(-(skillnum-1),-1,0);
					end = new Coordinate(skillnum-1,0,0);
				}

			}else if(skillnum == 3){
				start = new Coordinate(-(skillnum-2),-(skillnum-2),-(skillnum-2)-1);
				end = new Coordinate(skillnum-2,skillnum-2,(skillnum-2)-1);
			}else{
				start = new Coordinate(-(skillnum-2),-1,-(skillnum-2)-1);
				end = new Coordinate(skillnum-2,(skillnum-4)*2 + 1,(skillnum-2)-1);
			}
			break;
		case "E":
			//東を向いているとき
			if(skillnum < 3){
				if(playerdata.activeskilldata.mineflagnum == 1){
					start = new Coordinate(0,0,-(skillnum-1));
					end = new Coordinate(0,1,skillnum-1);
				}else if(playerdata.activeskilldata.mineflagnum == 2){
					start = new Coordinate(0,-1,-(skillnum-1));
					end = new Coordinate(0,0,skillnum-1);
				}

			}else if(skillnum == 3){
				start = new Coordinate(-(skillnum-2)+1,-(skillnum-2),-(skillnum-2));
				end = new Coordinate((skillnum-2)+1,skillnum-2,(skillnum-2));
			}else{
				start = new Coordinate(-(skillnum-2)+1,-1,-(skillnum-2));
				end = new Coordinate((skillnum-2)+1,(skillnum-4)*2 + 1,(skillnum-2));
			}
			break;
		case "S":
			//南を向いているとき
			if(skillnum < 3){
				if(playerdata.activeskilldata.mineflagnum == 1){
					start = new Coordinate(-(skillnum-1),0,0);
					end = new Coordinate(skillnum-1,1,0);
				}else if(playerdata.activeskilldata.mineflagnum == 2){
					start = new Coordinate(-(skillnum-1),-1,0);
					end = new Coordinate(skillnum-1,0,0);
				}

			}else if(skillnum == 3){
				start = new Coordinate(-(skillnum-2),-(skillnum-2),-(skillnum-2)+1);
				end = new Coordinate(skillnum-2,skillnum-2,(skillnum-2)+1);
			}else{
				start = new Coordinate(-(skillnum-2),-1,-(skillnum-2)+1);
				end = new Coordinate(skillnum-2,(skillnum-4)*2 + 1,(skillnum-2)+1);
			}
			break;
		case "W":
			//西を向いているとき
			if(skillnum < 3){
				if(playerdata.activeskilldata.mineflagnum == 1){
					start = new Coordinate(0,0,-(skillnum-1));
					end = new Coordinate(0,1,skillnum-1);
				}else if(playerdata.activeskilldata.mineflagnum == 2){
					start = new Coordinate(0,-1,-(skillnum-1));
					end = new Coordinate(0,0,skillnum-1);
				}

			}else if(skillnum == 3){
				start = new Coordinate(-(skillnum-2)-1,-(skillnum-2),-(skillnum-2));
				end = new Coordinate((skillnum-2)-1,skillnum-2,(skillnum-2));
			}else{
				start = new Coordinate(-(skillnum-2)-1,-1,-(skillnum-2));
				end = new Coordinate((skillnum-2)-1,(skillnum-4)*2 + 1,(skillnum-2));
			}
			break;
		case "U":
			//上を向いているとき
			if(skillnum < 3){
			}else if(skillnum == 3){
				start = new Coordinate(-(skillnum-2),0,-(skillnum-2));
				end = new Coordinate((skillnum-2),2,(skillnum-2));
			}else{
				start = new Coordinate(-(skillnum-2),0,-(skillnum-2));
				end = new Coordinate((skillnum-2),(skillnum-3)*2,(skillnum-2));
			}
			break;
		case "D":
			//下を向いているとき
			if(skillnum < 3){
			}else if(skillnum == 3){
				start = new Coordinate(-(skillnum-2),-2,-(skillnum-2));
				end = new Coordinate((skillnum-2),0,(skillnum-2));
			}else{
				start = new Coordinate(-(skillnum-2),-(skillnum-3)*2,-(skillnum-2));
				end = new Coordinate((skillnum-2),0,(skillnum-2));
			}
			break;
		}

		for(int x = start.x ; x <= end.x ; x++){
			for(int z = start.z ; z <= end.z ; z++){
				for(int y = start.y; y <= end.y ; y++){
					if(x==0&&y==0&&z==0){
						continue;
					}
					breakblock = block.getRelative(x, y, z);
					//もし壊されるブロックがもともとのブロックと同じ種類だった場合
					if(breakblock.getType().equals(material)
							|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
							|| (block.getType().equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))
							|| (block.getType().equals(Material.GLOWING_REDSTONE_ORE)&&breakblock.getType().equals(Material.REDSTONE_ORE))
							|| (block.getType().equals(Material.REDSTONE_ORE)&&breakblock.getType().equals(Material.GLOWING_REDSTONE_ORE))
							|| breakblock.getType().equals(Material.LAVA)
							){
						if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking()){
							if(canBreak(player, breakblock)){
								if(breakblock.getType().equals(Material.LAVA)){
									lavalist.add(breakblock);
								}else{
									breaklist.add(breakblock);
								}
							}
						}
					}
				}
			}
		}

		//減る経験値計算

		//実際に破壊するブロック数  * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数
		double useExp = (double) (breaklist.size())
				* ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum)
				/((end.x - start.x + 1) * (end.z - start.z + 1) * (end.y - start.y + 1)) ;
		if(SeichiAssist.DEBUG){
			player.sendMessage(ChatColor.RED + "必要経験値：" + ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum));
			player.sendMessage(ChatColor.RED + "全ての破壊数：" + ((end.x - start.x + 1) * (end.z - start.z + 1) * (end.y - start.y + 1)));
			player.sendMessage(ChatColor.RED + "実際の破壊数：" + breaklist.size());
			player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値：" + useExp);
		}
		//減る耐久値の計算
		short durability = (short) (tool.getDurability() + calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),breaklist.size()));
		//１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
		if(lavalist.size() == 1){
			durability += calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),10);
		}


		//実際に経験値を減らせるか判定
		if(!expman.hasExp(useExp)){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
			}
			return;
		}
		player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値:" + durability);
		//実際に耐久値を減らせるか判定
		if(tool.getType().getMaxDurability() <= durability && !tool.getItemMeta().spigot().isUnbreakable()){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません");
			}
			return;
		}


		//経験値を減らす
		expman.changeExp(-useExp);

		//耐久値を減らす
		tool.setDurability(durability);


		//以降破壊する処理

		playerdata.activeskilldata.blocklist = breaklist;

		//１マスの溶岩のみ破壊する処理
		if(lavalist.size() == 1){
			lavalist.get(0).setType(Material.AIR);
		}

		//選択されたブロックを破壊する処理

		//エフェクトが指定されていないときの処理
		if(playerdata.activeskilldata.effectnum == 0){
			for(Block b:breaklist){
				BreakBlock(player, b, centerofblock, tool,true);
			}
			playerdata.activeskilldata.blocklist.clear();
		}
		//エフェクトが指定されているときの処理
		else{
			ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
			skilleffect[playerdata.activeskilldata.effectnum - 1].runBreakEffect(breaklist, start, end);
		}

		//クールダウンを発生させる
		if(playerdata.activeskilldata.skillnum > 3 && breaklist.size() > 0){
			new CoolDownTaskRunnable(player).runTaskLater(plugin,ActiveSkill.BREAK.getCoolDown(playerdata.activeskilldata.skillnum));
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
	//ブロックを破壊する処理、ドロップも含む、統計増加も含む
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
					if(breakblock.getData() == 0x00){
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
	//num回だけ耐久を減らす処理
	public static short calcDurability(int enchantmentLevel,int num) {
		Random rand = new Random();
		short durability = 0;
		double probability = 1.0 / (enchantmentLevel + 1.0);

		for(int i = 0; i < num ; i++){
			if(probability >  rand.nextDouble() ){
				durability++;
			}
		}
		return durability;
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
