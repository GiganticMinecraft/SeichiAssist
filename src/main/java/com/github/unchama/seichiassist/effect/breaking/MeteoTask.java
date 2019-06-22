package com.github.unchama.seichiassist.effect.breaking;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.effect.XYZIterator2;
import com.github.unchama.seichiassist.effect.XYZTuple;
import com.github.unchama.seichiassist.util.BreakUtil;
import kotlin.Unit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class MeteoTask extends AbstractBreakTask2 {
	//プレイヤー情報
	private final Player player;
	//プレイヤーデータ
	private final PlayerData playerdata;
	//破壊するブロックの中心位置
	private final Location centerbreakloc;
	//使用するツール
	private final ItemStack tool;
	//破壊するブロックリスト
	private final List<Block> blocks;
	//スキルで破壊される相対座標
	private final Coordinate start;
	private final Coordinate end;
	//スキルが発動される中心位置
	private final Location droploc;

	public MeteoTask(final Player player, final PlayerData playerdata, final ItemStack tool, final List<Block> blocks, final Coordinate start,
                     final Coordinate end, final Location droploc) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.blocks = blocks;
		this.start = start;
		this.end = end;
		this.droploc = droploc.clone();
		this.centerbreakloc = this.droploc.add(relativeAverage(start.x, end.x), relativeAverage(start.y, end.y), relativeAverage(start.z, end.z));
	}

	@Override
	public void run() {
		new XYZIterator2(new XYZTuple(start.x, start.y, start.z), new XYZTuple(end.x, end.y, end.z), xyzTuple -> {
			//逐一更新が必要な位置
			final Location effectloc = droploc.clone().add(xyzTuple.getX(), xyzTuple.getY(), xyzTuple.getZ());
			if(isBreakBlock(effectloc)) {
				// TODO: Effect.EXPLOSION_HUGE -> Particle.EXPLOSION_HUGE
				player.getWorld().playEffect(effectloc, Effect.EXPLOSION_HUGE,1);
			}
			return Unit.INSTANCE;
		}).doAction();
		// 0..1 -> 0..0.4 -> 0.8..1.2
		final float vol = (new Random().nextFloat() * 0.4f) + 0.8f;
		player.getWorld().playSound(centerbreakloc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, vol);
		final boolean stepflag = playerdata.getActiveskilldata().skillnum <= 2;
		for(final Block b : blocks){
			BreakUtil.breakBlock(player, b, droploc, tool, stepflag);
			SeichiAssist.allblocklist.remove(b);
		}
	}

	private double relativeAverage(final int i1, final int i2) {
	    return i1 + (i2 - i1) / 2;
    }
}

