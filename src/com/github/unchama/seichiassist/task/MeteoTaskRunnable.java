package com.github.unchama.seichiassist.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.listener.PlayerBlockBreakListener;
import com.github.unchama.seichiassist.util.ExperienceManager;

public class MeteoTaskRunnable extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	UUID uuid;
	PlayerData playerdata;
	private Player player;
	private Block block;
	private ItemStack tool;
	private ExperienceManager expman;
	private boolean meteoflag;
	private int playerlocy;
	private String dir;
	private Material material;
	private Location centerofblock;
	private boolean sneakflag;
	//壊されるブロックの宣言
	Block breakblock;
	int startx;
	int starty;
	int startz;
	int endx;
	int endy;
	int endz;
	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public MeteoTaskRunnable(Player player,Block block,ItemStack tool, ExperienceManager expman) {
		this.player = player;
		this.block = block;
		this.tool = tool;
		this.expman = expman;
		meteoflag = false;//凍っているかどうか
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
		//メテオ発射音を鳴らす
		player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
		//クールダウンタイム生成
		new CoolDownTaskRunnable(player).runTaskLater(plugin,50);
	}
	@Override
	public void run() {
		/*
		 * ツール所持&耐久値確認処理ここから
		 */
		//プレイヤーインベントリを取得
		PlayerInventory inventory = player.getInventory();
		//メインハンドとオフハンドを取得
		ItemStack mainhanditem = inventory.getItemInMainHand();
		ItemStack offhanditem = inventory.getItemInOffHand();
		//実際に使用するツールを格納する
		ItemStack tool = null;
		//メインハンドにツールがあるか
		boolean mainhandtoolflag = SeichiAssist.breakmateriallist.contains(mainhanditem.getType());
		//オフハンドにツールがあるか
		boolean offhandtoolflag = SeichiAssist.breakmateriallist.contains(offhanditem.getType());

		//場合分け
		if(mainhandtoolflag){
			//メインハンドの時
			tool = mainhanditem;
		}else if(offhandtoolflag){
			//サブハンドの時
			//フラグ折る
			playerdata.skillflag = false;
			cancel();
			return;
		}else{
			//どちらにももっていない時処理を終了
			//フラグ折る
			playerdata.skillflag = false;
			cancel();
			return;
		}
		//耐久値がマイナスかつ耐久無限ツールでない時処理を終了
		if(tool.getDurability() > tool.getType().getMaxDurability() && !tool.getItemMeta().spigot().isUnbreakable()){
			//フラグ折る
			playerdata.skillflag = false;
			cancel();
			return;
		}
		/*
		 * ここまで
		 */

		if(meteoflag){
			//フラグ折っとく
			playerdata.skillflag = false;
			cancel();
			startx = 0;
			starty = -1;
			startz = 0;
			endx = 0;
			endy = +5;
			endz = 0;
			List<Location> explosionlist = new ArrayList<Location>();
			switch (dir){
				case "N":
					//北を向いているとき
					startx = -4;
					startz = -8;
					endx = 4;
					endz = 0;
					for(int x = -3; x<=3;x+=3){
						for(int z = -7;z<=1;z+=3){
							for(int y = 0;y<=4;y+=2){
								explosionlist.add(new Location(player.getWorld(), centerofblock.getX()+x, centerofblock.getY()+y,centerofblock.getZ()+z));
							}
						}
					}
					break;
				case "E":
					//東を向いているとき
					startx = 0;
					startz = -4;
					endx = 8;
					endz = 4;
					for(int x = 1; x<=7;x+=3){
						for(int z = -3;z<=3;z+=3){
							for(int y = 0;y<=4;y+=2){
								explosionlist.add(new Location(player.getWorld(), centerofblock.getX()+x, centerofblock.getY()+y,centerofblock.getZ()+z));
							}
						}
					}
					break;
				case "S":
					//南を向いているとき
					startx = -4;
					startz = 0;
					endx = 4;
					endz = 8;
					for(int x = -3; x<=3;x+=3){
						for(int z = 1;z<=7;z+=3){
							for(int y = 0;y<=4;y+=2){
								explosionlist.add(new Location(player.getWorld(), centerofblock.getX()+x, centerofblock.getY()+y,centerofblock.getZ()+z));
							}
						}
					}
					break;
				case "W":
					//西を向いているとき
					startx = -8;
					startz = -4;
					endx = 0;
					endz = 4;
					for(int x = -7; x<=1;x+=3){
						for(int z = -3;z<=3;z+=3){
							for(int y = 0;y<=4;y+=2){
								explosionlist.add(new Location(player.getWorld(), centerofblock.getX()+x, centerofblock.getY()+y,centerofblock.getZ()+z));
							}
						}
					}
					break;
				case "U":
					//上を向いているとき
					startx = -4;
					starty = 0;
					startz = -4;
					endx = 4;
					endy = 6;
					endz = 4;
					for(int x = -3; x<=3;x+=3){
						for(int z = -3;z<=3;z+=3){
							for(int y = 1;y<=5;y+=2){
								explosionlist.add(new Location(player.getWorld(), centerofblock.getX()+x, centerofblock.getY()+y,centerofblock.getZ()+z));
							}
						}
					}
					break;
				case "D":
					//下を向いているとき
					startx = -4;
					starty = -6;
					startz = -4;
					endx = 4;
					endy = 0;
					endz = 4;
					for(int x = -3; x<=3;x+=3){
						for(int z = -3;z<=3;z+=3){
							for(int y = -5;y<=1;y+=2){
								explosionlist.add(new Location(player.getWorld(), centerofblock.getX()+x, centerofblock.getY()+y,centerofblock.getZ()+z));
							}
						}
					}
					break;
			}

			if(!expman.hasExp(100)){
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
							if(playerlocy < breakblock.getLocation().getBlockY() || sneakflag){
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
				player.getWorld().playEffect(player.getLocation(), Effect.WITHER_BREAK_BLOCK, 1);
				for(Location loc : explosionlist){
					block.getWorld().createExplosion(loc,0,false);
					block.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
				}
			}

			int max = 500;
			int exp = 100;
			for(int n = max ; n > 0 ; n -= 5){
				if(count > n){
					expman.changeExp(-exp);
					break;
				}else{
					exp--;
				}
			}


		}else{
			meteoflag = true;
			if(player.isSneaking()){
				sneakflag = true;
			}
			if(!expman.hasExp(100)){
				//デバッグ用
				if(SeichiAssist.DEBUG){
					player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要な経験値が足りません");
				}
				cancel();
				//フラグ折っとく
				playerdata.skillflag = false;
				return;
			}
			//blockの位置を取得
			Location loc = block.getLocation().add(0.5, 0.5, 0.5);
			//プレイヤーの位置を取得
			Location ploc = player.getLocation();
			//メテオの発射位置を決定
			Location mloc = ploc.add(0,60,0);
			double speed = 0.0001;
			//メテオ発射
			Vector vec = new Vector(loc.getX()-mloc.getX(),loc.getY()-mloc.getY(),loc.getZ()-mloc.getZ());
			vec.multiply(speed);
			//Vector vec = new Vector(0,-1,0);
			final LargeFireball meteo = loc.getWorld().spawn(mloc, LargeFireball.class);
			meteo.setDirection(vec);
			meteo.setVelocity(vec);
		}
	}
}
