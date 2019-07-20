package com.github.unchama.seichiassist.effect.breaking;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.AsyncEntityRemover;
import com.github.unchama.seichiassist.util.BreakUtil;
import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class MagicTask extends AbstractRoundedTask {
	// プレイヤー情報
	private final Player player;
	// 破壊するブロックの中心位置
	private final Location centerBreak;
	// 使用するツール
	private final ItemStack tool;
	// 破壊するブロックリスト
	private final List<Block> blocks;
	// スキルが発動される中心位置
	private final Location skillCenter;

	public MagicTask(final @NotNull Player player, final ItemStack tool, final List<Block> blocks, final Coordinate start,
					 final Coordinate end, final Location skillCenter) {
		this.player = player;
		this.tool = tool;
		this.blocks = blocks;
		// スキルで破壊される相対座標
		this.skillCenter = skillCenter.clone();

		centerBreak = this.skillCenter.add(relativeAverage(start.x, end.x), relativeAverage(start.y, end.y), relativeAverage(start.z, end.z));
	}

	@Override
	public void firstAction() {
		//1回目のrun
		final DyeColor[] colors = { DyeColor.RED, DyeColor.BLUE, DyeColor.YELLOW, DyeColor.GREEN };
		final int rd = new Random().nextInt(colors.length);

		for (final Block b : blocks) {
			BreakUtil.breakBlock(player, b, skillCenter, tool, false);
			b.setType(Material.WOOL);
			final BlockState state = b.getState();
			final Wool woolBlock = (Wool) state.getData();
			woolBlock.setColor(colors[rd]);
			state.update();
		}
	}

	@Override
	public void secondAction() {
		//2回目のrun
		if (SeichiAssist.Companion.getEntitylist().isEmpty()) {
			final Chicken e = (Chicken) player.getWorld().spawnEntity(centerBreak, EntityType.CHICKEN);
			SeichiAssist.Companion.getEntitylist().add(e);
			e.playEffect(EntityEffect.WITCH_MAGIC);
			e.setInvulnerable(true);
			new AsyncEntityRemover(e).runTaskLater(SeichiAssist.Companion.getInstance(), 100);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 1, 1.5F);
		}

		for (final Block b : blocks) {
			b.setType(Material.AIR);
			b.getWorld().spawnParticle(Particle.NOTE, b.getLocation().add(0.5, 0.5, 0.5), 1);
			SeichiAssist.Companion.getAllblocklist().remove(b);
		}
		cancel();
	}

	private double relativeAverage(final int i1, final int i2) {
		return i1 + (i2 - i1) / 2;
	}
}
