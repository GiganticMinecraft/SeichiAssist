package com.github.unchama.seichiassist.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;

public class CondenSkillTaskRunnable extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;

	private Projectile proj;


	public CondenSkillTaskRunnable(Projectile proj) {
		this.proj = proj;
	}

	@Override
	public void run() {
		//player取得
		Player player = (Player)proj.getShooter();

		//壊されるブロックの中心を取得
		Block block = player.getWorld().getBlockAt(proj.getLocation());

		//他人の保護がかかっている場合は処理を終了
		if(!Util.getWorldGuard().canBuild(player, block.getLocation())){
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

		//ヒットフラグですでに終了しているとき処理を終了
		if(playerdata.activeskilldata.hitflag){
			playerdata.activeskilldata.hitflag = false;
			return;
		}
		//スキルで破壊されるブロックの時処理を終了
		if(playerdata.activeskilldata.blocklist.contains(block)){
			if(SeichiAssist.DEBUG){
				player.sendMessage("スキルで使用中のブロックです。");
			}
			return;
		}

		//経験値変更用のクラスを設定
		ExperienceManager expman = new ExperienceManager(player);

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
		if(playerdata.activeskilldata.skilltype == ActiveSkill.CONDENSE.gettypenum()){
			if(playerdata.activeskilldata.skillnum < 7){
				runCondenSkillofExplosion(player,playerdata.activeskilldata.skillnum, block, tool, expman);
			}else{
				runCondenSkillofExplosion(player,playerdata.activeskilldata.skillnum - 3, block, tool, expman);
			}

		}

		proj.remove();

	}

	public static void runCondenSkillofExplosion(Player player,
			int skilllevel, Block block, ItemStack tool, ExperienceManager expman) {
		HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = playermap.get(uuid);
		//元ブロックの真ん中の位置を取得
		Location centerofblock = block.getLocation().add(0.5, 0.5, 0.5);

		//壊されるブロックの宣言
		Block breakblock;
		Coordinate start = new Coordinate(-(skilllevel-3),-(skilllevel-3),-(skilllevel-3));
		Coordinate end = new Coordinate((skilllevel-3),(skilllevel-3),(skilllevel-3));

		//エフェクト用に壊されるブロック全てのリストデータ
		List<Block> breaklist = new ArrayList<Block>();


		for(int x = start.x ; x <= end.x ; x++){
			for(int z = start.z ; z <= end.z ; z++){
				for(int y = start.y; y <= end.y ; y++){
					breakblock = block.getRelative(x, y, z);
					if(playerdata.activeskilldata.skillnum < 7){
						if(breakblock.getType().equals(Material.WATER)
								|| breakblock.getType().equals(Material.STATIONARY_WATER)
								){
							if(Util.canBreak(player, breakblock)){
									breaklist.add(breakblock);
							}
						}
					}else{
						if(breakblock.getType().equals(Material.LAVA)
								|| breakblock.getType().equals(Material.STATIONARY_LAVA)
								){
							if(Util.canBreak(player, breakblock)){
									breaklist.add(breakblock);
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

		//実際に経験値を減らせるか判定
		if(!expman.hasExp(useExp)){
			//デバッグ用
			if(SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
			}
			return;
		}
		if(SeichiAssist.DEBUG){
			player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値:" + durability);
		}
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


		//選択されたブロックを破壊する処理

		Boolean stepflag;
		if(breaklist.size() < 500){
			stepflag = true;
		}else{
			stepflag = false;
		}
		if(SeichiAssist.DEBUG){
			player.sendMessage("エフェクト判定値:" + breaklist.size() + " 結果:" + stepflag);
		}
		Material replacematerial = Material.AIR;
		if(playerdata.activeskilldata.skillnum < 7){
			 replacematerial = Material.PACKED_ICE;
		}else{
			replacematerial = Material.MAGMA;
		}

		//エフェクトが指定されていないときの処理
		if(playerdata.activeskilldata.effectnum == 0){
			for(Block b:breaklist){
				b.setType(replacematerial);
			}

		}
		//エフェクトが指定されているときの処理
		else{
			ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
			skilleffect[playerdata.activeskilldata.effectnum - 1].runCondensEffect(player,playerdata,tool,breaklist, start, end,centerofblock);
		}

		playerdata.activeskilldata.blocklist.clear();


	}

}
