package com.github.unchama.seichiassist.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import com.github.unchama.seichiassist.SeichiAssist;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Util {
	static private FireworkEffect.Type[] types = { FireworkEffect.Type.BALL,
		FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.BURST,
		FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR, };

	public static void sendPlayerDataNullMessage(Player player){
		player.sendMessage(ChatColor.RED + "初回ログイン時の読み込み中か、読み込みに失敗しています");
		player.sendMessage(ChatColor.RED + "再接続しても改善されない場合はお問い合わせフォームからお知らせ下さい");
	}

	//スキルの発動可否の処理(発動可能ならtrue、発動不可ならfalse)
	public static boolean isSkillEnable(Player player){
		//デバッグモード時は全ワールドでスキル使用を許可する(DEBUGWORLDNAME = worldの場合)
		String worldname = SeichiAssist.SEICHIWORLDNAME;
		if(SeichiAssist.DEBUG){
			worldname = SeichiAssist.DEBUGWORLDNAME;
		}

		//整地ワールドzeroではスキル発動不可
		if(player.getWorld().getName().equalsIgnoreCase("world_sw_zero")){
			return false;
		}

		//プレイヤーの場所が各種整地ワールド(world_SWで始まるワールド)または各種メインワールド(world)にいる場合
		return player.getWorld().getName().toLowerCase().startsWith(worldname)
				|| player.getWorld().getName().equalsIgnoreCase("world")
				|| player.getWorld().getName().equalsIgnoreCase("world_2")
				|| player.getWorld().getName().equalsIgnoreCase("world_nether")
				|| player.getWorld().getName().equalsIgnoreCase("world_the_end")
				|| player.getWorld().getName().equalsIgnoreCase("world_TT")
				|| player.getWorld().getName().equalsIgnoreCase("world_nether_TT")
				|| player.getWorld().getName().equalsIgnoreCase("world_the_end_TT");
		//それ以外のワールドの場合
	}

	//プレイヤーが整地ワールドにいるかどうかの判定処理(整地ワールド=true、それ以外=false)
	public static boolean isSeichiWorld(Player player){
		//デバッグモード時は全ワールドtrue(DEBUGWORLDNAME = worldの場合)
		String worldname = SeichiAssist.SEICHIWORLDNAME;
		if(SeichiAssist.DEBUG){
			worldname = SeichiAssist.DEBUGWORLDNAME;
		}
		//整地ワールドではtrue
		return player.getWorld().getName().toLowerCase().startsWith(worldname);

		//それ以外のワールドの場合
	}

	//ガチャ券アイテムスタック型の取得
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
	//がちゃりんごの取得
	public static ItemStack getGachaimo() {
		ItemStack gachaimo;
		ItemMeta meta;
		gachaimo = new ItemStack(Material.GOLDEN_APPLE,1);
		meta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
		meta.setDisplayName(getGachaimoName());
		List<String> lore = getGachaimoLore();
		meta.setLore(lore);
		gachaimo.setItemMeta(meta);
		return gachaimo;
	}

	//がちゃりんごの名前を取得
	public static String getGachaimoName(){
		String name = ChatColor.GOLD + "" + ChatColor.BOLD + "がちゃりんご";
		return name;
	}
	//がちゃりんごの説明を取得
	public static List<String> getGachaimoLore(){
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "序盤に重宝します。"
				, ChatColor.RESET + "" +  ChatColor.AQUA + "マナ回復（小）");
		return lore;
	}

	//椎名林檎の取得
	public static ItemStack getMaxRingo(String name) {
		ItemStack maxringo;
		ItemMeta meta;
		maxringo = new ItemStack(Material.GOLDEN_APPLE,1);
		maxringo.setDurability((short) 1);
		meta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
		meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "椎名林檎");
		List<String> lore = getMaxRingoLore(name);
		meta.setLore(lore);
		maxringo.setItemMeta(meta);
		return maxringo;
	}
	//椎名林檎の説明を取得
	public static List<String> getMaxRingoLore(String name){
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "使用するとマナが全回復します"
				, ChatColor.RESET + "" +  ChatColor.AQUA + "マナ完全回復"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者:" + name
				, ChatColor.RESET + "" +  ChatColor.GRAY + "ガチャ景品と交換しました。");
		return lore;
	}

	//String -> double
	public static double toDouble(String s){
		return Double.parseDouble(s);
	}
	//String -> int
	public static int toInt(String s) {
		return Integer.parseInt(s);
	}
	//double -> .1double
	public static double Decimal(double d) {
		BigDecimal bi = new BigDecimal(String.valueOf(d));
		return bi.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	//プレイヤーのインベントリがフルかどうか確認
	public static boolean isPlayerInventryFill(Player player){
		return (player.getInventory().firstEmpty() == -1);
	}
	//指定されたアイテムを指定されたプレイヤーにドロップする
	public static void dropItem(Player player,ItemStack itemstack){
		player.getWorld().dropItemNaturally(player.getLocation(), itemstack);
	}
	//指定されたアイテムを指定されたプレイヤーインベントリに追加する
	public static void addItem(Player player,ItemStack itemstack){
		player.getInventory().addItem(itemstack);
	}

	/**
	 * プレイヤーに安全にアイテムを付与します。
	 *
	 * @param player 付与する対象プレイヤー
	 * @param itemStack 付与するアイテム
	 */
	public static void addItemToPlayerSafely(Player player, ItemStack itemStack) {
		if (isPlayerInventryFill(player)) {
			dropItem(player, itemStack);
		} else {
			addItem(player, itemStack);
		}
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

	public static void sendEveryMessageWithoutIgnore(String str){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			if (SeichiAssist.playermap.get(player.getUniqueId()).everymessageflag) {
				player.sendMessage(str);
			}
		}
	}

	/**
	 * json形式のチャットを送信する際に使用
	 */
	public static void sendEveryMessage(BaseComponent base){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			player.spigot().sendMessage(base);
		}
	}

	public static void sendEveryMessageWithoutIgnore(BaseComponent base){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			if (SeichiAssist.playermap.get(player.getUniqueId()).everymessageflag) {
				player.spigot().sendMessage(base);
			}
		}
	}

	public static String getEnchantName(String vaname, int enchlevel){
		switch(vaname){
			case "PROTECTION_ENVIRONMENTAL":
				return "ダメージ軽減" + " " + getEnchantLevelRome(enchlevel);

			case "PROTECTION_FIRE":
				return "火炎耐性" + " " + getEnchantLevelRome(enchlevel);

			case "PROTECTION_FALL":
				return "落下耐性" + " " + getEnchantLevelRome(enchlevel);

			case "PROTECTION_EXPLOSIONS":
				return "爆発耐性" + " " + getEnchantLevelRome(enchlevel);

			case "PROTECTION_PROJECTILE":
				return "飛び道具耐性" + " " + getEnchantLevelRome(enchlevel);

			case "OXYGEN":
				return "水中呼吸" + " " + getEnchantLevelRome(enchlevel);

			case "WATER_WORKER":
				return "水中採掘";

			case "THORNS":
				return "棘の鎧" + " " + getEnchantLevelRome(enchlevel);

			case "DEPTH_STRIDER":
				return "水中歩行" + " " + getEnchantLevelRome(enchlevel);

			case "FROST_WALKER":
				return "氷渡り" + " " + getEnchantLevelRome(enchlevel);

			case "DAMAGE_ALL":
				return "ダメージ増加" + " " + getEnchantLevelRome(enchlevel);

			case "DAMAGE_UNDEAD":
				return "アンデッド特効" + " " + getEnchantLevelRome(enchlevel);

			case "DAMAGE_ARTHROPODS":
				return "虫特効" + " " + getEnchantLevelRome(enchlevel);

			case "KNOCKBACK":
				return "ノックバック" + " " + getEnchantLevelRome(enchlevel);

			case "FIRE_ASPECT":
				return "火属性" + " " + getEnchantLevelRome(enchlevel);

			case "LOOT_BONUS_MOBS":
				return "ドロップ増加" + " " + getEnchantLevelRome(enchlevel);

			case "DIG_SPEED":
				return "効率強化" + " " + getEnchantLevelRome(enchlevel);

			case "SILK_TOUCH":
				return "シルクタッチ";

			case "DURABILITY":
				return "耐久力" + " " + getEnchantLevelRome(enchlevel);

			case "LOOT_BONUS_BLOCKS":
				return "幸運" + " " + getEnchantLevelRome(enchlevel);

			case "ARROW_DAMAGE":
				return "射撃ダメージ増加" + " " + getEnchantLevelRome(enchlevel);

			case "ARROW_KNOCKBACK":
				return "パンチ" + " " + getEnchantLevelRome(enchlevel);

			case "ARROW_FIRE":
				return "フレイム";

			case "ARROW_INFINITE":
				return "無限";

			case "LUCK":
				return "宝釣り" + " " + getEnchantLevelRome(enchlevel);

			case "LURE":
				return "入れ食い" + " " + getEnchantLevelRome(enchlevel);

			case "MENDING":
				return "修繕";

			default:
				return vaname;
		}
	}

	private static String getEnchantLevelRome(int enchantlevel){
		switch(enchantlevel){
			case 1:
				return "Ⅰ";

			case 2:
				return "Ⅱ";

			case 3:
				return "Ⅲ";

			case 4:
				return "Ⅳ";

			case 5:
				return "Ⅴ";

			case 6:
				return "Ⅵ";

			case 7:
				return "Ⅶ";

			case 8:
				return "Ⅷ";

			case 9:
				return "Ⅸ";

			case 10:
				return "Ⅹ" ;

			default:
				return String.valueOf(enchantlevel);
		}

	}

	public static String getDescFormat(List<String> list)
	{
		return list.toString().replaceAll(",", "\n").replaceAll("\\[", " ").replaceAll("\\]", "\n");
	}

	public static void sendEverySound(Sound str, float a, float b){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			player.playSound(player.getLocation(), str, a, b);
		}
	}
	public static void sendEverySoundWithoutIgnore(Sound str, float a, float b){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			if (SeichiAssist.playermap.get(player.getUniqueId()).everysoundflag) {
				player.playSound(player.getLocation(), str, a, b);
			}
		}
	}

	public static int toSecond(int _tick){
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

	//指定された場所に花火を打ち上げる関数
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
		effect.withColor(getRandomColors(1 + rand.nextInt(5)));

		// 余韻の色を単色～3色以内でランダムに決める
		effect.withFade(getRandomColors(1 + rand.nextInt(3)));

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
	//カラーをランダムで決める
	public static Color[] getRandomColors(int length) {
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
	//コアプロテクトAPIを返す
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
	//ワールドガードAPIを返す
	public static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = SeichiAssist.plugin.getServer().getPluginManager().getPlugin("WorldGuard");

	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }

	    return (WorldGuardPlugin) plugin;
	}
	//ワールドエディットAPIを返す
	public static WorldEditPlugin getWorldEdit() {
        Plugin pl = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if(pl instanceof WorldEditPlugin)
            return (WorldEditPlugin)pl;
        else return null;
    }

	//ガチャアイテムを含んでいるか調べる
	public static boolean containsGachaTicket(Player player) {
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
	//boolean -> int
	public static int toInt(boolean flag) {
		if(flag){
			return 1;
		}else{
			return 0;
		}
	}
	//Listの中でひとつでもstringに該当するものがあればtrueを開放します。
	public static int LoreContains(List<String> lore, String string) {
		for(int i = 0; i < lore.size(); i++){
			if(lore.get(i).contains(string))return i;
		}
		return -1;
	}
	public static boolean isGachaTicket(ItemStack itemstack) {
		if(!itemstack.getType().equals(Material.SKULL_ITEM)){
			return false;
		}
		SkullMeta skullmeta = (SkullMeta) itemstack.getItemMeta();

		//ownerがいない場合処理終了
		if(!skullmeta.hasOwner()){
			return false;
		}
		//ownerがうんちゃまじゃない時の処理
		return skullmeta.getOwner().equals("unchama");
	}
	public static boolean removeItemfromPlayerInventory(PlayerInventory inventory,
			ItemStack itemstack, int count) {
		//持っているアイテムを減らす処理
		if (itemstack.getAmount() == count) {
			// アイテムをcount個使うので、プレイヤーの手を素手にする
			inventory.setItemInMainHand(new ItemStack(Material.AIR));
		}
		else if(itemstack.getAmount() > count){
			// プレイヤーが持っているアイテムをcount個減らす
			itemstack.setAmount(itemstack.getAmount()-count);
		}
		else return itemstack.getAmount() >= count;
		return true;
	}
	public static ItemStack getForBugskull(String name) {
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skull.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券");
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで使えます"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者：" + name
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "運営から不具合のお詫びです");
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}
	public static ItemStack getVoteskull(String name) {
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skull.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券");
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで使えます"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者：" + name
				,ChatColor.RESET + "" +  ChatColor.LIGHT_PURPLE + "投票ありがとナス♡");
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}
	public static ItemStack getExchangeskull(String name) {
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skull.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券");
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで使えます"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者：" + name
				,ChatColor.RESET + "" +  ChatColor.GRAY + "ガチャ景品と交換しました。");
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}

	public static boolean ItemStackContainsOwnerName(ItemStack itemstack, String name) {

		ItemMeta meta = itemstack.getItemMeta();
		List<String> lore;
		if(meta.hasLore()){
			lore = meta.getLore();
		}else{
			lore = new ArrayList<String>();
		}

		for(int i=0; i<lore.size(); i++){
			if(lore.get(i).indexOf("所有者：")!=-1){ //"所有者:がある"
				int idx = lore.get(i).lastIndexOf("所有者：");
					idx += 4; //「所有者：」の右端(名前の左端)までidxを移動
					String temp = lore.get(i).substring(idx);
					if(temp.equals(name)){
						return true;
					}
			}
		}
		return false;
	}

	public static ItemStack ItemStackResetName(ItemStack itemstack) {

		ItemStack itemstack_temp = new ItemStack(itemstack);
		ItemMeta meta = itemstack_temp.getItemMeta();
		List<String> lore;
		if(meta != null){
			if(meta.hasLore()){
				lore = meta.getLore();

				int i=0;
				for(i=0; i<lore.size(); i++){
					if(lore.get(i).indexOf("所有者：")!=-1){ //"所有者:がある"
						break;
					}
				}
				if(i!=lore.size()){ //所有者表記が無かった場合を除く
					lore.remove(i);
					meta.setLore(lore);
				}
			}
		}
		return itemstack_temp;
	}

	public static int getMineStackTypeindex(int idx){
		int temp = 0;
		int type = SeichiAssist.minestacklist.get(idx).getStacktype();
		for (int i = 0; i < idx; i++) {
			if (SeichiAssist.minestacklist.get(i).getStacktype() == type) {
				temp++;
			}
		}
		return temp;
	}

	/**
	 * GUIメニューアイコン作成用
	 * @author karayuu
	 *
	 * @param material メニューアイコンMaterial, not {@code null}
	 * @param amount メニューアイコンのアイテム個数
	 * @param displayName メニューアイコンのDisplayName, not {@code null}
	 * @param lore メニューアイコンのLore, not {@code null}
	 * @param isHideFlags 攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
	 * @throws IllegalArgumentException Material,DisplayName, Loreのいずれかが {@code null} の時
	 * @return ItemStack型のメニューアイコン
	 */
	public static ItemStack getMenuIcon(Material material, int amount,
										String displayName, List<String> lore, boolean isHideFlags) {
		if (material == null || displayName == null || lore == null) {
			throw new IllegalArgumentException("Material,DisplayName,LoreにNullは指定できません。");
		}
		ItemStack menuicon = new ItemStack(material, amount);
		ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(material);
		itemMeta.setDisplayName(displayName);
		itemMeta.setLore(lore);
		if (isHideFlags) {
			itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		menuicon.setItemMeta(itemMeta);

		return menuicon;
	}

	/**
	 * GUIメニューアイコン作成用
	 * @author karayuu
	 *
	 * @param material メニューアイコンMaterial, not {@code null}
	 * @param amount メニューアイコンのアイテム個数
	 * @param durabity メニューアイコンのダメージ値
	 * @param displayName メニューアイコンのDisplayName, not {@code null}
	 * @param lore メニューアイコンのLore, not {@code null}
	 * @param isHideFlags 攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
	 * @throws IllegalArgumentException Material,DisplayName, Loreのいずれかが {@code null} の時
	 * @return ItemStack型のメニューアイコン
	 */
	public static ItemStack getMenuIcon(Material material, int amount, int durabity,
										String displayName, List<String> lore, boolean isHideFlags) {
		if (material == null || displayName == null || lore == null) {
			throw new IllegalArgumentException("Material,DisplayName,LoreにNullは指定できません。");
		}
		ItemStack menuicon = new ItemStack(material, amount, (short) durabity);
		ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(material);
		itemMeta.setDisplayName(displayName);
		itemMeta.setLore(lore);
		if (isHideFlags) {
			itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		menuicon.setItemMeta(itemMeta);

		return menuicon;
	}

	/**
	 * PlayerDataでチャンク数をゲット・セットするためのenum
	 */
	public enum DirectionType {
		AHEAD,
		BEHIND,
		RIGHT,
		LEFT,
		;
	}

	/**
	 * PlayerDataなどで使用する方角関係のenum
	 */
	public enum Direction {
		NORTH,
		SOUTH,
		EAST,
		WEST,
		;
	}

	public static Direction getPlayerDirection(Player player) {
		double rotation = (player.getLocation().getYaw() + 180) % 360;

		if (rotation < 0) {
			rotation += 360;
		}

		//0,360:south 90:west 180:north 270:east
		if (0.0 <= rotation && rotation < 45.0) {
			//前が北(North)
			return Direction.NORTH;
		} else if (45.0 <= rotation && rotation < 135.0) {
			//前が東(East)
			return Direction.EAST;
		} else if (135.0 <= rotation && rotation < 225.0) {
			//前が南(South)
			return Direction.SOUTH;
		} else if (225.0 <= rotation && rotation < 315.0) {
			//前が西(West)
			return Direction.WEST;
		} else if (315.0 <= rotation && rotation < 360.0) {
			//前が北(North)
			return Direction.NORTH;
		}
		//ここに到達はありえない。
		return null;
	}

	public static String showTime(Calendar cal) {
		  Date date = cal.getTime();
		  SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		  return format.format(date);
		}

	public static boolean isVotingFairyPeriod(Calendar start, Calendar end) {
		Calendar cur = Calendar.getInstance();
		return cur.after(start) && cur.before(end);
	}

	public static String getWorldName(String s){
		String worldname = "";
		switch(s){
		case "world_spawn":
			worldname = "スポーンワールド";
			break;
		case "world":
			worldname = "メインワールド";
			break;
		case "world_SW":
			worldname = "第一整地ワールド";
			break;
		case "world_SW_2":
			worldname = "第二整地ワールド";
			break;
		case "world_SW_3":
			worldname = "第三整地ワールド";
			break;
		case "world_SW_nether":
			worldname = "整地ネザー";
			break;
		case "world_SW_the_end":
			worldname = "整地エンド";
			break;
		default:
			worldname = s;
		}
		return worldname;
	}

    public static void setDifficulty(List<String> worldNameList, Difficulty difficulty) {
        for (String name : worldNameList) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                Bukkit.getLogger().warning(name + "という名前のワールドは存在しません。");
                continue;
            }
            world.setDifficulty(difficulty);
        }
    }

    /**
     * 指定した名前のマインスタックの番号を返す
     * @param s SeichiAssist.javaのminestacklistに定義されてる英語名
     * @return マインスタック番号(見つからなかった場合は-1)
     */
    public static int MineStackobjname_indexOf(String s){
        int id = -1;
        for(int x = 0 ; x < SeichiAssist.minestacklist.size() ; x++){
            if( s.equals( SeichiAssist.minestacklist.get(x).getMineStackObjName() ) ){
                id = x;
                break;
            }
        }
        return id;
    }

	public static boolean isEnemy(EntityType type) {
		switch(type){
		//通常世界MOB
		case CAVE_SPIDER:
			return true;
		case CREEPER:
			return true;
		case GUARDIAN:
			return true;
		case SILVERFISH:
			return true;
		case SKELETON:
			return true;
		case SLIME:
			return true;
		case SPIDER:
			return true;
		case WITCH:
			return true;
		case ZOMBIE:
			return true;
		//ネザーMOB
		case BLAZE:
			return true;
		case GHAST:
			return true;
		case MAGMA_CUBE:
			return true;
		case PIG_ZOMBIE:
			return true;
		//エンドMOB
		case ENDERMAN:
			return true;
		case ENDERMITE:
			return true;
		case SHULKER:
			return true;
		//敵MOB以外(エンドラ,ウィザーは除外)
		default:
			return false;
		}
	}
}
