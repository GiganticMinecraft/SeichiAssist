package com.github.unchama.seichiassist.util;

import java.math.BigDecimal;
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
		meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "がちゃりんご");
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "序盤に重宝します。"
				, ChatColor.RESET + "" +  ChatColor.AQUA + "マナ回復（小）");
		meta.setLore(lore);
		gachaimo.setItemMeta(meta);
		return gachaimo;
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
	//boolean -> int
	public static int toInt(boolean flag) {
		if(flag){
			return 1;
		}else{
			return 0;
		}
	}
	//Listの中でひとつでもstringに該当するものがあればtrueを開放します。
	public static boolean LoreContains(List<String> lore, String string) {
		for(int i = 0; i < lore.size(); i++){
			if(lore.get(i).contains(string))return true;
		}
		return false;
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




}
