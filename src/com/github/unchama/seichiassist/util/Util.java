package com.github.unchama.seichiassist.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Dye;
import org.bukkit.plugin.Plugin;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Util {
	static private FireworkEffect.Type[] types = { FireworkEffect.Type.BALL,
		FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.BURST,
		FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR, };
	public static ItemStack getskull(String name){
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skull.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券");
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで使えます"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者:" + name);
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}
	public static ItemStack getInventoryOpenItem(String name){
		ItemStack endframe = new ItemStack(Material.ENDER_PORTAL_FRAME,1);
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_PORTAL_FRAME);
		itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "4次元ポケット");
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで開けます"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者:" + name);
		itemmeta.setLore(lore);
		endframe.setItemMeta(itemmeta);
		return endframe;
	}
	public static int getOnlinePlayer(){
		return Bukkit.getOnlinePlayers().size();
	}
	public static double toDouble(String s){
		return Double.parseDouble(s);
	}
	public static int toInt(String s) {
		return Integer.parseInt(s);
	}
	public static double Decimal(double d) {
		BigDecimal bi = new BigDecimal(String.valueOf(d));
		return bi.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	public static boolean isPlayerInventryFill(Player player){
		return (player.getInventory().firstEmpty() == -1);
	}
	public static void dropItem(Player player,ItemStack itemstack){
		player.getWorld().dropItemNaturally(player.getLocation(), itemstack);
	}
	public static void addItem(Player player,ItemStack itemstack){
		player.getInventory().addItem(itemstack);
	}

	public static void sendAdminMessage(String str){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			if(player.hasPermission("SeichiAssist.admin")){
				player.sendMessage(str);
			}
		}
	}


	public static void sendEveryMessage(String str){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			player.sendMessage(str);
		}
	}
	public static void sendEverySound(Sound str, float a, float b){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			player.playSound(player.getLocation(), str, a, b);
		}
	}

	public static int toTickSecond(int _tick){
		return _tick/20;
	}

	public static String toTimeString(int _second) {
		int second = _second;
		int minute = 0;
		int hour = 0;
		String time = "";
		while(second >= 60){
			second -=60;
			minute++;
		}
		while(minute >= 60){
			minute -= 60;
			hour++;
		}
		if(hour != 0){
			time = hour + "時間";
		}
		if(minute != 0){
			time = time + minute + "分";
		}
		/*
		if(second != 0){
			time = time + second + "秒";
		}
		*/
		return time;
	}

	//プレイヤーネームを格納（toLowerCaseで全て小文字にする。)
	public static String getName(Player p) {
		return p.getName().toLowerCase();
	}
	public static String getName(String name) {
		//小文字にしてるだけだよ
		return name.toLowerCase();
	}
	public static void launchFireWorks(Location loc) {
		// 花火を作る
		Firework firework = loc.getWorld().spawn(loc, Firework.class);

		// 花火の設定情報オブジェクトを取り出す
		FireworkMeta meta = firework.getFireworkMeta();
		Builder effect = FireworkEffect.builder();
		Random rand = new Random();

		// 形状をランダムに決める
		effect.with(types[rand.nextInt(types.length)]);

		// 基本の色を単色～5色以内でランダムに決める
		effect.withColor(getRandomCrolors(1 + rand.nextInt(5)));

		// 余韻の色を単色～3色以内でランダムに決める
		effect.withFade(getRandomCrolors(1 + rand.nextInt(3)));

		// 爆発後に点滅するかをランダムに決める
		effect.flicker(rand.nextBoolean());

		// 爆発後に尾を引くかをランダムに決める
		effect.trail(rand.nextBoolean());

		// 打ち上げ高さを1以上4以内でランダムに決める
		meta.setPower(1 + rand.nextInt(4));

		// 花火の設定情報を花火に設定
		meta.addEffect(effect.build());
		firework.setFireworkMeta(meta);

	}
	public static Color[] getRandomCrolors(int length) {
		// 配列を作る
		Color[] colors = new Color[length];
		Random rand = new Random();
		// 配列の要素を順に処理していく
		for (int n = 0; n != length; n++) {
			// 24ビットカラーの範囲でランダムな色を決める
			colors[n] = Color.fromBGR(rand.nextInt(1 << 24));
		}

		// 配列を返す
		return colors;
	}
	public static CoreProtectAPI getCoreProtect() {
		Plugin plugin = SeichiAssist.plugin.getServer().getPluginManager().getPlugin("CoreProtect");

		// Check that CoreProtect is loaded
		if (plugin == null || !(plugin instanceof CoreProtect)) {
		    return null;
		}

		// Check that the API is enabled
		CoreProtectAPI CoreProtect = ((CoreProtect)plugin).getAPI();
		if (CoreProtect.isEnabled()==false){
		    return null;
		}

		// Check that a compatible version of the API is loaded
		if (CoreProtect.APIVersion() < 4){
		    return null;
		}

		return CoreProtect;
	}
	public static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = SeichiAssist.plugin.getServer().getPluginManager().getPlugin("WorldGuard");

	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }

	    return (WorldGuardPlugin) plugin;
	}
	public static WorldEditPlugin getWorldEdit() {
        Plugin pl = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if(pl instanceof WorldEditPlugin)
            return (WorldEditPlugin)pl;
        else return null;
    }
	public static boolean containsGacha(Player player) {
		org.bukkit.inventory.ItemStack[] inventory = player.getInventory().getStorageContents();
		Material material;
		SkullMeta skullmeta;
		for (int i = 0; i < inventory.length; i++) {
			material = inventory[i].getType();
			if(material.equals(Material.SKULL_ITEM)){
				skullmeta = (SkullMeta)inventory[i].getItemMeta();
				if(skullmeta.hasOwner()){
					if(skullmeta.getOwner().equals("unchama")){
						return true;
					}
				}
			}
		}
		return false;
	}
	//他のプラグインの影響があってもブロックを破壊できるのか
	public static boolean canBreak(Player player ,Block breakblock) {
		//壊されるブロックの状態を取得
		BlockState blockstate = breakblock.getState();
		//壊されるブロックのデータを取得
		@SuppressWarnings("deprecation")
		byte data = blockstate.getData().getData();


		//壊されるブロックがワールドガード範囲だった場合処理を終了
		if(!Util.getWorldGuard().canBuild(player, breakblock.getLocation())){
			player.sendMessage(ChatColor.RED + "ワールドガードで保護されています。");
			return false;
		}
		//コアプロテクトのクラスを取得
		CoreProtectAPI CoreProtect = Util.getCoreProtect();
		//破壊ログを設定
		Boolean success = CoreProtect.logRemoval(player.getName(), breakblock.getLocation(), blockstate.getType(),data);
		//もし失敗したらプレイヤーに報告し処理を終了
		if(!success){
			player.sendMessage(ChatColor.RED + "coreprotectに保存できませんでした。管理者に報告してください。");
			return false;
		}
		return true;
	}
	//ブロックを破壊する処理、ドロップも含む、統計増加も含む
	public static void BreakBlock(Player player,Block breakblock,Location centerofblock,ItemStack tool,Boolean stepflag) {

		Material material = breakblock.getType();
		ItemStack itemstack = dropItemOnTool(breakblock,tool);

		if(material.equals(Material.GLOWING_REDSTONE_ORE)){
			material = Material.REDSTONE_ORE;
		}


		//アイテムをドロップさせる
		if(!addItemtoMineStack(player,itemstack)){
			breakblock.getWorld().dropItemNaturally(centerofblock,itemstack);
		}


		//ブロックを空気に変える
		breakblock.setType(Material.AIR);

		if(stepflag){
			//あたかもプレイヤーが壊したかのようなエフェクトを表示させる、壊した時の音を再生させる
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND,material);
		}
		// Effect.ENDER_SIGNALこれかっこいい
		// Effect.EXPLOSION 範囲でかい
		// Effect.WITCH_MAGIC 小さい 紫
		// Effect.SPELL かわいい
		// Effect.WITHER_SHOOT 音だけ、結構うるさい
		// Effect.WITHER_BREAK_BLOCK これまた音だけ　うるせえ
		// Effect.COLOURED_DUST エフェクトちっちゃすぎ
		// Effect.LARGE_SMOKE EXPLOSIONの黒版
		// Effect.MOBSPAWNER_FLAMES 火の演出　すき
		// Effect.SMOKE　黒いすすを噴き出してる
		// Effect.HAPPY_VILLAGER 緑のパーティクル　けっこう長く残る
		// Effect.INSTANT_SPELL かなりいい白いパーティクル
		//expman.changeExp(calcExpDrop(playerdata));
		//orb.setExperience(calcExpDrop(blockexpdrop,playerdata));
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
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
		return true;

	}
	@SuppressWarnings("deprecation")
	public static ItemStack dropItemOnTool(Block breakblock, ItemStack tool) {
		ItemStack dropitem = null;
		Material dropmaterial;
		Material breakmaterial = breakblock.getType();
		int fortunelevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        int bonus = (int) (Math.random() * ((fortunelevel + 2)) - 1);
        if (bonus <= 1) {
            bonus = 1;
        }
        byte b = breakblock.getData();
        b &= 0x03;


		int silktouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH);
		if(silktouch > 0){
			//シルクタッチの処理
			switch(breakmaterial){
			case GLOWING_REDSTONE_ORE:
				dropmaterial = Material.REDSTONE_ORE;
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
					dropitem = dye.toItemStack(bonus);
					break;
				case EMERALD_ORE:
					dropmaterial = Material.EMERALD;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case GLOWING_REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case QUARTZ_ORE:
					dropmaterial = Material.QUARTZ;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case GRAVEL:
					dropmaterial = Material.FLINT;
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
					dropitem = dye.toItemStack();
					break;
				case EMERALD_ORE:
					dropmaterial = Material.EMERALD;
					dropitem = new ItemStack(dropmaterial);
					break;
				case REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial);
					break;
				case GLOWING_REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial,bonus);
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
				default:
					//breakblcokのままのアイテムスタックを保存
					dropitem = new ItemStack(breakmaterial,1,b);
					break;
			}
		}
		return dropitem;
	}

	//追加経験値の設定
	public static int calcExpDrop(PlayerData playerdata) {
		//０～１のランダムな値を取得
		double rand = Math.random();
		//10%の確率で経験値付与
		if(rand < 0.1){
			//Lv8未満は獲得経験値ゼロ、それ以上はレベルに応じて経験値付与
			if(playerdata.level < 8){
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
}
