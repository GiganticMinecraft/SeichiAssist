package com.github.unchama.seichiassist.effect.breaking;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.effect.XYZIterator;
import com.github.unchama.seichiassist.effect.XYZTuple;
import com.github.unchama.seichiassist.util.BreakUtil;
import kotlin.Unit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BlizzardTask extends AbstractRoundedTask {
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
	//音の聞こえる距離
	private int soundRadius;
	private boolean setRadius;

	public BlizzardTask(final Player player, final PlayerData playerdata, final ItemStack tool, final List<Block> blocks, final Coordinate start,
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
	public void firstAction() {
		//1回目のrun
		if (playerdata.getActiveskilldata().skillnum <= 2) {
			for(final Block b : blocks){
				BreakUtil.breakBlock(player, b, droploc, tool, true);
				SeichiAssist.allblocklist.remove(b);
			}
			cancel();
		} else {
			for(final Block b : blocks){
				BreakUtil.breakBlock(player, b, droploc, tool, false);
				b.setType(Material.PACKED_ICE);
			}
		}
		soundRadius = 5;
		setRadius = playerdata.getActiveskilldata().skilltype == ActiveSkill.BREAK.gettypenum();
	}

	@Override
	public void secondAction() {
		//2回目のrun
		new XYZIterator(new XYZTuple(start.x, start.y, start.z), new XYZTuple(end.x, end.y, end.z), xyzTuple -> {
			//逐一更新が必要な位置
			final Location effectloc = droploc.clone().add(xyzTuple.getX(), xyzTuple.getY(), xyzTuple.getZ());
			if (blocks.contains(effectloc.getBlock())) {
				player.getWorld().playEffect(effectloc, Effect.SNOWBALL_BREAK, 1);
			}
			return Unit.INSTANCE;
		});

		if (playerdata.getActiveskilldata().skillnum > 2) {
			for (final Block b : blocks) {
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

