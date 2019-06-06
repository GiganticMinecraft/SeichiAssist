package com.github.unchama.seichiassist.effect.breaking;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.AsyncEntityRemover;
import com.github.unchama.seichiassist.util.BreakUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import java.util.List;
import java.util.Random;

public class MagicTask extends AbstractRoundedTask {
	// プレイヤー情報
	Player player;
	// プレイヤーデータ
	PlayerData playerdata;
	// プレイヤーの位置情報
	Location ploc;
	// 破壊するブロックの中心位置
	Location centerbreakloc;
	// 使用するツール
	ItemStack tool;
	// 破壊するブロックリスト
	List<Block> blocks;
	// スキルで破壊される相対座標
	Coordinate start, end;
	// スキルが発動される中心位置
	Location droploc;
	// 逐一更新が必要な位置
	Location effectloc;
	Wool woolBlock;
	BlockState state;

	public MagicTask(Player player, PlayerData playerdata, ItemStack tool, List<Block> blocks, Coordinate start,
					 Coordinate end, Location droploc) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.blocks = blocks;
		this.start = start;
		this.end = end;
		this.droploc = droploc.clone();

		this.ploc = player.getLocation().clone();
		this.centerbreakloc = this.droploc.add(start.x + (end.x - start.x) / 2, start.y + (end.y - start.y) / 2, start.z + (end.z - start.z) / 2);

	}

	@Override
	public void firstAction() {
		//1回目のrun
		DyeColor[] colors = { DyeColor.RED, DyeColor.BLUE, DyeColor.YELLOW, DyeColor.GREEN };
		int rd = new Random().nextInt(colors.length);

		for (Block b : blocks) {
			BreakUtil.breakBlock(player, b, droploc, tool, false);
			b.setType(Material.WOOL);
			state = b.getState();
			woolBlock = (Wool) state.getData();
			woolBlock.setColor(colors[rd]);
			state.update();
		}
	}

	@Override
	public void secondAction() {
		//2回目のrun
		if (SeichiAssist.entitylist.isEmpty()) {
			Chicken e = (Chicken) player.getWorld().spawnEntity(centerbreakloc, EntityType.CHICKEN);
			SeichiAssist.entitylist.add(e);
			e.playEffect(EntityEffect.WITCH_MAGIC);
			e.setInvulnerable(true);
			new AsyncEntityRemover(e).runTaskLater(SeichiAssist.instance, 100);
			player.getWorld().playSound(effectloc, Sound.ENTITY_WITCH_AMBIENT, 1, 1.5F);
		}

		for (Block b : blocks) {
			b.setType(Material.AIR);
			b.getWorld().spawnParticle(Particle.NOTE, b.getLocation().add(0.5, 0.5, 0.5), 1);
			SeichiAssist.allblocklist.remove(b);
		}
		cancel();
	}
}
