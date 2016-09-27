package com.github.unchama.seichiassist.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.BreakArea;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;

public class AssaultTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Player player;
	UUID uuid;
	PlayerData playerdata;
	int level;
	int type;
	int playerlocy;
	PlayerInventory inventory;
	ItemStack tool;
	//経験値変更用のクラスを設定
	ExperienceManager expman;
	//一回の破壊の範囲
	Coordinate breaklength;
	//１回の全て破壊したときのブロック数
	int ifallbreaknum;
	//破壊エリアデータ
	BreakArea assaultarea;



	boolean errorflag = false;

	boolean waterflag = false,lavaflag = false,breakflag = false,condensflag = false;

	public AssaultTaskRunnable(Player player) {
		this.player = player;
		this.uuid = player.getUniqueId();
		this.playerdata = playermap.get(uuid);


		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[blockbreaklistener処理]でエラー発生");
			plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			errorflag = true;
			return;
		}

		this.level = playerdata.activeskilldata.assaultnum;
		this.type = playerdata.activeskilldata.assaulttype;
		this.assaultarea = playerdata.activeskilldata.assaultarea;


		//もしサバイバルでなければ処理を終了
		//もしフライ中なら終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){// || player.isFlying()){
			player.sendMessage(ChatColor.GREEN + "ゲームモードをサバイバルに変更してください。");
			errorflag = true;
			return;
		}

		this.expman = new ExperienceManager(player);

		//プレイヤーインベントリを取得
		inventory = player.getInventory();
		//メインハンドとオフハンドを取得
		ItemStack mainhanditem = inventory.getItemInMainHand();
		ItemStack offhanditem = inventory.getItemInOffHand();
		//実際に使用するツールを格納する
		tool = null;
		//メインハンドにツールがあるか
		boolean mainhandtoolflag = SeichiAssist.breakmateriallist.contains(mainhanditem.getType());
		//オフハンドにツールがあるか
		boolean offhandtoolflag = SeichiAssist.breakmateriallist.contains(offhanditem.getType());

		//場合分け
		if(offhandtoolflag){
			//サブハンドの時
			tool = offhanditem;
		}else if(mainhandtoolflag){
			//メインハンドの時
			player.sendMessage(ChatColor.GREEN + "使うツールをオフハンドにセット(fキー)してください");
			errorflag = true;
			return;
		}else{
			//どちらにももっていない時処理を終了
			player.sendMessage(ChatColor.GREEN + "使うツールをオフハンドにセット(fキー)してください");
			errorflag = true;
			return;
		}
		//耐久値がマイナスかつ耐久無限ツールでない時処理を終了
		if(tool.getDurability() > tool.getType().getMaxDurability() && !tool.getItemMeta().spigot().isUnbreakable()){
			player.sendMessage(ChatColor.GREEN + "不正な耐久値です。");
			errorflag = true;
			return;
		}
		this.breaklength = assaultarea.getBreakLength();
		//壊すフラグを指定
		if(playerdata.activeskilldata.assaulttype == ActiveSkill.CONDENSE.gettypenum()){
			if(playerdata.activeskilldata.assaultnum < 7){
				waterflag = true;
			}else{
				lavaflag = true;
			}
			condensflag = true;

		}else if(playerdata.activeskilldata.assaulttype == ActiveSkill.ARMOR.gettypenum()){
			breakflag = true;
		}
		ifallbreaknum = (breaklength.x * breaklength.y * breaklength.z);

		//以下実行処理
		if(!errorflag){
			//プレイヤーに使用音
			player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, (float)1.5, (float) 0.65);
			//プレイヤーにエフェクト
		}

	}
	private void setCancel() {
		if(!errorflag){
			//プレイヤーに終了音
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_CLOSE, (float)1.5, (float) 0.65);
			//プレイヤーにエフェクト終了
		}
		playerdata.activeskilldata.assaultflag = false;
		playerdata.activeskilldata.mineflagnum = 0;
		playerdata.activeskilldata.removeAreaTask(true);
		this.cancel();
	}

	@Override
	public void run() {

		if(isCanceled()){
			setCancel();
			return;
		}
		//もしサバイバルでなければ処理を終了
		//もしフライ中なら終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){// || player.isFlying()){
			player.sendMessage(ChatColor.GREEN + "ゲームモードをサバイバルに変更してください。");
			setCancel();
			return;
		}
		List<Block> breaklist = new ArrayList<Block>();
		List<Block> lavalist = new ArrayList<Block>();
		List<Block> waterlist = new ArrayList<Block>();

		//プレイヤーの足のy座標を取得
		int playerlocy = player.getLocation().getBlockY() - 1 ;
		Block block = player.getLocation().getBlock();

		ItemStack offhanditem = inventory.getItemInOffHand();
		//最初に登録したツールと今のツールが違う場合
		if(!tool.equals(offhanditem)){
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "ツールの変更を検知しました");
			}
			setCancel();
			return;
		}



		//壊されるブロックの宣言
		Block breakblock;
		Coordinate start = assaultarea.getStartList().get(0);
		Coordinate end = assaultarea.getEndList().get(0);

		for(int x = start.x ; x <= end.x ; x++){
			for(int z = start.z ; z <= end.z ; z++){
				for(int y = start.y; y <= end.y ; y++){
					breakblock = block.getRelative(x, y, z);
					boolean lava_materialflag = breakblock.getType().equals(Material.STATIONARY_LAVA)
												|| breakblock.getType().equals(Material.LAVA);
					boolean water_materialflag = breakblock.getType().equals(Material.STATIONARY_WATER)
												|| breakblock.getType().equals(Material.WATER);
					if(SeichiAssist.materiallist.contains(breakblock.getType())
							|| lava_materialflag || water_materialflag
							){
						if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking() || breakblock.equals(block) || !breakflag){
							if(Util.canBreak(player, breakblock)){
								if(lava_materialflag){
									lavalist.add(breakblock);
								}else if(water_materialflag){
									waterlist.add(breakblock);
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
		//重力値計算
		double gravity = Util.getGravity(player,block,end.y,1);

		// 実際に破壊するブロック数の計算分岐
		int breaksum = 0;
		if(waterflag){
			breaksum = waterlist.size();
		}else if(lavaflag){
			breaksum = lavalist.size();
		}else if(breakflag){
			breaksum = waterlist.size() + lavalist.size() + breaklist.size();
		}

		//減る経験値計算
		//実際に破壊するブロック数 * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力

		double useExp = (double)breaksum * gravity
				* ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.assaulttype, playerdata.activeskilldata.assaultnum)
				/(ifallbreaknum) ;



		//減る耐久値の計算
		short durability = (short) (tool.getDurability() + Util.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),breaksum));


		//重力値の判定
		if(gravity > 15){
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "スキルを使用するには上から掘ってください。");
			}
			playerdata.activeskilldata.blocklist.removeAll(breaklist);
			setCancel();
			return;
		}

		//実際に経験値を減らせるか判定
		if(!expman.hasExp(useExp)){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
			}
			playerdata.activeskilldata.blocklist.removeAll(breaklist);
			setCancel();
			return;
		}


		//実際に耐久値を減らせるか判定
		if(tool.getType().getMaxDurability() <= durability && !tool.getItemMeta().spigot().isUnbreakable()){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません");
			}
			playerdata.activeskilldata.blocklist.removeAll(breaklist);
			setCancel();
			return;
		}


		//経験値を減らす
		expman.changeExp(-useExp);

		//耐久値を減らす
		tool.setDurability(durability);


		//以降破壊する処理

		//破壊する処理分岐
		if(waterflag){
			for(int waternum = 0 ; waternum <waterlist.size();waternum++){
				waterlist.get(waternum).setType(Material.PACKED_ICE);
				Util.logPlace(player,waterlist.get(waternum));
			}
		}else if(lavaflag){
			for(int lavanum = 0 ; lavanum <lavalist.size();lavanum++){
				lavalist.get(lavanum).setType(Material.MAGMA);
				Util.logPlace(player,lavalist.get(lavanum));
			}
		}else if(breakflag){
			for(int waternum = 0 ; waternum <waterlist.size();waternum++){
				waterlist.get(waternum).setType(Material.AIR);
			}
			for(int lavanum = 0 ; lavanum <lavalist.size();lavanum++){
				lavalist.get(lavanum).setType(Material.AIR);
			}
			for(Block b:breaklist){
				Util.BreakBlock(player, b, player.getLocation(), tool,false);
				playerdata.activeskilldata.blocklist.remove(b);
			}
		}
		playerdata.activeskilldata.blocklist.removeAll(breaklist);
	}

	private boolean isCanceled() {
		if(playerdata.activeskilldata.mineflagnum == 0 || errorflag){
			return true;
		}else{
			return false;
		}
	}
}
