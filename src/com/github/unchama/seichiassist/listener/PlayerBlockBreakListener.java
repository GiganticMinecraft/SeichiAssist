package com.github.unchama.seichiassist.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

		//デバッグ用
		if(SeichiAssist.DEBUG){
			player.sendMessage("ブロックブレイクイベントが呼び出されました");
		}




		//壊されるブロックを取得
		Block block = event.getBlock();
		//他人の保護がかかっている場合は処理を終了
		if(!Util.getWorldGuard().canBuild(player, block.getLocation())){
			return;
		}
		//ブロックのタイプを取得
		Material material = block.getType();



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
		//もしサバイバルでなければ処理を終了
		//もしフライ中なら終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL) || player.isFlying()){
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

		//クールダウンタイム中は処理を終了
		if(!playerdata.activeskilldata.skillcanbreakflag){
			//SEを再生
			player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, (float)0.5, 1);
			return;
		}

		//これ以前の終了処理はパッシブの追加経験値はもらえません
		//経験値変更用のクラスを設定
		ExperienceManager expman = new ExperienceManager(player);
		//passiveskill[追加経験値獲得]処理実行
		int exp = Util.calcExpDrop(playerdata);
		expman.changeExp(exp);


		//これ以降の終了処理は経験値はもらえます
		//ブロックタイプがmateriallistに登録されていなければ処理終了
		if(!SeichiAssist.materiallist.contains(material)){
			return;
		}

		//アクティブスキルフラグがオフの時処理を終了
		if(playerdata.activeskilldata.mineflagnum == 0 || playerdata.activeskilldata.skillnum == 0){
			return;
		}


		if(playerdata.activeskilldata.skilltype == ActiveSkill.MULTI.gettypenum()){
			runMultiSkill(player, playerdata.activeskilldata.skillnum, block, tool, expman);
			event.setCancelled(true);
		}else if(playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum()){
			runBreakSkill(player, playerdata.activeskilldata.skillnum, block, tool, expman);
			event.setCancelled(true);
		}


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
		String dir = Util.getCardinalDirection(player);
		//元ブロックのマテリアルを取得
		Material material = block.getType();
		//元ブロックの真ん中の位置を取得
		Location centerofblock = block.getLocation().add(0.5, 0.5, 0.5);



		//壊されるブロックの宣言
		Block breakblock;
		//実際に破壊するブロック数
		long breakblocknum = 0;
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
			breaklist.clear();
			lavalist.clear();
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
						breakblock = block.getRelative(x, y, z);
						//もし壊されるブロックがもともとのブロックと同じ種類だった場合
						if(breakblock.getType().equals(material)
								|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
								|| (block.getType().equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))
								|| (block.getType().equals(Material.GLOWING_REDSTONE_ORE)&&breakblock.getType().equals(Material.REDSTONE_ORE))
								|| (block.getType().equals(Material.REDSTONE_ORE)&&breakblock.getType().equals(Material.GLOWING_REDSTONE_ORE))
								|| breakblock.getType().equals(Material.STATIONARY_LAVA)
								){
							if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking() || breakblock.equals(block)){
								if(Util.canBreak(player, breakblock)){
									if(breakblock.getType().equals(Material.STATIONARY_LAVA)){
										lavalist.add(breakblock);
									}else{
										breaklist.add(breakblock);
										playerdata.activeskilldata.blocklist.add(breakblock);
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
			short durability = (short) (tool.getDurability() + Util.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),breaklist.size()));
			//１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
			durability += Util.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),10*lavalist.size());

			//実際に経験値を減らせるか判定
			if(!expman.hasExp(useExp)){
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
				}

				playerdata.activeskilldata.blocklist.removeAll(breaklist);

				break;
			}
			//実際に耐久値を減らせるか判定
			if(tool.getType().getMaxDurability() <= durability && !tool.getItemMeta().spigot().isUnbreakable()){
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません");
				}
				playerdata.activeskilldata.blocklist.removeAll(breaklist);

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
			breakblocknum += (long)breaklist.size();
		}


		//壊したブロック数に応じてクールダウンを発生させる
		long cooldown = (long) ActiveSkill.MULTI.getCoolDown(playerdata.activeskilldata.skillnum) * breakblocknum /((end.x - start.x + 1) * (end.z - start.z + 1) * (end.y - start.y + 1) * breaknum);
		if(cooldown >= 5){
			new CoolDownTaskRunnable(player,false,true).runTaskLater(plugin,cooldown);
		}else{
			new CoolDownTaskRunnable(player,false,false).runTaskLater(plugin,cooldown);
		}

		//自身のみしか壊さない時自然に処理する
		if(breakblocknum==1){
			Util.BreakBlock(player, block, centerofblock, tool,false);
			playerdata.activeskilldata.blocklist.remove(block);
		}//エフェクトが指定されていないときの処理
		else if(playerdata.activeskilldata.effectnum == 0){
			new MultiBreakTaskRunnable(player,block,tool,multibreaklist,multilavalist,startlist,endlist).runTaskTimer(plugin,0,4);
		}
		//エフェクトが選択されているとき
		else{
			ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
			skilleffect[playerdata.activeskilldata.effectnum - 1].runMultiEffect(player,playerdata,tool,multibreaklist, startlist, endlist,centerofblock);
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
		String dir = Util.getCardinalDirection(player);
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
				start = new Coordinate(-(skillnum-2),-1,-(skillnum-2)*2);
				end = new Coordinate(skillnum-2,(skillnum-4)*2 + 1,0);
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
				start = new Coordinate(0,-1,-(skillnum-2));
				end = new Coordinate((skillnum-2)*2,(skillnum-4)*2 + 1,(skillnum-2));
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
				start = new Coordinate(-(skillnum-2),-1,0);
				end = new Coordinate(skillnum-2,(skillnum-4)*2 + 1,(skillnum-2)*2);
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
				start = new Coordinate(-(skillnum-2)*2,-1,-(skillnum-2));
				end = new Coordinate(0,(skillnum-4)*2 + 1,(skillnum-2));
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
					breakblock = block.getRelative(x, y, z);
					//もし壊されるブロックがもともとのブロックと同じ種類だった場合
					if(breakblock.getType().equals(material)
							|| (block.getType().equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
							|| (block.getType().equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))
							|| (block.getType().equals(Material.GLOWING_REDSTONE_ORE)&&breakblock.getType().equals(Material.REDSTONE_ORE))
							|| (block.getType().equals(Material.REDSTONE_ORE)&&breakblock.getType().equals(Material.GLOWING_REDSTONE_ORE))
							|| breakblock.getType().equals(Material.STATIONARY_LAVA)
							){
						if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking() || breakblock.equals(block)){
							if(Util.canBreak(player, breakblock)){
								if(breakblock.getType().equals(Material.STATIONARY_LAVA)){
									lavalist.add(breakblock);
								}else{
									breaklist.add(breakblock);
									playerdata.activeskilldata.blocklist.add(breakblock);
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
		short durability = (short) (tool.getDurability() + Util.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),breaklist.size()));
		//１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
		durability += Util.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),10 * lavalist.size());



		//実際に経験値を減らせるか判定
		if(!expman.hasExp(useExp)){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
			}

			return;
		}
		if(SeichiAssist.DEBUG){
			player.sendMessage(ChatColor.RED + "アクティブスキル発動後のツールの耐久値:" + durability);
		}

		//実際に耐久値を減らせるか判定
		if(tool.getType().getMaxDurability() <= durability && !tool.getItemMeta().spigot().isUnbreakable()){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません");
			}
			playerdata.activeskilldata.blocklist.removeAll(breaklist);
			return;
		}


		//経験値を減らす
		expman.changeExp(-useExp);

		//耐久値を減らす
		tool.setDurability(durability);

		//壊したブロック数に応じてクールダウンを発生させる
		long cooldown = (long) ActiveSkill.BREAK.getCoolDown(playerdata.activeskilldata.skillnum) * breaklist.size() /((end.x - start.x + 1) * (end.z - start.z + 1) * (end.y - start.y + 1));
		if(cooldown >= 5){
			new CoolDownTaskRunnable(player,false,true).runTaskLater(plugin,cooldown);
		}else{
			new CoolDownTaskRunnable(player,false,false).runTaskLater(plugin,cooldown);
		}

		//以降破壊する処理

		//溶岩の破壊する処理
		for(int lavanum = 0 ; lavanum <lavalist.size();lavanum++){
			lavalist.get(lavanum).setType(Material.AIR);
		}

		//選択されたブロックを破壊する処理

		//自身のみしか壊さない時自然に処理する
		if(breaklist.size()==1){
			Util.BreakBlock(player, block, centerofblock, tool,false);
		}//エフェクトが指定されていないときの処理
		else if(playerdata.activeskilldata.effectnum == 0){
			for(Block b:breaklist){
				Util.BreakBlock(player, b, centerofblock, tool,true);
				playerdata.activeskilldata.blocklist.remove(b);
			}

		}
		//エフェクトが指定されているときの処理
		else{
			ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
			skilleffect[playerdata.activeskilldata.effectnum - 1].runBreakEffect(player,playerdata,tool,new ArrayList<Block>(breaklist), start, end,centerofblock);
		}
	}
}
