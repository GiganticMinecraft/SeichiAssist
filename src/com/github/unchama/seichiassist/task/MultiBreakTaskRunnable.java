package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BreakUtil;

public class MultiBreakTaskRunnable extends BukkitRunnable{
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	private Player player;
	private Location droploc;
	private ItemStack tool;
	private List<List<Block>> multibreaklist;
	private List<List<Block>> multilavalist;
	private List<Coordinate> startlist;
	private List<Coordinate> endlist;
	private int breaknum;
	private UUID uuid;
	private PlayerData playerdata;
	private int count;

	public MultiBreakTaskRunnable(Player player,Block centerblock,ItemStack tool,
			List<List<Block>> multibreaklist, List<List<Block>> multilavalist,
			List<Coordinate> startlist, List<Coordinate> endlist) {
		this.player = player;
		this.droploc = centerblock.getLocation().add(0.5, 0.5, 0.5);
		this.tool = tool;
		this.multibreaklist = multibreaklist;
		this.multilavalist = multilavalist;
		this.startlist = startlist;
		this.endlist = endlist;
		this.breaknum = multibreaklist.size();
		this.count = 0;
		//this.key = key;
		//UUIDを取得
		uuid = player.getUniqueId();
		//playerdataを取得
		playerdata = playermap.get(uuid);
	}

	@Override
	public void run() {
		if(count < breaknum){
			if(SeichiAssist.DEBUG){
				player.sendMessage("" + count);
			}
			//溶岩の破壊する処理
			for(int lavanum = 0 ; lavanum < multilavalist.get(count).size();lavanum++){
				multilavalist.get(count).get(lavanum).setType(Material.AIR);
			}

			//エフェクトが選択されていない時の通常処理
			if(playerdata.activeskilldata.effectnum == 0){
				//ブロックを破壊する処理
				for(Block b:multibreaklist.get(count)){
					BreakUtil.BreakBlock(player, b, droploc, tool,false);
					SeichiAssist.allblocklist.remove(b);
				}
			}

			//通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
			else if(playerdata.activeskilldata.effectnum <= 100){
				ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
				skilleffect[playerdata.activeskilldata.effectnum - 1].runBreakEffect(player,playerdata,tool,multibreaklist.get(count), startlist.get(count), endlist.get(count),droploc);
			}

			//スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
			else if(playerdata.activeskilldata.effectnum > 100){
				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
				premiumeffect[playerdata.activeskilldata.effectnum - 1 - 100].runBreakEffect(player,playerdata,tool,multibreaklist.get(count), startlist.get(count), endlist.get(count),droploc);
			}
			count++;
		}else{
			cancel();
		}

	}

}
