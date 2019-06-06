package com.github.unchama.seichiassist.effect.breaking;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BreakUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BlizzardTask extends AbstractRoundedTask {
	Player player;
	PlayerData playerdata;
	ItemStack tool;
	//破壊するブロックリスト
	List<Block> breaklist;
	//スキルで破壊される相対座標
	Coordinate start, end;
	//スキルが発動される中心位置
	Location droploc;
	//逐一更新が必要な位置
	Location effectloc;
	//音の聞こえる距離
	int soundRadius;
	boolean setRadius;

	public BlizzardTask(Player player, PlayerData playerdata, ItemStack tool, List<Block> breaklist, Coordinate start,
						Coordinate end, Location droploc) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.breaklist = breaklist;
		this.start = start;
		this.end = end;
		this.droploc = droploc;
	}

	@Override
	public void firstAction() {
		//1回目のrun
		if(playerdata.getActiveskilldata().skillnum > 2){
			for(Block b : breaklist){
				BreakUtil.breakBlock(player, b, droploc, tool, false);
				b.setType(Material.PACKED_ICE);
			}
		}else{
			for(Block b : breaklist){
				BreakUtil.breakBlock(player, b, droploc, tool, true);
				SeichiAssist.allblocklist.remove(b);
			}
			cancel();
		}
		soundRadius = 5;
		setRadius = playerdata.getActiveskilldata().skilltype == ActiveSkill.BREAK.gettypenum();
	}

	@Override
	public void secondAction() {
		//2回目のrun
		for (int x = start.x; x < end.x; x++) {
			for (int z = start.z; z < end.z; z++) {
				for (int y = start.y; y < end.y; y++) {
					effectloc = droploc.clone();
					effectloc.add(x, y, z);
					if (breaklist.contains(effectloc.getBlock())) {
						player.getWorld().playEffect(effectloc, Effect.SNOWBALL_BREAK, 1);
					}
				}
			}
		}
		if (playerdata.getActiveskilldata().skillnum > 2) {
			for (Block b : breaklist) {
				b.setType(Material.AIR);
				if (setRadius) {
					b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.PACKED_ICE, soundRadius);
				} else {
					b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.PACKED_ICE);
				}
				SeichiAssist.allblocklist.remove(b);
			}
		}
	}
}

