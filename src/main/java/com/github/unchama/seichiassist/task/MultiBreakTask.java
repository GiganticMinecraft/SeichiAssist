package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class MultiBreakTask extends BukkitRunnable{
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.Companion.getPlayermap();
	private Player player;
	private Location droploc;
	private ItemStack tool;
	private List<List<Block>> multibreaklist;
	private List<List<Block>> multilavalist;
	private List<Coordinate> startlist;
	private List<Coordinate> endlist;
	private int breaknum;
	private PlayerData playerdata;
	private int count;

	public MultiBreakTask(Player player, Block centerblock, ItemStack tool,
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
		//playerdataを取得
		playerdata = playermap.get(player.getUniqueId());
	}

	@Override
	public void run() {
		if(count < breaknum){
			if(SeichiAssist.Companion.getDEBUG()){
				player.sendMessage("" + count);
			}
			//溶岩の破壊する処理
			for(int lavanum = 0 ; lavanum < multilavalist.get(count).size();lavanum++){
				multilavalist.get(count).get(lavanum).setType(Material.AIR);
			}

			final Set<Block> converted = new HashSet<>(multibreaklist.get(count));
			final Coordinate startPoint = startlist.get(count);
			final Coordinate endPoint = endlist.get(count);
			//エフェクトが選択されていない時の通常処理
			if(playerdata.getActiveskilldata().effectnum == 0){
				//ブロックを破壊する処理
				for(Block b:multibreaklist.get(count)){
					BreakUtil.breakBlock(player, b, droploc, tool,false);
					SeichiAssist.Companion.getAllblocklist().remove(b);
				}
			}

			//通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
			else if(playerdata.getActiveskilldata().effectnum <= 100){
				ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
				skilleffect[playerdata.getActiveskilldata().effectnum - 1].runBreakEffect(player, playerdata, tool, converted, startPoint, endPoint, droploc);
			}

			//スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
			else if(playerdata.getActiveskilldata().effectnum > 100){
				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
				premiumeffect[playerdata.getActiveskilldata().effectnum - 1 - 100].runBreakEffect(player, tool, converted, startPoint, endPoint, droploc);
			}
			count++;
		}else{
			cancel();
		}

	}

}
