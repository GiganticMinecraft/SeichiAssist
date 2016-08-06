package com.github.unchama.seichiassist.task;

import java.util.Random;

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
import com.github.unchama.seichiassist.listener.PlayerBlockBreakListener;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;

public class IllusionTaskRunnable extends BukkitRunnable{
	private Player player;
	private Block block;
	private ItemStack tool;
	private ExperienceManager expman;
	private int illusionnum;
	private int playerlocy;
	private String dir;
	private Material material;
	private Location centerofblock;
	Random rand;
	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public IllusionTaskRunnable(Player player,Block block,ItemStack tool, ExperienceManager expman) {
		this.player = player;
		this.block = block;
		this.tool = tool;
		this.expman = expman;
		illusionnum = 0;//実行回数
		//プレイヤーの足のy座標を取得
		playerlocy = player.getLocation().getBlockY() - 1 ;
		//プレイヤーの向いている方角を取得
		dir = PlayerBlockBreakListener.getCardinalDirection(player);
		//元ブロックのマテリアルを取得
		material = block.getType();
		//元ブロックの真ん中の位置を取得
		centerofblock = block.getLocation().add(0.5, 0.5, 0.5);
		//ランダム値を生成
		 rand = new Random();


	}
	@Override
	public void run() {
		if(illusionnum > 9){
			cancel();
		}else{
			illusionnum++;

			//壊されるブロックの宣言
			Block breakblock;
			int startx = 0;
			int starty = -1;
			int startz = 0;
			int endx = 0;
			int endy = +15;
			int endz = 0;
			player.sendMessage("" + illusionnum);
			switch (dir){
				case "N":
					//北を向いているとき
					startx = -8;
					startz = -16;
					endx = 8;
					endz = 0;
					break;
				case "E":
					//東を向いているとき
					startx = 0;
					startz = -8;
					endx = 16;
					endz = 8;
					break;
				case "S":
					//南を向いているとき
					startx = -8;
					startz = 0;
					endx = 8;
					endz = 16;
					break;
				case "W":
					//西を向いているとき
					startx = -16;
					startz = -8;
					endx = 0;
					endz = 8;
					break;
				case "U":
					//上を向いているとき
					startx = -8;
					starty = 0;
					startz = -8;
					endx = 8;
					endy = 16;
					endz = 8;
					break;
				case "D":
					//下を向いているとき
					startx = -8;
					starty = -16;
					startz = -8;
					endx = 8;
					endy = 0;
					endz = 8;
					break;
			}

			if(!expman.hasExp(5)){
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
				}
				cancel();
				return;
			}

			int count = 0;
			for(int x = startx ; x <= endx ; x++){
				for(int z = startz ; z <= endz ; z++){
					for(int y = starty; y <= endy ; y++){
						breakblock = block.getRelative(x, y, z);
						if(x==0&&y==0&&z==0){
							if(illusionnum != 1){
								continue;
							}else{
								//壊されるブロックがワールドガード範囲だった場合処理を終了
								if(!Util.getWorldGuard().canBuild(player, breakblock.getLocation())){
									player.sendMessage(ChatColor.RED + "ワールドガードで保護されています。");
									return;
								}
								breakblock.setType(material);
							}
						}
						//もし壊されるブロックがもともとのブロックと同じ種類だった場合
						if(breakblock.getType().equals(material)
								|| (material.equals(Material.DIRT)&&breakblock.getType().equals(Material.GRASS))
								|| (material.equals(Material.GRASS)&&breakblock.getType().equals(Material.DIRT))){
							if(playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking()){
								if(PlayerBlockBreakListener.canBreak(player, breakblock)){
									if(rand.nextDouble() > 0.9898229){
										//アクティブスキル発動
										PlayerBlockBreakListener.BreakBlock(player, breakblock, centerofblock, tool);
										player.getWorld().playEffect(breakblock.getLocation(), Effect.COLOURED_DUST, 1);
										player.getWorld().playSound(breakblock.getLocation(), Sound.BLOCK_NOTE_HARP, 1, (float) 17);
										count ++;
									}
								}
							}
						}
					}

				}
			}

			if(count>25){
				expman.changeExp(-5);
			}else if(count>20){
				expman.changeExp(-4);
			}else if(count>15){
				expman.changeExp(-3);
			}else if(count>10){
				expman.changeExp(-2);
			}else if(count>5){
				expman.changeExp(-1);
			}
		}
	}
}
