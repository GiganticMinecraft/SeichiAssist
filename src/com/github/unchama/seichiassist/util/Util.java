package com.github.unchama.seichiassist.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import zedly.zenchantments.Zenchantments;

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
		if(player.getWorld().getName().toLowerCase().startsWith(worldname)
				|| player.getWorld().getName().equalsIgnoreCase("world")
				|| player.getWorld().getName().equalsIgnoreCase("world_2")
				|| player.getWorld().getName().equalsIgnoreCase("world_nether")
				|| player.getWorld().getName().equalsIgnoreCase("world_the_end")
				|| player.getWorld().getName().equalsIgnoreCase("world_TT")
				|| player.getWorld().getName().equalsIgnoreCase("world_nether_TT")
				|| player.getWorld().getName().equalsIgnoreCase("world_the_end_TT")
				){
			return true;
		}
		//それ以外のワールドの場合
		return false;
	}

	//プレイヤーが整地ワールドにいるかどうかの判定処理(整地ワールド=true、それ以外=false)
	public static boolean isSeichiWorld(Player player){
		//デバッグモード時は全ワールドtrue(DEBUGWORLDNAME = worldの場合)
		String worldname = SeichiAssist.SEICHIWORLDNAME;
		if(SeichiAssist.DEBUG){
			worldname = SeichiAssist.DEBUGWORLDNAME;
		}
		//整地ワールドではtrue
		if(player.getWorld().getName().toLowerCase().startsWith(worldname)){
			return true;
		}

		//それ以外のワールドの場合
		return false;
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

	//ZenchantmentAPIを返す
	public static Zenchantments getZenchantments() {
        Plugin pl = Bukkit.getServer().getPluginManager().getPlugin("Zenchantments");
        if(pl instanceof Zenchantments)
            return (Zenchantments)pl;
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
		if(!skullmeta.getOwner().equals("unchama")){
			return false;
		}

		return true;
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
		else if(itemstack.getAmount() < count){
			return false;
		}
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
		int temp=0;
		int type=SeichiAssist.minestacklist.get(idx).getStacktype();
		for(int i=0; i<idx; i++){
			if(SeichiAssist.minestacklist.get(i).getStacktype()==type){
				temp++;
			}
		}
		return temp;
	}




}
