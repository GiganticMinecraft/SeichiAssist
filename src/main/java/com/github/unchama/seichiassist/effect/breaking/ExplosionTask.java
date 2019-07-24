package com.github.unchama.seichiassist.effect.breaking;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.effect.XYZIterator2;
import com.github.unchama.seichiassist.effect.XYZTuple;
import com.github.unchama.seichiassist.util.BreakUtil;
import kotlin.Unit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ExplosionTask extends AbstractBreakTask2 {
	private final Player player;
	private final PlayerData playerdata;
	private final ItemStack tool;
	//破壊するブロックリスト
	private final List<Block> blocks;
	//スキルで破壊される相対座標
	private final Coordinate start;
	private final Coordinate end;
	//スキルが発動される中心位置
	private final Location droploc;
	public ExplosionTask(final Player player, final PlayerData playerdata, final ItemStack tool, final List<Block> blocks, final Coordinate start,
						 final Coordinate end, final Location droploc) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.blocks = blocks;
		this.start = start;
		this.end = end;
		this.droploc = droploc;
	}

	@Override
	public void run() {
		new XYZIterator2(new XYZTuple(start.x, start.y, start.z), new XYZTuple(end.x, end.y, end.z), xyzTuple -> {
			final Location explosionloc = droploc.clone();
			explosionloc.add(xyzTuple.getX(), xyzTuple.getY(), xyzTuple.getZ());
			if(isBreakBlock(explosionloc)) {
				player.getWorld().createExplosion(explosionloc, 0, false);
			}
			return Unit.INSTANCE;
		});

		final boolean stepflag = playerdata.getActiveskilldata().skillnum <= 2;
		for(final Block b : blocks) {
			BreakUtil.breakBlock(player, b, droploc, tool, stepflag);
			SeichiAssist.Companion.getAllblocklist().remove(b);
		}
	}
}

