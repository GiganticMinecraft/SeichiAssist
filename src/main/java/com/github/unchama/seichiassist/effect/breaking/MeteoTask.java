package com.github.unchama.seichiassist.effect.breaking;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.effect.FixedMetadataValueHolder;
import com.github.unchama.seichiassist.task.ArrowControlTask;
import com.github.unchama.seichiassist.util.BreakUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class MeteoTask extends BukkitRunnable{
	//プレイヤー情報
	Player player;
	//プレイヤーデータ
	PlayerData playerdata;
	//プレイヤーの位置情報
	Location ploc;
	//破壊するブロックの中心位置
	Location centerbreakloc;
	//使用するツール
	ItemStack tool;
	//破壊するブロックリスト
	List<Block> blocks;
	//スキルで破壊される相対座標
	Coordinate start, end;
	//スキルが発動される中心位置
	Location droploc;
	//逐一更新が必要な位置
	Location effectloc;
	//音の聞こえる距離
	int soundRadius;
	//音距離を設定するフラグ
	boolean setRange;
	//隕石
	LargeFireball proj;

	public MeteoTask(Player player, PlayerData playerdata, ItemStack tool, List<Block> blocks, Coordinate start,
					 Coordinate end, Location droploc) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.blocks = blocks;
		this.start = start;
		this.end = end;
		this.droploc = droploc.clone();

		this.ploc = player.getLocation().clone();
		this.centerbreakloc = this.droploc.add(start.x + (end.x-start.x)/2, start.y + (end.y-start.y)/2,start.z + (end.z-start.z)/2);

		//音の聞こえる範囲を設定
		soundRadius = 5;

		//音を設定するか設定
		setRange = playerdata.getActiveskilldata().skilltype == ActiveSkill.BREAK.gettypenum();
		if(playerdata.getActiveskilldata().skillnum > 2){
			launchFireball();
		}

	}

	private void launchFireball() {
		//メテオの発射位置を決定
		Random rand = new Random();
		Location launchloc = ploc.add((rand.nextDouble() * 20)-10 ,(rand.nextDouble() * 20) - 10 + 60,(rand.nextDouble() * 20)-10);
		Vector vec = new Vector(centerbreakloc.getX()-launchloc.getX(),centerbreakloc.getY()-launchloc.getY(),centerbreakloc.getZ()-launchloc.getZ());
		launchloc.setDirection(vec);
		vec.normalize();
		double k = 1.0;
		vec.multiply(k);
		proj = player.getWorld().spawn(launchloc, LargeFireball.class);
		proj.setShooter(player);
		proj.setMetadata("Effect", FixedMetadataValueHolder.TRUE);
		proj.setVelocity(vec);
		new ArrowControlTask(proj,centerbreakloc).runTaskTimer(SeichiAssist.instance, 0, 1);
	}

	@Override
	public void run() {
		for(int x = start.x + 1; x < end.x ; x += 2){
			for(int z = start.z + 1; z < end.z ; z += 2){
				for(int y = start.y + 1; y < end.y ; y += 2){
					effectloc = droploc.clone();
					effectloc.add(x,y,z);
					if(isBreakBlock(effectloc)){
						// TODO: Effect.EXPLOSION_HUGE -> Particle.EXPLOSION_HUGE
						player.getWorld().playEffect(effectloc, Effect.EXPLOSION_HUGE,1);
					}
					//player.spawnParticle(Particle.EXPLOSION_NORMAL,explosionloc.add(x, y, z),1);
					//player.playSound(explosionloc.add(x, y, z), Sound.ENTITY_GENERIC_EXPLODE, (float)1, (float)((rand.nextDouble()*0.4)+0.8));
					//player.getWorld().playEffect(explosionloc.add(x, y, z), Effect.EXPLOSION, 0,(int)10);
				}
			}
		}
		// 0..1 -> 0..0.4 -> 0.8..1.2
		// (float)((Math.random()*0.4)+0.8)
		player.getWorld().playSound(centerbreakloc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 1.2f - (new Random().nextFloat() / 0.25f));
		final boolean stepflag = !(playerdata.getActiveskilldata().skillnum > 2);
		for(Block b : blocks){
			BreakUtil.breakBlock(player, b, droploc, tool, stepflag);
			SeichiAssist.allblocklist.remove(b);
		}
	}

	/**
	 * {@code loc}を中心にした3x3x3の立方体の範囲のブロックが一つでも{@code blocks}に格納されているか調べる
	 * @param loc 中心点
	 * @return 含まれているならtrue、含まれていないならfalse
	 */
	private boolean isBreakBlock(Location loc) {
		Block b = loc.getBlock();
		if(blocks.contains(b)) return true;
		for(int x = -1 ; x <= 1 ; x++) {
			for(int z = -1 ; z <= 1 ; z++) {
				for(int y = -1; y <= 1 ; y++) {
					if(blocks.contains(b.getRelative(x, y, z))) return true;
				}
			}
		}
		return false;
	}

}

