package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.listener.PlayerBlockBreakListener;
import com.github.unchama.seichiassist.util.ExperienceManager;

public class ThunderStormTaskRunnable extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;

	UUID uuid;
	PlayerData playerdata;
	private Player player;
	private Block block;
	private ItemStack tool;
	private ExperienceManager expman;
	private int thundernum;
	private int playerlocy;
	private String dir;
	private Material material;
	private Location centerofblock;
	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public ThunderStormTaskRunnable(Player player,Block block,ItemStack tool, ExperienceManager expman) {
		this.player = player;
		this.block = block;
		this.tool = tool;
		this.expman = expman;
		thundernum = 0;//雷撃を行う回数
		//プレイヤーの足のy座標を取得
		playerlocy = player.getLocation().getBlockY() - 1 ;
		//プレイヤーの向いている方角を取得
		dir = PlayerBlockBreakListener.getCardinalDirection(player);
		//元ブロックのマテリアルを取得
		material = block.getType();
		//元ブロックの真ん中の位置を取得
		centerofblock = block.getLocation().add(0.5, 0.5, 0.5);
		//UUIDを取得
		uuid = player.getUniqueId();
		//playerdataを取得
		playerdata = playermap.get(uuid);
		//フラグ立てとく
		playerdata.skillflag = true;
		//クールダウンタイム生成
		new CoolDownTaskRunnable(player).runTaskLater(plugin,20);

	}
	@Override
	public void run() {

		if(thundernum > 4){
			//フラグ折っとく
			playerdata.skillflag = false;
			cancel();
		}else{
			thundernum++;

			//壊されるブロックの宣言
			Block breakblock;
			int startx = 0;
			int starty = -1;
			int startz = 0;
			int endx = 0;
			int endy = +1;
			int endz = 0;
			Location explosionloc = null;
			if(SeichiAssist.DEBUG){
				player.sendMessage("" + thundernum);
			}

			switch (dir){
				case "N":
					//北を向いているとき
					startx = -1;
					startz = -2 + (thundernum - 1) * 3 * (-1);
					endx = 1;
					endz = 0 + (thundernum - 1) * 3 * (-1);
					explosionloc = centerofblock.add(0, 0, -1 + (thundernum - 1) * 3 * (-1));
					break;
				case "E":
					//東を向いているとき
					startx = 0 + (thundernum - 1) * 3 * (1);
					startz = -1;
					endx = 2 + (thundernum - 1) * 3 * (1);
					endz = 1;
					explosionloc = centerofblock.add(1 + (thundernum - 1) * 3 * (1), 0, 0);
					break;
				case "S":
					//南を向いているとき
					startx = -1;
					startz = 0 + (thundernum - 1) * 3 * (1);
					endx = 1;
					endz = 2 + (thundernum - 1) * 3 * (1);
					explosionloc = centerofblock.add(0, 0, 1 + (thundernum - 1) * 3 * (1));
					break;
				case "W":
					//西を向いているとき
					startx = -2 + (thundernum - 1) * 3 * (-1);
					startz = -1;
					endx = 0 + (thundernum - 1) * 3 * (-1);
					endz = 1;
					explosionloc = centerofblock.add(-1 + (thundernum - 1) * 3 * (-1), 0, 0);
					break;
				case "U":
					//上を向いているとき
					startx = -1;
					starty = 0 + (thundernum - 1) * 3 * (1);
					startz = -1;
					endx = 1;
					endy = 2 + (thundernum - 1) * 3 * (1);
					endz = 1;
					explosionloc = centerofblock.add(0, 1 + (thundernum - 1) * 3 * (1), 0);
					break;
				case "D":
					//下を向いているとき
					startx = -1;
					starty = -2 + (thundernum - 1) * 3 * (-1);
					startz = -1;
					endx = 1;
					endy = 0 + (thundernum - 1) * 3 * (-1);
					endz = 1;
					explosionloc = centerofblock.add(0, -1 + (thundernum - 1) * 3 * (-1), 0);
					break;
			}

			if(!expman.hasExp(8)){
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
				}
				cancel();
				//フラグ折る
				playerdata.skillflag = false;
				return;
			}

			int count = 0;
			for(int x = startx ; x <= endx ; x++){
				for(int z = startz ; z <= endz ; z++){
					for(int y = starty; y <= endy ; y++){
						if(x==0&&y==0&&z==0){
							continue;
						}
						breakblock = block.getRelative(x, y, z);
						//もし壊されるブロックがもともとのブロックと同じ種類だった場合
						if(breakblock.getType().equals(material)
								|| (material.equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
								|| (material.equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))
								|| breakblock.getType().equals(Material.LAVA)
								){
							if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking()){
								if(PlayerBlockBreakListener.canBreak(player, breakblock)){
									if(breakblock.getType().equals(Material.LAVA)){
										breakblock.setType(Material.AIR);
									}else{
										//アクティブスキル発動
										PlayerBlockBreakListener.BreakBlock(player, breakblock, centerofblock, tool,false);
										count ++;
									}
								}
							}
						}
					}

				}
			}
			if(count > 0){
				block.getWorld().createExplosion(explosionloc, 0, false);
			}
			if(thundernum == 1){
				block.getWorld().spigot().strikeLightningEffect(explosionloc,true);
			}

			if(count>21){
				expman.changeExp(-8);
			}else if(count>14){
				expman.changeExp(-6);
			}else if(count>7){
				expman.changeExp(-4);
			}else if(count>0){
				expman.changeExp(-2);
			}
		}
	}
}
