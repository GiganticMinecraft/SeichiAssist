package com.github.unchama.seichiassist.util;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.coreprotect.CoreProtectAPI;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.BreakArea;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;

public class BreakUtil {
	//他のプラグインの影響があってもブロックを破壊できるのか
	public static boolean canBreak(Player player ,Block breakblock) {
		if(!player.isOnline() || breakblock == null){
			return false;
		}

		//壊されるブロックがワールドガード範囲だった場合処理を終了
		if(!Util.getWorldGuard().canBuild(player, breakblock.getLocation())){
			player.sendMessage(ChatColor.RED + "ワールドガードで保護されています。");
			return false;
		}

		BreakUtil.logRemove(player,breakblock);


		return true;
	}
	public static boolean equalignoreWorld(String name) {
		List<String> ignoreworldlist = SeichiAssist.ignoreWorldlist;
		for(String s : ignoreworldlist){
			if(name.equalsIgnoreCase(s.toLowerCase())){
				return true;
			}
		}
		return false;
	}
	//ブロックを破壊する処理、ドロップも含む、統計増加も含む
	public static void BreakBlock(Player player,Block breakblock,Location centerofblock,ItemStack tool,Boolean stepflag) {

		Material material = breakblock.getType();
		if(!SeichiAssist.materiallist.contains(material)){
			return;
		}

		ItemStack itemstack = dropItemOnTool(breakblock,tool);


		if(material.equals(Material.GLOWING_REDSTONE_ORE)){
			material = Material.REDSTONE_ORE;
		}
		if(material.equals(Material.AIR)){
			return;
		}

		if(itemstack != null){
			//アイテムをドロップさせる
			if(!addItemtoMineStack(player,itemstack)){
				breakblock.getWorld().dropItemNaturally(centerofblock,itemstack);
			}
		}

		//ブロックを空気に変える
		breakblock.setType(Material.AIR);

		if(stepflag){
			//あたかもプレイヤーが壊したかのようなエフェクトを表示させる、壊した時の音を再生させる
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND,material);
		}
		//プレイヤーの統計を１増やす
		player.incrementStatistic(Statistic.MINE_BLOCK, material);

	}

	public static boolean addItemtoMineStack(Player player, ItemStack itemstack) {
		SeichiAssist plugin = SeichiAssist.plugin;
		HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
		Config config = SeichiAssist.config;
		//もしサバイバルでなければ処理を終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return false;
		}
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[PickupItem処理]でエラー発生");
			plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return false;
		}
		//レベルが足りない場合処理終了
		if(playerdata.level < config.getMineStacklevel(1)){
			return false;
		}
		//minestackflagがfalseの時は処理を終了
		if(!playerdata.minestackflag){
			return false;
		}

		int amount = itemstack.getAmount();
		Material material = itemstack.getType();

		int v1 = config.getMineStacklevel(1);
		int v2 = config.getMineStacklevel(2);
		int v3 = config.getMineStacklevel(3);
		int v4 = config.getMineStacklevel(4);
		int v5 = config.getMineStacklevel(5);
		int v6 = config.getMineStacklevel(6);
		int v7 = config.getMineStacklevel(7);
		int v8 = config.getMineStacklevel(8);
		int v9 = config.getMineStacklevel(9);
		int v10 = config.getMineStacklevel(10);


		switch(material){
			case DIRT:
				if(playerdata.level < v1){
					return false;
				}
				playerdata.minestack.dirt += amount;
				break;
			case GRASS:
				if(playerdata.level < v1){
					return false;
				}
				playerdata.minestack.grass += amount;
				break;
			case GRAVEL:
				if(playerdata.level < v2){
					return false;
				}
				playerdata.minestack.gravel += amount;
				break;
			case COBBLESTONE:
				if(playerdata.level < v3){
					return false;
				}
				playerdata.minestack.cobblestone += amount;
				break;
			case STONE:
				if(playerdata.level < v3){
					return false;
				}
				playerdata.minestack.stone += amount;
				break;
			case SAND:
				if(playerdata.level < v4){
					return false;
				}
				playerdata.minestack.sand += amount;
				break;
			case PACKED_ICE:
				if(playerdata.level < v4){
					return false;
				}
				playerdata.minestack.packed_ice += amount;
				break;
			case SANDSTONE:
				if(playerdata.level < v4){
					return false;
				}
				playerdata.minestack.sandstone += amount;
				break;
			case NETHERRACK:
				if(playerdata.level < v5){
					return false;
				}
				playerdata.minestack.netherrack += amount;
				break;
			case SOUL_SAND:
				if(playerdata.level < v6){
					return false;
				}
				playerdata.minestack.soul_sand += amount;
				break;
			case MAGMA:
				if(playerdata.level < v6){
					return false;
				}
				playerdata.minestack.magma += amount;
				break;
			case ENDER_STONE:
				if(playerdata.level < v7){
					return false;
				}
				playerdata.minestack.ender_stone += amount;
				break;
			case COAL:
				if(playerdata.level < v8){
					return false;
				}
				playerdata.minestack.coal += amount;
				break;
			case COAL_ORE:
				if(playerdata.level < v8){
					return false;
				}
				playerdata.minestack.coal_ore += amount;
				break;
			case IRON_ORE:
				if(playerdata.level < v9){
					return false;
				}
				playerdata.minestack.iron_ore += amount;
				break;
			case QUARTZ:
				if(playerdata.level < v10){
					return false;
				}
				playerdata.minestack.quartz += amount;
				break;
			case QUARTZ_ORE:
				if(playerdata.level < v10){
					return false;
				}
				playerdata.minestack.quartz_ore += amount;
				break;
			default:
				return false;
		}
		//player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, (float)0.1, (float)1);
		return true;

	}
	@SuppressWarnings("deprecation")
	public static ItemStack dropItemOnTool(Block breakblock, ItemStack tool) {
		ItemStack dropitem = null;
		Material dropmaterial;
		Material breakmaterial = breakblock.getType();
		int fortunelevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
		double rand = Math.random();
        int bonus = (int) (rand * ((fortunelevel + 2)) - 1);
        if (bonus <= 1) {
            bonus = 1;
        }
        byte b = breakblock.getData();
        byte b_tree = b;
        b_tree &= 0x03;
        b &= 0x0F;


		int silktouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH);
		if(silktouch > 0){
			//シルクタッチの処理
			switch(breakmaterial){
			case GLOWING_REDSTONE_ORE:
				dropmaterial = Material.REDSTONE_ORE;
				dropitem = new ItemStack(dropmaterial);
				break;
			case LOG:
			case LOG_2:
			case LEAVES:
			case LEAVES_2:
				dropitem = new ItemStack(breakmaterial,1,b_tree);
				break;
			case MONSTER_EGGS:
				dropmaterial = Material.STONE;
				dropitem = new ItemStack(dropmaterial);
				break;
			default:
				dropitem = new ItemStack(breakmaterial,1,b);
				break;
			}

		}else if(fortunelevel > 0 && SeichiAssist.luckmateriallist.contains(breakmaterial)){
			//幸運の処理
			switch(breakmaterial){
				case COAL_ORE:
					dropmaterial = Material.COAL;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case DIAMOND_ORE:
					dropmaterial = Material.DIAMOND;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case LAPIS_ORE:
					Dye dye = new Dye();
					dye.setColor(DyeColor.BLUE);

					bonus *= (rand * 4) + 4;
					dropitem = dye.toItemStack(bonus);
					break;
				case EMERALD_ORE:
					dropmaterial = Material.EMERALD;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					bonus *= rand + 4;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case GLOWING_REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					bonus *= rand + 4;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case QUARTZ_ORE:
					dropmaterial = Material.QUARTZ;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				default:
					break;
			}
		}else{
			//シルク幸運なしの処理
			switch(breakmaterial){
				case COAL_ORE:
					dropmaterial = Material.COAL;
					dropitem = new ItemStack(dropmaterial);
					break;
				case DIAMOND_ORE:
					dropmaterial = Material.DIAMOND;
					dropitem = new ItemStack(dropmaterial);
					break;
				case LAPIS_ORE:
					Dye dye = new Dye();
					dye.setColor(DyeColor.BLUE);
					dropitem = dye.toItemStack((int) ((rand*4) + 4));
					break;
				case EMERALD_ORE:
					dropmaterial = Material.EMERALD;
					dropitem = new ItemStack(dropmaterial);
					break;
				case REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial,(int) (rand+4));
					break;
				case GLOWING_REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial,(int) (rand+4));
					break;
				case QUARTZ_ORE:
					dropmaterial = Material.QUARTZ;
					dropitem = new ItemStack(dropmaterial);
					break;
				case STONE:
					//Material.STONEの処理
					if(breakblock.getData() == 0x00){
						//焼き石の処理
						dropmaterial = Material.COBBLESTONE;
						dropitem = new ItemStack(dropmaterial);
					}else{
						//他の石の処理
						dropitem = new ItemStack(breakmaterial,1,b);
					}
					break;
				case GRASS:
					//芝生の処理
					dropmaterial = Material.DIRT;
					dropitem = new ItemStack(dropmaterial);
					break;
				case GRAVEL:
					double p = 0;
					switch(fortunelevel){
					case 1:
						p = 0.14;
						break;
					case 2:
						p = 0.25;
						break;
					case 3:
						p = 1.00;
						break;
					default :
						p = 0.1;
						break;
					}
					if(p>rand){
						dropmaterial = Material.FLINT;
					}else{
						dropmaterial = Material.GRAVEL;
					}
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case LEAVES:
				case LEAVES_2:
					dropitem = null;
					break;
				case CLAY:
					dropmaterial = Material.CLAY_BALL;
					dropitem = new ItemStack(dropmaterial,4);
					break;
				case MONSTER_EGGS:
					Location loc = breakblock.getLocation();
					breakblock.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
					dropitem = null;
					break;
				case LOG:
				case LOG_2:
					dropitem = new ItemStack(breakmaterial,1,b_tree);
					break;
				default:
					//breakblcokのままのアイテムスタックを保存
					dropitem = new ItemStack(breakmaterial,1,b);
					break;
			}
		}
		return dropitem;
	}
/*マナ追加のためいったん消えてもらおう
	//追加経験値の設定
	public static int calcExpDrop(PlayerData playerdata) {
		//０～１のランダムな値を取得
		double rand = Math.random();
		//10%の確率で経験値付与
		if(rand < 0.1){
			//Lv8未満は獲得経験値ゼロ、それ以上はレベルに応じて経験値付与
			if(playerdata.level < 8 || playerdata.activeskilldata.skillcanbreakflag == false){
				return 0;
			}else if (playerdata.level < 18){
				return SeichiAssist.config.getDropExplevel(1);
			}else if (playerdata.level < 28){
				return SeichiAssist.config.getDropExplevel(2);
			}else if (playerdata.level < 38){
				return SeichiAssist.config.getDropExplevel(3);
			}else if (playerdata.level < 48){
				return SeichiAssist.config.getDropExplevel(4);
			}else if (playerdata.level < 58){
				return SeichiAssist.config.getDropExplevel(5);
			}else if (playerdata.level < 68){
				return SeichiAssist.config.getDropExplevel(6);
			}else if (playerdata.level < 78){
				return SeichiAssist.config.getDropExplevel(7);
			}else if (playerdata.level < 88){
				return SeichiAssist.config.getDropExplevel(8);
			}else if (playerdata.level < 98){
				return SeichiAssist.config.getDropExplevel(9);
			}else{
				return SeichiAssist.config.getDropExplevel(10);
			}
		}else{
			return 0;
		}
	}
	*/
	public static double calcManaDrop(PlayerData playerdata) {
		//０～１のランダムな値を取得
		double rand = Math.random();
		//10%の確率で経験値付与
		if(rand < 0.1){
			//Lv8未満は獲得経験値ゼロ、それ以上はレベルに応じて経験値付与
			if(playerdata.level < 8 || playerdata.activeskilldata.skillcanbreakflag == false){
				return 0;
			}else if (playerdata.level < 18){
				return SeichiAssist.config.getDropExplevel(1);
			}else if (playerdata.level < 28){
				return SeichiAssist.config.getDropExplevel(2);
			}else if (playerdata.level < 38){
				return SeichiAssist.config.getDropExplevel(3);
			}else if (playerdata.level < 48){
				return SeichiAssist.config.getDropExplevel(4);
			}else if (playerdata.level < 58){
				return SeichiAssist.config.getDropExplevel(5);
			}else if (playerdata.level < 68){
				return SeichiAssist.config.getDropExplevel(6);
			}else if (playerdata.level < 78){
				return SeichiAssist.config.getDropExplevel(7);
			}else if (playerdata.level < 88){
				return SeichiAssist.config.getDropExplevel(8);
			}else if (playerdata.level < 98){
				return SeichiAssist.config.getDropExplevel(9);
			}else{
				return SeichiAssist.config.getDropExplevel(10);
			}
		}else{
			return 0;
		}
	}
	//num回だけ耐久を減らす処理
	public static short calcDurability(int enchantmentLevel,int num) {
		Random rand = new Random();
		short durability = 0;
		double probability = 1.0 / (enchantmentLevel + 1.0);

		for(int i = 0; i < num ; i++){
			if(probability >  rand.nextDouble() ){
				durability++;
			}
		}
		return durability;
	}

	public static String getCardinalDirection(Entity entity) {
		double rotation = (entity.getLocation().getYaw() + 180) % 360;
		Location loc = entity.getLocation();
		float pitch = loc.getPitch();
		if (rotation < 0) {
		rotation += 360.0;
		}

		if(pitch <= -30){
			return "U";
		}else if(pitch >= 25){
			return "D";
		}else if (0 <= rotation && rotation < 45.0) {
			return "N";
		}else if (45.0 <= rotation && rotation < 135.0) {
			return "E";
		}else if (135.0 <= rotation && rotation < 225.0) {
			return "S";
		}else if (225.0 <= rotation && rotation < 315.0) {
			return "W";
		}else if (315.0 <= rotation && rotation < 360.0) {
		return "N";
		} else {
		return null;
		}
	}
	public static double getGravity(BreakArea area, Block block, int weight) {
		int gravity = 1;
		Coordinate start = area.getAllStart();
		Coordinate end = area.getAllEnd();
		Block calcblock;

		for(int x = start.x ; x <= end.x ; x++){
			for(int z = start.z ; z <= end.z ; z++){
				for(int y = end.y + 1; y <= end.y + 11; y++){
					//最大10ブロック分
					calcblock = block.getRelative(x, y, z);
					if(!SeichiAssist.gravitymateriallist.contains(calcblock.getType()) &&
						!SeichiAssist.cancelledmateriallist.contains(calcblock.getType()) &&
						!SeichiAssist.transparentmateriallist.contains(calcblock.getType())){
						//もし透過許可ブロックに入っていないブロックだった場合重力値を加算
						gravity++;
					}
				}
			}
		}
		//重力値に重みづけ
		gravity = gravity*weight;
		//１より小さくなった場合は１に変更
		if(gravity < 1)gravity = 1;
		return gravity;
	}
	@SuppressWarnings("deprecation")
	public static boolean logRemove(Player player, Block breakblock) {
		//設置するブロックの状態を取得
		BlockState blockstate = breakblock.getState();
		//設置するブロックのデータを取得
		byte data = blockstate.getData().getData();

		if(!equalignoreWorld(player.getWorld().getName()) || breakblock.getY() < 8){
			//コアプロテクトのクラスを取得
			CoreProtectAPI CoreProtect = Util.getCoreProtect();
			//破壊ログを設定
			Boolean success = CoreProtect.logRemoval(player.getName(), breakblock.getLocation(), blockstate.getType(),data);
			//もし失敗したらプレイヤーに報告し処理を終了
			if(!success){
				player.sendMessage(ChatColor.RED + "coreprotectに保存できませんでした。管理者に報告してください。");
				return false;
			}
		}
		return true;
	}
	@SuppressWarnings("deprecation")
	public static boolean logPlace(Player player, Block placeblock) {
		//設置するブロックの状態を取得
		BlockState blockstate = placeblock.getState();
		//設置するブロックのデータを取得
		byte data = blockstate.getData().getData();

		if(!equalignoreWorld(player.getWorld().getName()) || placeblock.getY() < 8){
			//コアプロテクトのクラスを取得
			CoreProtectAPI CoreProtect = Util.getCoreProtect();
			//破壊ログを設定
			Boolean success = CoreProtect.logPlacement(player.getName(), placeblock.getLocation(), blockstate.getType(),data);
			//もし失敗したらプレイヤーに報告し処理を終了
			if(!success){
				player.sendMessage(ChatColor.RED + "coreprotectに保存できませんでした。管理者に報告してください。");
				return false;
			}
		}
		return true;
	}
}
