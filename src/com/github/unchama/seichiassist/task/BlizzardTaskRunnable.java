package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.listener.PlayerBlockBreakListener;
import com.github.unchama.seichiassist.util.ExperienceManager;

public class BlizzardTaskRunnable extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	UUID uuid;
	PlayerData playerdata;
	private Player player;
	private Block block;
	private ItemStack tool;
	private ExperienceManager expman;
	private boolean frozenflag;
	private int playerlocy;
	private String dir;
	private Material material;
	private Location centerofblock;
	//壊されるブロックの宣言
	Block breakblock;
	int startx;
	int starty;
	int startz;
	int endx;
	int endy;
	int endz;
	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public BlizzardTaskRunnable(Player player,Block block,ItemStack tool, ExperienceManager expman) {
		this.player = player;
		this.block = block;
		this.tool = tool;
		this.expman = expman;
		frozenflag = false;//凍っているかどうか
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
		new CoolDownTaskRunnable(player).runTaskLater(plugin,50);
	}
	@Override
	public void run() {
		if(frozenflag){
			//フラグ折る
			playerdata.skillflag = false;
			cancel();
			for(int x = startx ; x <= endx ; x++){
				for(int z = startz ; z <= endz ; z++){
					for(int y = starty; y <= endy ; y++){
						if(x==0&&y==0&&z==0){
							continue;
						}
						breakblock = block.getRelative(x, y, z);
						//もし壊されるブロックがもともとのブロックと同じ種類だった場合
						if(breakblock.getType().equals(Material.PACKED_ICE)){
							if(PlayerBlockBreakListener.canBreak(player, breakblock)){
								//ブロックを空気に変える
								breakblock.setType(Material.AIR);
								//あたかもプレイヤーが壊したかのようなエフェクトを表示させる
								player.getWorld().playEffect(breakblock.getLocation().add(0.5,0.5,0.5), Effect.STEP_SOUND,Material.PACKED_ICE,6);
							}

						}
					}

				}
			}

		}else{
			frozenflag = true;
			startx = 0;
			starty = -1;
			startz = 0;
			endx = 0;
			endy = +3;
			endz = 0;

			switch (dir){
				case "N":
					//北を向いているとき
					startx = -3;
					startz = -6;
					endx = 3;
					endz = 0;
					break;
				case "E":
					//東を向いているとき
					startx = 0;
					startz = -3;
					endx = 6;
					endz = 3;
					break;
				case "S":
					//南を向いているとき
					startx = -3;
					startz = 0;
					endx = 3;
					endz = 6;
					break;
				case "W":
					//西を向いているとき
					startx = -6;
					startz = -3;
					endx = 0;
					endz = 3;
					break;
				case "U":
					//上を向いているとき
					startx = -3;
					starty = 0;
					startz = -3;
					endx = 3;
					endy = 4;
					endz = 3;
					break;
				case "D":
					//下を向いているとき
					startx = -3;
					starty = -4;
					startz = -3;
					endx = 3;
					endy = 0;
					endz = 3;
					break;
			}

			if(!expman.hasExp(70)){
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
										player.getWorld().playEffect(breakblock.getLocation().add(0.5,0.5,0.5), Effect.SNOWBALL_BREAK, 1);
										player.getWorld().playEffect(breakblock.getLocation().add(0.5,0.5,0.5), Effect.STEP_SOUND,breakblock.getType());
										count ++;
									}
									breakblock.setType(Material.PACKED_ICE);

								}
							}
						}
					}

				}
			}
			if(count > 0){
				block.getWorld().playSound(centerofblock, Sound.ENTITY_POLAR_BEAR_AMBIENT, 1, (float) 1.2);
			}

			int max = 210;
			int exp = 70;
			for(int n = max ; n > 0 ; n -= 3){
				if(count > n){
					expman.changeExp(-exp);
					break;
				}else{
					exp--;
				}
			}
		}
	}
}
