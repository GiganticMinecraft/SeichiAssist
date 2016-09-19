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

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class MultiBreakTaskRunnable extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.plugin;
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
	private Material material;

	public MultiBreakTaskRunnable(Player player,Block centerblock,ItemStack tool,
			List<List<Block>> multibreaklist, List<List<Block>> multilavalist,
			List<Coordinate> startlist, List<Coordinate> endlist) {
		this.player = player;
		this.droploc = centerblock.getLocation().add(0.5, 0.5, 0.5);
		this.material = centerblock.getType();
		this.tool = tool;
		this.multibreaklist = multibreaklist;
		this.multilavalist = multilavalist;
		this.startlist = startlist;
		this.endlist = endlist;
		this.breaknum = multibreaklist.size();
		this.count = 0;
		//UUIDを取得
		uuid = player.getUniqueId();
		//playerdataを取得
		playerdata = playermap.get(uuid);
		//クールダウンタイム生成
		new CoolDownTaskRunnable(player).runTaskLater(plugin,ActiveSkill.MULTI.getCoolDown(playerdata.activeskilldata.skillnum));
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
			for(Block b:multibreaklist.get(count)){
				Util.BreakBlock(player, b, droploc, tool,true);
			}
			count++;
		}else{
			playerdata.activeskilldata.blocklist.clear();
			cancel();
		}

	}

}
