package com.github.unchama.seichiassist.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;

public class AssaultArmorTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Player player;
	UUID uuid;
	PlayerData playerdata;

	Location ploc;
	int playerlocy;
	PlayerInventory inventory;
	ItemStack tool;
	//経験値変更用のクラスを設定
	ExperienceManager expman;

	public AssaultArmorTaskRunnable(Player player) {
		this.player = player;
		uuid = player.getUniqueId();
		playerdata = playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[blockbreaklistener処理]でエラー発生");
			plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			playerdata.activeskilldata.mineflagnum = 0;
			this.cancel();
			return;
		}

		expman = new ExperienceManager(player);


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
			playerdata.activeskilldata.mineflagnum = 0;
			this.cancel();
			return;
		}else{
			//どちらにももっていない時処理を終了
			player.sendMessage(ChatColor.GREEN + "使うツールをオフハンドにセット(fキー)してください");
			playerdata.activeskilldata.mineflagnum = 0;
			this.cancel();
			return;
		}
		//耐久値がマイナスかつ耐久無限ツールでない時処理を終了
		if(tool.getDurability() > tool.getType().getMaxDurability() && !tool.getItemMeta().spigot().isUnbreakable()){
			player.sendMessage(ChatColor.GREEN + "不正な耐久値です。");
			playerdata.activeskilldata.mineflagnum = 0;
			this.cancel();
			return;
		}
	}

	@Override
	public void run() {
		if(playerdata.activeskilldata.mineflagnum == 0){
			this.cancel();
			return;
		}
		//もしサバイバルでなければ処理を終了
		//もしフライ中なら終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL) || player.isFlying()){
			player.sendMessage(ChatColor.GREEN + "フライ機能をOFFにしてください.");
			playerdata.activeskilldata.mineflagnum = 0;
			this.cancel();
			return;
		}
		List<Block> breaklist = new ArrayList<Block>();
		List<Block> lavalist = new ArrayList<Block>();

		ploc = player.getLocation();
		//プレイヤーの足のy座標を取得
		int playerlocy = ploc.getBlockY() - 1 ;
		Block block = ploc.getBlock();

		ItemStack offhanditem = inventory.getItemInOffHand();
		//最初に登録したツールと今のツールが違う場合
		if(!tool.equals(offhanditem)){
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "ツールの変更を検知しました");
			}
			playerdata.activeskilldata.mineflagnum = 0;
			this.cancel();
			return;
		}



		//壊されるブロックの宣言
		Block breakblock;
		Coordinate start = new Coordinate(-2,-1,-2);
		Coordinate end = new Coordinate(2,2,2);

		for(int x = start.x ; x <= end.x ; x++){
			for(int z = start.z ; z <= end.z ; z++){
				for(int y = start.y; y <= end.y ; y++){
					breakblock = block.getRelative(x, y, z);
					if(SeichiAssist.materiallist.contains(breakblock.getType())
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
				* 100
				/((end.x - start.x + 1) * (end.z - start.z + 1) * (end.y - start.y + 1)) ;
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
			playerdata.activeskilldata.blocklist.removeAll(breaklist);
			playerdata.activeskilldata.mineflagnum = 0;
			this.cancel();
			return;
		}


		//実際に耐久値を減らせるか判定
		if(tool.getType().getMaxDurability() <= durability && !tool.getItemMeta().spigot().isUnbreakable()){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません");
			}
			playerdata.activeskilldata.blocklist.removeAll(breaklist);
			playerdata.activeskilldata.mineflagnum = 0;
			this.cancel();
			return;
		}


		//経験値を減らす
		expman.changeExp(-useExp);

		//耐久値を減らす
		tool.setDurability(durability);


		//以降破壊する処理

		//溶岩の破壊する処理
		for(int lavanum = 0 ; lavanum <lavalist.size();lavanum++){
			lavalist.get(lavanum).setType(Material.AIR);
		}

		for(Block b:breaklist){
			Util.BreakBlock(player, b, ploc, tool,true);
			playerdata.activeskilldata.blocklist.remove(b);
		}
	}

}
