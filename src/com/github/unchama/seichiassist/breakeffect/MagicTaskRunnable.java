package com.github.unchama.seichiassist.breakeffect;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.EntityRemoveTaskRunnable;
import com.github.unchama.seichiassist.util.Util;

public class MagicTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	//プレイヤー情報
	Player player;
	//プレイヤーデータ
	PlayerData playerdata;
	//プレイヤーの位置情報
	Location ploc;
	//ブロックの位置情報
	Location bloc;
	//破壊するブロックの中心位置
	Location centerbreakloc;
	//使用するツール
	ItemStack tool;
	//破壊するブロックリスト
	List<Block> breaklist;
	//スキルで破壊される相対座標
	Coordinate start,end;
	//スキルが発動される中心位置
	Location droploc;
	//相対座標から得られるスキルの範囲座標
	Coordinate breaklength;
	//逐一更新が必要な位置
	Location effectloc;
	Wool red;
	BlockState state;
	Material m;


	public MagicTaskRunnable(Player player,PlayerData playerdata,ItemStack tool,List<Block> breaklist, Coordinate start,
			Coordinate end, Location droploc) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.breaklist = breaklist;
		this.start = start;
		this.end = end;
		this.droploc = droploc.clone();

		this.ploc = player.getLocation().clone();
		this.centerbreakloc = this.droploc.add(start.x + (end.x-start.x)/2, start.y + (end.y-start.y)/2,start.z + (end.z-start.z)/2);



		for(Block b : breaklist){
			Util.BreakBlock(player, b, droploc, tool, false);
			b.setType(Material.WOOL);
	        state = b.getState();
	        red = (Wool)state.getData();
	        red.setColor(DyeColor.RED);
	        state.update();
		}
	}

	@Override
	public void run() {
		for(int x = start.x + 1 ; x < end.x ; x=x+2){
			for(int z = start.z + 1 ; z < end.z ; z=z+2){
				for(int y = start.y + 1; y < end.y ; y=y+2){
					effectloc = droploc.clone();
					effectloc.add(x, y, z);
					if(isBreakBlock(effectloc)){
						Chicken e = (Chicken) player.getWorld().spawnEntity(effectloc, EntityType.CHICKEN);
						e.playEffect(EntityEffect.WITCH_MAGIC);
						e.setInvulnerable(true);
						new EntityRemoveTaskRunnable((Entity)e).runTaskLater(plugin,100);
						player.getWorld().playSound(effectloc, Sound.ENTITY_WITCH_AMBIENT, 1, 1.5F);
					}
				}
			}
		}
		for(Block b : breaklist){
			b.setType(Material.AIR);
			b.getWorld().playEffect(b.getLocation().add(0.5,0.5,0.5), Effect.NOTE, 1);
			playerdata.activeskilldata.blocklist.remove(b);
		}

	}
	private boolean isBreakBlock(Location loc) {
		Block b = loc.getBlock();
		if(breaklist.contains(b))return true;
		for(int x = -1 ; x < 2 ; x++){
			for(int z = -1 ; z < 2 ; z++){
				for(int y = -1; y < 2 ; y++){
					if(breaklist.contains(b.getRelative(x, y, z)))return true;
				}
			}
		}
		return false;
	}
}
