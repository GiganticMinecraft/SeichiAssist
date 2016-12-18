package com.github.unchama.seichiassist.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.MenuInventoryData;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.CoolDownTaskRunnable;
import com.github.unchama.seichiassist.task.TitleUnlockTaskRunnable;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class PlayerInventoryListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	List<GachaData> gachadatalist = SeichiAssist.gachadatalist;
	SeichiAssist plugin = SeichiAssist.plugin;
	private Config config = SeichiAssist.config;
	private Sql sql = SeichiAssist.plugin.sql;

	//棒メニュー
	@EventHandler
	public void onPlayerClickStickMenuEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが36でない時終了
		if(topinventory.getSize() != 36){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニュー")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			//経験値変更用のクラスを設定
			ExperienceManager expman = new ExperienceManager(player);

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			//ページ変更処理
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData2(player));
				return;
			}

			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}

			else if(itemstackcurrent.getType().equals(Material.CHEST)){
				//レベルが足りない場合処理終了
				if( playerdata.level < SeichiAssist.config.getMineStacklevel(1)){
					player.sendMessage(ChatColor.GREEN + "整地レベルが"+SeichiAssist.config.getMineStacklevel(1)+ "以上必要です");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					return;
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player,0));
				return;
			}
			//スキルメニューを開く
			else if(itemstackcurrent.getType().equals(Material.ENCHANTED_BOOK)){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				//アクティブスキルとパッシブスキルの分岐
				if(itemmeta.getDisplayName().contains("アクティブ")){
					player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
				}else if(itemmeta.getDisplayName().contains("パッシブ")){
					//player.sendMessage("未実装ナリよ");
					player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player));
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 0.8);
				return;
			}
			//整地神番付を開く
			else if(itemstackcurrent.getType().equals(Material.COOKIE)){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getRankingList(player));
				return;
			}


			//溜まったガチャ券をインベントリへ
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("unchama")){
				//連打防止クールダウン処理
				if(!playerdata.gachacooldownflag){
					return;
				}else{
			        //連打による負荷防止の為クールダウン処理
					new CoolDownTaskRunnable(player,false,false,true).runTaskLater(plugin,20);
				}

				ItemStack skull = Util.getskull(Util.getName(player));
				int count = 0;
				while(playerdata.gachapoint >= config.getGachaPresentInterval() && count < 576){
					playerdata.gachapoint -= config.getGachaPresentInterval();
					if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
						Util.addItem(player,skull);
					}else{
						Util.dropItem(player,skull);
					}
					count++;
				}
				//プレイヤーデータを更新
				playerdata.lastgachapoint = playerdata.gachapoint;

				if(count > 0){
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + count + "枚" + ChatColor.WHITE + "プレゼントフォーユー");
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
				}

				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemmeta.setLore(MenuInventoryData.GachaGetButtonLore(playerdata));
				itemstackcurrent.setItemMeta(itemmeta);
			}

			//運営からのガチャ券受け取り
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("whitecat_haru")){

				//nは最新のnumofsorryforbugの値になる(上限値576個)
				int n = sql.givePlayerBug(player,playerdata);
				//0だったら処理終了
				if(n == 0){
					return;
				}

				ItemStack skull = Util.getForBugskull(Util.getName(player));
				int count = 0;
				while(n > 0){
					playerdata.numofsorryforbug--;
					if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
						Util.addItem(player,skull);
					}else{
						Util.dropItem(player,skull);
					}
					n--;
					count++;
				}

				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
				player.sendMessage(ChatColor.GREEN + "運営チームから"+count+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "を受け取りました");

				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemmeta.setLore(MenuInventoryData.SorryGachaGetButtonLore(playerdata));
				itemstackcurrent.setItemMeta(itemmeta);
			}

			//投票特典受け取り
			else if(itemstackcurrent.getType().equals(Material.DIAMOND)){

				//nは特典をまだ受け取ってない投票分
				int n = sql.compareVotePoint(player,playerdata);
				//投票数に変化が無ければ処理終了
				if(n == 0){
					return;
				}
				//先にp_voteの値を更新しておく
				playerdata.p_givenvote += n;

				int count = 0;
				while(n > 0){
					//ここに投票1回につきプレゼントする特典の処理を書く

					//ガチャ券プレゼント処理
					ItemStack skull = Util.getVoteskull(Util.getName(player));
					for (int i = 0; i < 10; i++){
						if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
							Util.addItem(player,skull);
						}else{
							Util.dropItem(player,skull);
						}
					}

					//ピッケルプレゼント処理(レベル30になるまで)
					/*
					if(playerdata.level < 30){

					}
					*/
					ItemStack itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
					ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
					itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Thanks for Voting!");
					List<String> lore = Arrays.asList("投票ありがとナス♡"
							);
					itemmeta.addEnchant(Enchantment.DIG_SPEED, 3, true);
					itemmeta.addEnchant(Enchantment.DURABILITY, 3, true);
					itemmeta.setLore(lore);
					itemstack.setItemMeta(itemmeta);
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,itemstack);
					}else{
						Util.dropItem(player,itemstack);
					}

					//エフェクトポイント加算処理
					playerdata.activeskilldata.effectpoint += 10;

					n--;
					count++;
				}

				player.sendMessage(ChatColor.GOLD + "投票特典" + ChatColor.WHITE + "(" + count + "票分)を受け取りました");
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);

				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemmeta.setLore(MenuInventoryData.VoteGetButtonLore(playerdata));
				itemstackcurrent.setItemMeta(itemmeta);
			}


			//経験値を消費してプレイヤーの頭を召喚
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_Villager")){
				//経験値変更用のクラスを設定
				//経験値が足りなかったら処理を終了
				if(!expman.hasExp(10000)){
					player.sendMessage(ChatColor.RED + "必要な経験値が足りません");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					return;
				}
				//経験値消費
				expman.changeExp(-10000);

				//プレイヤーの頭作成
				ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
				SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				skull.setDurability((short) 3);
				skullmeta.setOwner(player.getName());
				skull.setItemMeta(skullmeta);

				//渡すか、落とすか
				if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
					Util.addItem(player,skull);
				}else{
					Util.dropItem(player,skull);
				}
				player.sendMessage(ChatColor.GOLD + "経験値10000を消費して自分の頭を召喚しました");
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			}

			else if(itemstackcurrent.getType().equals(Material.STONE_BUTTON)){
				playerdata.gachaflag = !playerdata.gachaflag;
				if(playerdata.gachaflag){
					player.sendMessage(ChatColor.GREEN + "毎分のガチャ券受け取り:ON");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					ItemMeta itemmeta = itemstackcurrent.getItemMeta();
					List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "毎分受け取ります"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更"
							);
					itemmeta.setLore(lore);
					itemstackcurrent.setItemMeta(itemmeta);
				}else{
					player.sendMessage(ChatColor.RED + "毎分のガチャ券受け取り:OFF");
					player.sendMessage(ChatColor.GREEN + "ガチャ券受け取りボタンを押すともらえます");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1, 1);
					ItemMeta itemmeta = itemstackcurrent.getItemMeta();
					List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "後でまとめて受け取ります"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更"
							);
					itemmeta.setLore(lore);
					itemstackcurrent.setItemMeta(itemmeta);
				}
			}

			else if(itemstackcurrent.getType().equals(Material.DIAMOND_PICKAXE)){
				// ver0.3.2 採掘速度上昇効果トグル
				playerdata.effectflag = !playerdata.effectflag;
				if(playerdata.effectflag){
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

					//effect追加の処理
					//実際に適用されるeffect量
					int minespeedlv = 0;
					//合計effect量
					double sum = 0;
					//最大持続時間
					int maxduration = 0;
					//effectdatalistにある全てのeffectについて計算
					for(EffectData ed :playerdata.effectdatalist){
						//effect量を加算
						sum += ed.amplifier;
						//持続時間の最大値を取得
						if(maxduration < ed.duration){
							maxduration = ed.duration;
						}
					}
					//実際のeffect値をsum-1の切り捨て整数値に設定
					minespeedlv = (int)(sum - 1);

					//実際のeffect値が0より小さいときはeffectを適用しない
					if(minespeedlv < 0){
						player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true);
					}else{
						player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, minespeedlv, false, false), true);
					}

					player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON");
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);

					//現在の採掘速度上昇効果を削除する
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true);

					player.sendMessage(ChatColor.RED + "採掘速度上昇効果:OFF");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.EFButtonMeta(playerdata,itemmeta));
			}

			else if(itemstackcurrent.getType().equals(Material.FLINT_AND_STEEL)){
				// 死亡メッセージ表示トグル
				playerdata.dispkilllogflag = !playerdata.dispkilllogflag;
				if(playerdata.dispkilllogflag){
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "死亡メッセージ:表示");
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "死亡メッセージ:隠す");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.dispKillLogToggleMeta(playerdata,itemmeta));
			}

			//追加
			else if(itemstackcurrent.getType().equals(Material.BARRIER)){
				// ワールドガード保護表示トグル
				playerdata.dispworldguardlogflag = !playerdata.dispworldguardlogflag;
				if(playerdata.dispworldguardlogflag){
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "ワールドガード保護メッセージ:表示");
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "ワールドガード保護メッセージ:隠す");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.dispWorldGuardLogToggleMeta(playerdata,itemmeta));
			}

			else if(itemstackcurrent.getType().equals(Material.IRON_SWORD)){
				// 死亡メッセージ表示トグル
				playerdata.pvpflag = !playerdata.pvpflag;
				if(playerdata.pvpflag){
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "PvP:ON");
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "PvP:OFF");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.dispPvPToggleMeta(playerdata,itemmeta));
			}

			else if(itemstackcurrent.getType().equals(Material.BEACON)){
				// spawnコマンド実行
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/spawn");
			}

			else if(itemstackcurrent.getType().equals(Material.BED)){
				// sethomeコマンド実行
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();

				if(itemmeta.getDisplayName().contains("サブホームポイント")){//ホームボタンかサブホームボタンか判定
					//ホームをセット
					int z = Integer.parseInt( itemmeta.getDisplayName().substring(15, 16) ) - 1;	//サブホームボタンの番号
					playerdata.SetSubHome(player.getLocation(), z);

					//mysqlにも書き込んどく
					/*別スレッド処理PlayerDataSaveTaskRunnableに移動
					if(!sql.UpDataSubHome(playerdata.SubHomeToString())){
						player.sendMessage(ChatColor.RED + "失敗");
					}else{
						player.sendMessage("現在位置をサブホームポイント"+(z+1)+"に設定しました");
					}
					*/
					player.sendMessage("現在位置をサブホームポイント"+(z+1)+"に設定しました");
					player.closeInventory();
				}else {
					player.chat("/sethome");
				}
			}

			else if(itemstackcurrent.getType().equals(Material.COMPASS)){
				// homeコマンド実行
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("サブホームポイント")){//ホームボタンかサブホームボタンか判定
					//サブホームに移動
					int z = Integer.parseInt( itemmeta.getDisplayName().substring(15, 16) ) - 1;	//サブホームボタンの番号
					Location l = playerdata.GetSubHome(z);
					if(l != null){
						World world = Bukkit.getWorld(l.getWorld().getName());
						if(world != null){
							player.teleport(l);
							player.sendMessage("サブホームポイント"+ (z+1) +"にワープしました");
						}else{
							player.sendMessage("サブホームポイント"+ (z+1) +"が設定されてません");
						}
					}else{
						player.sendMessage("サブホームポイント"+ (z+1) +"が設定されてません");
					}
				}else {
					player.chat("/home");
				}



			}

			else if(itemstackcurrent.getType().equals(Material.WORKBENCH)){
				// /fc craftコマンド実行
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/fc craft");
			}

			else if(itemstackcurrent.getType().equals(Material.WOOD_AXE)){
				// wand召喚
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("//wand");
				player.sendMessage(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "保護のかけ方\n"
						+ ChatColor.RESET + "" +  ChatColor.GREEN + "①召喚された斧を手に持ちます\n"
						+ ChatColor.RESET + "" +  ChatColor.GREEN + "②保護したい領域の一方の角を" + ChatColor.YELLOW + "左" + ChatColor.GREEN + "クリック\n"
						+ ChatColor.RESET + "" +  ChatColor.GREEN + "③もう一方の対角線上の角を" + ChatColor.RED + "右" + ChatColor.GREEN + "クリック\n"
						+ ChatColor.RESET + "" +  ChatColor.GREEN + "④メニューの" + ChatColor.RESET + "" +  ChatColor.YELLOW + "金の斧" + ChatColor.RESET + "" +  ChatColor.GREEN + "をクリック\n"
						+ ChatColor.DARK_GREEN + "解説ページ→" + ChatColor.UNDERLINE + "http://seichi.click/d/WorldGuard"
						);
			}

			else if(itemstackcurrent.getType().equals(Material.GOLD_AXE)){
				// 保護の設定
				player.closeInventory();
				Selection selection = Util.getWorldEdit().getSelection(player);
				if(!player.hasPermission("worldguard.region.claim")){
					player.sendMessage(ChatColor.RED + "このワールドでは保護を申請できません");
					return;
				}else if (selection == null) {
					player.sendMessage(ChatColor.RED + "先に木の斧で範囲を指定してからこのボタンを押してください");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					return;
				}else if(selection.getLength() < 10||selection.getWidth() < 10){
					player.sendMessage(ChatColor.RED + "指定された範囲が狭すぎます。1辺当たり最低10ブロック以上にしてください");
					player.sendMessage(ChatColor.DARK_GRAY + "[TIPS]どうしても小さい保護が必要な人は直接コマンド入力で作ろう！");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					return;
				}

				player.chat("//expand vert");
				player.chat("/rg claim " + player.getName() + "_" + playerdata.rgnum);
				playerdata.rgnum += 1;
				player.chat("//sel");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
			}

			else if(itemstackcurrent.getType().equals(Material.STONE_AXE)){
				// 保護リストの表示
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
				player.sendMessage(ChatColor.GRAY + "--------------------\n"
						+ ChatColor.GRAY + "複数ページの場合… " + ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.BOLD + "/rg list ページNo\n"
						+ ChatColor.RESET + "" +  ChatColor.GRAY + "先頭に[+]のついた保護はOwner権限\n[-]のついた保護はMember権限を保有しています\n"
						+ ChatColor.DARK_GREEN + "解説ページ→" + ChatColor.UNDERLINE + "http://seichi.click/d/WorldGuard");
				player.chat("/rg list");
			}

			else if(itemstackcurrent.getType().equals(Material.DIAMOND_AXE)){
				// ReguionGUI表示
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
				player.chat("/land");
			}

			else if(itemstackcurrent.getType().equals(Material.NETHER_STAR)){
				// hubコマンド実行
				// player.chat("/hub");
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage(ChatColor.RESET + "" +  ChatColor.GRAY + "/hubと入力してEnterを押してください");
			}

			else if(itemstackcurrent.getType().equals(Material.BOOK)){
				// wikiリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "http://seichi.click");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType().equals(Material.BOOK_AND_QUILL)){
				// 投票リンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "https://minecraft.jp/servers/play.seichi.click/vote");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType().equals(Material.PAPER)){
				// 運営方針とルールリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "http://seichi.click/d/%b1%bf%b1%c4%ca%fd%bf%cb%a4%c8%a5%eb%a1%bc%a5%eb");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType().equals(Material.MAP)){
				// 鯖マップリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "http://seichi.click/d/DynmapLinks");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();

			}

			else if(itemstackcurrent.getType().equals(Material.SIGN)){
				// 掲示板リンク表示
				player.sendMessage(ChatColor.DARK_GRAY + "開いたら下の方までスクロールしてください\n"
						+ ChatColor.RED + "" + ChatColor.UNDERLINE + "https://minecraft.jp/servers/play.seichi.click"
						);
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType().equals(Material.ENDER_PORTAL_FRAME)){
				//ver0.3.2 四次元ポケットを開く
				//レベルが足りない場合処理終了
				if( playerdata.level < SeichiAssist.config.getPassivePortalInventorylevel()){
					player.sendMessage(ChatColor.GREEN + "4次元ポケットを開くには整地レベルが"+SeichiAssist.config.getPassivePortalInventorylevel()+ "以上必要です");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					return;
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, 1, (float) 0.1);

				//レベルに応じたポケットサイズ変更処理
				//アイテム消失を防ぐ為、現在のサイズよりも四次元ポケットサイズが大きくなる場合のみ拡張処理する
				if(playerdata.inventory.getSize() < playerdata.getPocketSize()){
					//現在の四次元ポケットの中身を取得
					ItemStack[] item = playerdata.inventory.getContents();
					//新しいサイズの四次元ポケットを作成
					Inventory newsizepocket = Bukkit.getServer().createInventory(null,playerdata.getPocketSize(),ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "4次元ポケット");
					//for文で一個ずつ新しいサイズのポケットに入れてく
					int i = 0;
		            for (ItemStack m : item) {
		                newsizepocket.setItem(i, m);
		                i++;
		            }
		            //出来たら置き換える
		            playerdata.inventory = newsizepocket;
				}
				//インベントリを開く
				player.openInventory(playerdata.inventory);
			}


			else if(itemstackcurrent.getType().equals(Material.ENDER_CHEST)){
				//レベルが足りない場合処理終了
				if( playerdata.level < SeichiAssist.config.getDokodemoEnderlevel()){
					player.sendMessage(ChatColor.GREEN + "整地レベルが"+SeichiAssist.config.getDokodemoEnderlevel()+ "以上必要です");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					return;
				}
				//どこでもエンダーチェストを開く
				player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, 1, (float) 1.0);
				player.openInventory(player.getEnderChest());
			}


			else if(itemstackcurrent.getType().equals(Material.BUCKET)){
				//ゴミ箱を開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 1.5);
				//インベントリを開く
				player.openInventory(SeichiAssist.plugin.getServer().createInventory(null, 9*4 ,ChatColor.RED + "" + ChatColor.BOLD + "ゴミ箱(取扱注意)"));
			}


			else if(itemstackcurrent.getType().equals(Material.NOTE_BLOCK)){
				//ガチャ景品交換システムを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 0.5);
				//インベントリを開く
				player.openInventory(SeichiAssist.plugin.getServer().createInventory(null, 9*4 ,ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "交換したい景品を入れてください"));
			}

			else if(itemstackcurrent.getType().equals(Material.END_CRYSTAL)){
				//実績メニューを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				//インベントリを開く
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
			}

		}
	}

	//追加!!!
	//スキルメニューの処理
	@EventHandler
	public void onPlayerClickPassiveSkillSellectEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが36でない時終了
		if(topinventory.getSize() != 36){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//経験値変更用のクラスを設定
		ExperienceManager expman = new ExperienceManager(player);


		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキル切り替え")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			//ページ変更処理
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}

			else if(itemstackcurrent.getType().equals(Material.DIAMOND_PICKAXE)){
				// 複数破壊トグル

				if(playerdata.level>=SeichiAssist.config.getMultipleIDBlockBreaklevel()){
					playerdata.multipleidbreakflag = !playerdata.multipleidbreakflag;
					if(playerdata.multipleidbreakflag){
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
						player.sendMessage(ChatColor.GREEN + "複数種類同時破壊:ON");
					}else{
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
						player.sendMessage(ChatColor.RED + "複数種類同時破壊:OFF");
					}
					ItemMeta itemmeta = itemstackcurrent.getItemMeta();
					itemstackcurrent.setItemMeta(MenuInventoryData.MultipleIDBlockBreakToggleMeta(playerdata,itemmeta));
				} else {
					player.sendMessage("整地レベルが足りません");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
				}
			}
		}
	}






	//スキルメニューの処理
	@EventHandler
	public void onPlayerClickActiveSkillSellectEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}


		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが36でない時終了
		if(topinventory.getSize() != 36){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//経験値変更用のクラスを設定
		ExperienceManager expman = new ExperienceManager(player);


		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキル選択")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			int type = 0;
			String name = null;
			int skilllevel;
			//ARROWSKILL
			type = ActiveSkill.ARROW.gettypenum();
			for(skilllevel = 4;skilllevel <= 9 ; skilllevel++){
				name = ActiveSkill.ARROW.getName(skilllevel);
				if(itemstackcurrent.getType().equals(ActiveSkill.ARROW.getMaterial(skilllevel))){
					PotionMeta potionmeta =(PotionMeta)itemstackcurrent.getItemMeta();
					if(potionmeta.getBasePotionData().getType().equals(ActiveSkill.ARROW.getPotionType(skilllevel))){
						if(playerdata.activeskilldata.skilltype == type
								&& playerdata.activeskilldata.skillnum == skilllevel){
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
							player.sendMessage(ChatColor.YELLOW + "既に選択されています");
						}else{
							playerdata.activeskilldata.updataSkill(player,type,skilllevel,1);
							player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + name + "  が選択されました");
							player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
						}
					}
				}
			}
			//MULTISKILL
			type = ActiveSkill.MULTI.gettypenum();
			for(skilllevel = 4;skilllevel <= 9 ; skilllevel++){
				name = ActiveSkill.MULTI.getName(skilllevel);
				if(itemstackcurrent.getType().equals(ActiveSkill.MULTI.getMaterial(skilllevel))){
					if(playerdata.activeskilldata.skilltype == type
							&& playerdata.activeskilldata.skillnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "既に選択されています");
					}else{
						playerdata.activeskilldata.updataSkill(player,type,skilllevel,1);
						player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + name + "  が選択されました");
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}
			//BREAKSKILL
			type = ActiveSkill.BREAK.gettypenum();
			for(skilllevel = 1;skilllevel <= 9 ; skilllevel++){
				name = ActiveSkill.BREAK.getName(skilllevel);
				if(itemstackcurrent.getType().equals(ActiveSkill.BREAK.getMaterial(skilllevel))){
					if(playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum()
							&& playerdata.activeskilldata.skillnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "既に選択されています");
					}else{
						playerdata.activeskilldata.updataSkill(player,type,skilllevel,1);
						player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + name + "  が選択されました");
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}
			//CONDENSKILL
			type = ActiveSkill.CONDENSE.gettypenum();
			for(skilllevel = 4;skilllevel <= 9 ; skilllevel++){
				name = ActiveSkill.CONDENSE.getName(skilllevel);
				if(itemstackcurrent.getType().equals(ActiveSkill.CONDENSE.getMaterial(skilllevel))){
					if(playerdata.activeskilldata.assaulttype == type
							&& playerdata.activeskilldata.assaultnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "既に選択されています");
					}else{
						playerdata.activeskilldata.updataAssaultSkill(player,type,skilllevel,1);
						player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + name + "  が選択されました");
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}

			//アサルトアーマー
			type = ActiveSkill.ARMOR.gettypenum();
			skilllevel = 10;
			if(itemstackcurrent.getType().equals(ActiveSkill.ARMOR.getMaterial(skilllevel))){
				if(playerdata.activeskilldata.assaultnum == skilllevel || playerdata.activeskilldata.assaulttype == type){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else{
					playerdata.activeskilldata.updataAssaultSkill(player,type,skilllevel,1);
					player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + "アサルト・アーマー" + "  が選択されました");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}
			}

			//ページ変更処理
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}
			else if(itemstackcurrent.getType().equals(Material.STONE_BUTTON)){
				if(itemstackcurrent.getItemMeta().getDisplayName().contains("リセット")){
					//経験値変更用のクラスを設定
					//経験値が足りなかったら処理を終了
					if(!expman.hasExp(10000)){
						player.sendMessage(ChatColor.RED + "必要な経験値が足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						return;
					}
					//経験値消費
					expman.changeExp(-10000);

					//リセット処理
					playerdata.activeskilldata.reset();
					//スキルポイント更新
					playerdata.activeskilldata.updataActiveSkillPoint(player, playerdata.level);
					//リセット音を流す
					player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1, (float) 0.1);
					//メッセージを流す
					player.sendMessage(ChatColor.LIGHT_PURPLE + "アクティブスキルポイントをリセットしました");
					//メニューを開く
					player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
				}
			}
			else if(itemstackcurrent.getType().equals(Material.GLASS)){
				if(playerdata.activeskilldata.skilltype == 0 && playerdata.activeskilldata.skillnum == 0
				&&playerdata.activeskilldata.assaulttype == 0 && playerdata.activeskilldata.assaultnum == 0
						){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に全ての選択は削除されています");
				}else{
					playerdata.activeskilldata.clearSellect(player);

				}
			}
			else if(itemstackcurrent.getType().equals(Material.BOOKSHELF)){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1, (float) 0.5);
				player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player));
				return;
			}
		}
	}
	//スキルエフェクトメニューの処理 + エフェクト開放の処理
	@EventHandler
	public void onPlayerClickActiveSkillEffectSellectEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}
		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズ終了
		if(topinventory.getSize() != 9 * 6){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキルエフェクト選択")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
				return;
			}else if(itemstackcurrent.getType().equals(Material.GLASS)){
				if(playerdata.activeskilldata.effectnum == 0){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else{
					playerdata.activeskilldata.effectnum = 0;
					player.sendMessage(ChatColor.GREEN + "エフェクト:未設定  が選択されました");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}
				return;
			}else if(itemstackcurrent.getType().equals(Material.BOOK_AND_QUILL)){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBuyRecordMenuData(player));
				return;
			}else{
				ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
				for(int i = 0; i < skilleffect.length ; i++){
					if(itemstackcurrent.getType().equals(skilleffect[i].getMaterial())){
						if(playerdata.activeskilldata.effectnum == skilleffect[i].getNum()){
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
							player.sendMessage(ChatColor.YELLOW + "既に選択されています");
						}else{
							playerdata.activeskilldata.effectnum = skilleffect[i].getNum();
							player.sendMessage(ChatColor.GREEN + "エフェクト:" + skilleffect[i].getName() + ChatColor.RESET + "" + ChatColor.GREEN + " が選択されました");
							player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
						}
					}
				}
				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
				for(int i = 0; i < premiumeffect.length ; i++){
					if(itemstackcurrent.getType().equals(premiumeffect[i].getMaterial())){
						if(playerdata.activeskilldata.effectnum == premiumeffect[i].getNum()){
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
							player.sendMessage(ChatColor.YELLOW + "既に選択されています");
						}else{
							playerdata.activeskilldata.effectnum = premiumeffect[i].getNum() + 100;
							player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "プレミアムエフェクト:" + premiumeffect[i].getName() + ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.BOLD + " が選択されました");
							player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
						}
					}
				}
			}


			//ここからエフェクト開放の処理
			if(itemstackcurrent.getType().equals(Material.BEDROCK)){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
				for(int i = 0; i < skilleffect.length ; i++){
					if(itemmeta.getDisplayName().contains(skilleffect[i].getName())){
						if(playerdata.activeskilldata.effectpoint < skilleffect[i].getUsePoint()){
							player.sendMessage(ChatColor.DARK_RED  + "エフェクトポイントが足りません");
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float)0.5);
						}else{
							skilleffect[i].setObtained(playerdata.activeskilldata.effectflagmap);
							player.sendMessage(ChatColor.LIGHT_PURPLE + "エフェクト：" + skilleffect[i].getName() + ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE+ "" + ChatColor.BOLD + "" + " を解除しました");
							player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
							playerdata.activeskilldata.effectpoint -= skilleffect[i].getUsePoint();
							player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player));
						}
					}
				}
			}
			//ここからプレミアムエフェクト開放の処理
			if(itemstackcurrent.getType().equals(Material.BEDROCK)){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
				for(int i = 0; i < premiumeffect.length ; i++){
					if(itemmeta.getDisplayName().contains(premiumeffect[i].getName())){
						if(playerdata.activeskilldata.premiumeffectpoint < premiumeffect[i].getUsePoint()){
							player.sendMessage(ChatColor.DARK_RED  + "プレミアムエフェクトポイントが足りません");
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float)0.5);
						}else{
							premiumeffect[i].setObtained(playerdata.activeskilldata.premiumeffectflagmap);
							player.sendMessage(ChatColor.LIGHT_PURPLE+ "" + ChatColor.BOLD + "プレミアムエフェクト：" + premiumeffect[i].getName() + ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE+ "" + ChatColor.BOLD + "" + " を解除しました");
							if(!sql.addPremiumEffectBuy(playerdata,premiumeffect[i])){
								player.sendMessage("購入履歴が正しく記録されませんでした。管理者に報告してください。");
							}
							player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
							playerdata.activeskilldata.premiumeffectpoint -= premiumeffect[i].getUsePoint();
							player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player));
						}
					}
				}
			}
		}
	}

	//スキル解放の処理
	@EventHandler
	public void onPlayerClickActiveSkillReleaseEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが36でない時終了
		if(topinventory.getSize() != 36){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキル選択")){
			event.setCancelled(true);
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(itemstackcurrent.getType().equals(Material.BEDROCK)){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				int skilllevel = 0;
				int skilltype = 0;
				if(itemmeta.getDisplayName().contains("エビフライ・ドライブ")){
					skilllevel = 4;
					skilltype = 1;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float)0.5);
					}else if(playerdata.activeskilldata.breakskill < 3){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(3,3) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float)0.5);
					}else{
						playerdata.activeskilldata.arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ホーリー・ショット")){
					skilllevel = 5;
					skilltype = 1;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ツァーリ・ボンバ")){
					skilllevel = 6;
					skilltype = 1;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アーク・ブラスト")){
					skilllevel = 7;
					skilltype = 1;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ファンタズム・レイ")){
					skilllevel = 8;
					skilltype = 1;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("スーパー・ノヴァ")){
					skilllevel = 9;
					skilltype = 1;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						if(playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.condenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("トム・ボウイ")){
					skilllevel = 4;
					skilltype = 2;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < 3){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(3,3) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("サンダー・ストーム")){
					skilllevel = 5;
					skilltype = 2;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("スターライト・ブレイカー")){
					skilllevel = 6;
					skilltype = 2;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アース・ディバイド")){
					skilllevel = 7;
					skilltype = 2;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ヘヴン・ゲイボルグ")){
					skilllevel = 8;
					skilltype = 2;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ディシジョン")){
					skilllevel = 9;
					skilltype = 2;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						if(playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.condenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("デュアル・ブレイク")){
					skilllevel = 1;
					skilltype = 3;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("トリアル・ブレイク")){
					skilllevel = 2;
					skilltype = 3;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("エクスプロージョン")){
					skilllevel = 3;
					skilltype = 3;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ミラージュ・フレア")){
					skilllevel = 4;
					skilltype = 3;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ドッ・カーン")){
					skilllevel = 5;
					skilltype = 3;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ギガンティック・ボム")){
					skilllevel = 6;
					skilltype = 3;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ブリリアント・デトネーション")){
					skilllevel = 7;
					skilltype = 3;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("レムリア・インパクト")){
					skilllevel = 8;
					skilltype = 3;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("エターナル・ヴァイス")){
					skilllevel = 9;
					skilltype = 3;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						if(playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.condenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ホワイト・ブレス")){
					skilllevel = 4;
					skilltype = 4;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < 3){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(3,3) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.condenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アブソリュート・ゼロ")){
					skilllevel = 5;
					skilltype = 4;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.condenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ダイアモンド・ダスト")){
					skilllevel = 6;
					skilltype = 4;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.condenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ラヴァ・コンデンセーション")){
					skilllevel = 7;
					skilltype = 4;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.condenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("モエラキ・ボールダーズ")){
					skilllevel = 8;
					skilltype = 4;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.condenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("エルト・フェットル")){
					skilllevel = 9;
					skilltype = 4;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.condenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						if(playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.multiskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アサルト・アーマー")){

				}


			}
		}
	}
	//マインスタックメニュー
	@EventHandler
	public void onPlayerClickMineStackMenuEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();

		int open_flag=-1;

		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが36でない時終了
		/*
		if(topinventory.getSize() != 36){
			return;
		}
		*/
		//インベントリサイズが54でない時終了
		if(topinventory.getSize() != 54){
			return;
		}


		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//インベントリ名が以下の時処理

		if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			if(SeichiAssist.DEBUG){
				player.sendMessage("MineStackSize = " + SeichiAssist.minestacklist.size());
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}

			//追加
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("MineStack") &&
					itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display-1));

				}
				/*
				if(itemmeta.getDisplayName().contains("MineStack2ページ目")){//移動するページの種類を判定
					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getMineStackMenu(player, 1));
				} else if(itemmeta.getDisplayName().contains("MineStack3ページ目")){//移動するページの種類を判定
					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getMineStackMenu(player, 2));
				}
				*/
				return;
			}

			//追加
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();

				if(itemmeta.getDisplayName().contains("MineStack") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
						int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

						//開く音を再生
						player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
						player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display-1));

					}
				/*
				if(itemmeta.getDisplayName().contains("MineStack1ページ目")){//移動するページの種類を判定
					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getMineStackMenu(player, 0));
				} else if(itemmeta.getDisplayName().contains("MineStack2ページ目")){//移動するページの種類を判定
					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getMineStackMenu(player, 1));
				}
				*/
				return;
			}

			if(itemstackcurrent.getType().equals(Material.IRON_PICKAXE)){
				// 対象ブロック自動スタック機能トグル
				playerdata.minestackflag = !playerdata.minestackflag;
				if(playerdata.minestackflag){
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "対象ブロック自動スタック機能:ON");
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "対象ブロック自動スタック機能:OFF");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.MineStackToggleMeta(playerdata,itemmeta));
			}

			else {
				for(int i=0; i<SeichiAssist.minestacklist.size(); i++){
					if(itemstackcurrent.getType().equals(SeichiAssist.minestacklist.get(i).getMaterial())
							&& itemstackcurrent.getDurability() == SeichiAssist.minestacklist.get(i).getDurability()){ //MaterialとサブIDが一致

						if(SeichiAssist.minestacklist.get(i).getNameloreflag()==false){
							playerdata.minestack.setNum(i, (giveMineStack(player,playerdata.minestack.getNum(i),new ItemStack(SeichiAssist.minestacklist.get(i).getMaterial(), 1, (short)SeichiAssist.minestacklist.get(i).getDurability() ))) );
							open_flag = (i+1)/45;
						} else { //名前と説明文がある
							//System.out.println("debug AA");
							//同じ名前の別アイテムに対応するためにインベントリの「解放レベル」を見る
							int level = SeichiAssist.config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel());
							int level_ = 0;
							String temp = null;
							for(int j=0; j<itemstackcurrent.getItemMeta().getLore().size(); j++){
								String lore = itemstackcurrent.getItemMeta().getLore().get(j);
								//System.out.println(j);
						        Pattern p = Pattern.compile(".*Lv[0-9]+以上でスタック可能.*");
						        Matcher m = p.matcher(lore);
						        if(m.matches()){
						        	//System.out.println(lore);
						        	String matchstr = lore.replaceAll("^.*Lv","");
						        	//System.out.println(matchstr);
						        	level_ = Integer.parseInt(matchstr.replaceAll("[^0-9]+","")); //数字以外を全て消す
						        	break;
						        }
							}
							//System.out.println(itemstackcurrent.getItemMeta().getLore());
							//System.out.println(SeichiAssist.minestacklist.get(i).getLore());
							//System.out.println(level + " " + level_);
							if(level==level_){
								//System.out.println("DEBUG!!!!");
								//System.out.println(itemstackcurrent.getItemMeta().getDisplayName());
								//System.out.println(SeichiAssist.minestacklist.get(i).getJapaneseName());
								String itemstack_name = itemstackcurrent.getItemMeta().getDisplayName();
								String minestack_name = SeichiAssist.minestacklist.get(i).getJapaneseName();
								itemstack_name = itemstack_name.replaceAll("§[0-9A-Za-z]","");
								minestack_name = minestack_name.replaceAll("§[0-9A-Za-z]","");
								//System.out.println(itemstack_name);
								//System.out.println(minestack_name);

								if(SeichiAssist.minestacklist.get(i).getGachatype()==-1){//ガチャアイテムにはない（がちゃりんご）
									if(itemstack_name.equals(minestack_name)){ //表記はアイテム名だけなのでアイテム名で判定
										playerdata.minestack.setNum(i, (giveMineStackNameLore(player,playerdata.minestack.getNum(i),new ItemStack(SeichiAssist.minestacklist.get(i).getMaterial(), 1, (short)SeichiAssist.minestacklist.get(i).getDurability()),-1)));
										open_flag = (i+1)/45;
									}
								} else { //ガチャアイテム(処理は同じでも念のためデバッグ用に分離)
									if(SeichiAssist.minestacklist.get(i).getGachatype()>=0){
										if(itemstack_name.equals(minestack_name)){ //表記はアイテム名だけなのでアイテム名で判定
											//盾、バナーの模様判定
											if( ( itemstackcurrent.getType().equals(Material.SHIELD) || (itemstackcurrent.getType().equals(Material.BANNER)) ) && SeichiAssist.minestacklist.get(i).getItemStack().getType().equals(itemstackcurrent.getType())){
												BlockStateMeta bs0 = (BlockStateMeta) itemstackcurrent.getItemMeta();
												Banner b0 = (Banner) bs0.getBlockState();
												List<org.bukkit.block.banner.Pattern> p0 = b0.getPatterns();

												BlockStateMeta bs1 = (BlockStateMeta) SeichiAssist.minestacklist.get(i).getItemStack().getItemMeta();
												Banner b1 = (Banner) bs1.getBlockState();
												List<org.bukkit.block.banner.Pattern> p1 = b1.getPatterns();

												if(p0.containsAll(p1)){
													playerdata.minestack.setNum(i, (giveMineStackNameLore(player,playerdata.minestack.getNum(i),new ItemStack(SeichiAssist.minestacklist.get(i).getMaterial(), 1, (short)SeichiAssist.minestacklist.get(i).getDurability()),SeichiAssist.minestacklist.get(i).getGachatype())));
													open_flag = (i+1)/45;
												}
											} else {
												playerdata.minestack.setNum(i, (giveMineStackNameLore(player,playerdata.minestack.getNum(i),new ItemStack(SeichiAssist.minestacklist.get(i).getMaterial(), 1, (short)SeichiAssist.minestacklist.get(i).getDurability()),SeichiAssist.minestacklist.get(i).getGachatype())));
												open_flag = (i+1)/45;
											}
										}
									}
								}
							}
						}
					}
				}
			}

			if(open_flag!=-1){
				player.openInventory(MenuInventoryData.getMineStackMenu(player, open_flag));
				open_flag=-1;
			}
		}
	}

	//ランキングメニュー
	@EventHandler
	public void onPlayerClickSeichiRankingMenuEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが36でない時終了
		if(topinventory.getSize() != 54){
			return;
		}
		Player player = (Player)he;

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地神ランキング")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown")){
					ItemMeta itemmeta = itemstackcurrent.getItemMeta();
					if(itemmeta.getDisplayName().contains("整地紳ランキング３ページ目")){
						//開く音を再生
						player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
						player.openInventory(MenuInventoryData.getRankingList3(player));
						return;
					}
					else{
						//開く音を再生
						player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
						player.openInventory(MenuInventoryData.getRankingList2(player));
						return;
					}
			}
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("整地紳ランキング２ページ目")){
					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList2(player));
					return;
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getRankingList(player));
				return;
			}
		}
	}
	//購入履歴メニュー
	@EventHandler
	public void onPlayerClickPremiumLogMenuEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが36でない時終了
		if(topinventory.getSize() != 36){
			return;
		}
		Player player = (Player)he;

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.BLUE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player));
				return;
			}
		}
	}
	//minestackの1stack付与の処理
	private int giveMineStack(Player player,int minestack,Material type){
		if(minestack >= type.getMaxStackSize()){ //スタックサイズが64でないアイテムにも対応
			ItemStack itemstack = new ItemStack(type,type.getMaxStackSize());
			if(!Util.isPlayerInventryFill(player)){
				Util.addItem(player,itemstack);
			}else{
				Util.dropItem(player,itemstack);
			}
			minestack -= type.getMaxStackSize();
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
		}else if(minestack == 0){
			return minestack;
		}else{
			ItemStack itemstack = new ItemStack(type,minestack);
			if(!Util.isPlayerInventryFill(player)){
				Util.addItem(player,itemstack);
			}else{
				Util.dropItem(player,itemstack);
			}
			minestack -= minestack;
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
		}
		return minestack;
	}

	//minestackの1stack付与 ItemStack版
	private int giveMineStack(Player player,int minestack,ItemStack itemstack){
		if(minestack >= itemstack.getMaxStackSize()){ //スタック数が64でないアイテムにも対応
			itemstack.setAmount(itemstack.getMaxStackSize());
			if(!Util.isPlayerInventryFill(player)){
				Util.addItem(player,itemstack);
			}else{
				Util.dropItem(player,itemstack);
			}
			minestack -= itemstack.getMaxStackSize();
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
		}else if(minestack == 0){
			return minestack;
		}else{
			itemstack.setAmount(minestack);
			if(!Util.isPlayerInventryFill(player)){
				Util.addItem(player,itemstack);
			}else{
				Util.dropItem(player,itemstack);
			}
			minestack -= minestack;
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
		}
		return minestack;
	}

	//minestackの1stack付与 ItemStack版(名前、説明文付き専用)
	private int giveMineStackNameLore(Player player,int minestack,ItemStack itemstack, int num){
		ItemMeta meta = itemstack.getItemMeta();
		if(num==-1){//がちゃりんごの場合
			//ItemStack gachaimo;
			//ItemMeta meta;
			itemstack = new ItemStack(Material.GOLDEN_APPLE,1);
			meta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
			meta.setDisplayName(Util.getGachaimoName());
			List<String> lore = Util.getGachaimoLore();
			meta.setLore(lore);
			itemstack.setItemMeta(meta);

			meta.setDisplayName(Util.getGachaimoName());
			meta.setLore(Util.getGachaimoLore());
		} else if(num>=0){ //他のガチャアイテムの場合 -2以下は他のアイテムに対応させる
			MineStackGachaData g = new MineStackGachaData(SeichiAssist.msgachadatalist.get(num));
			UUID uuid = player.getUniqueId();
			PlayerData playerdata = playermap.get(uuid);
			String name = playerdata.name;
			if(g.probability < 0.1){ //ガチャアイテムに名前を付与
				g.addname(name);
				//player.sendMessage("Debug!");

			}
			itemstack = new ItemStack(g.itemstack); //この1行だけで問題なく動くのかテスト
		}
		if(minestack >= itemstack.getMaxStackSize()){ //スタック数が64でないアイテムにも対応
			itemstack.setAmount(itemstack.getMaxStackSize());
			if(!Util.isPlayerInventryFill(player)){
				Util.addItem(player,itemstack);
			}else{
				Util.dropItem(player,itemstack);
			}
			minestack -= itemstack.getMaxStackSize();
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
		}else if(minestack == 0){
			return minestack;
		}else{
			itemstack.setAmount(minestack);
			if(!Util.isPlayerInventryFill(player)){
				Util.addItem(player,itemstack);
			}else{
				Util.dropItem(player,itemstack);
			}
			minestack -= minestack;
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
		}
		return minestack;
	}


    //ガチャ交換システム
    @EventHandler
    public void onGachaTradeEvent(InventoryCloseEvent event){
        Player player = (Player)event.getPlayer();
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);
		//エラー分岐
		if(playerdata == null){
			return;
		}
		String name = playerdata.name;
        Inventory inventory = event.getInventory();

        //インベントリサイズが36でない時終了
        if(inventory.getSize() != 36){
            return;
        }
        if(inventory.getTitle().equals(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "交換したい景品を入れてください")){
            //PlayerInventory pinventory = player.getInventory();
            //ItemStack itemstack = pinventory.getItemInMainHand();
            int givegacha = 0;
            /*この分岐処理必要かなぁ…とりあえずコメントアウト
            if(itemstack.getType().equals(Material.STICK)){
            }
            */
            /*
             * step1 for文でinventory内に対象商品がないか検索
             * あったらdurabilityに応じてgivegachaを増やし、非対象商品は返却boxへ
             */
            //ガチャ景品交換インベントリの中身を取得
            ItemStack[] item = inventory.getContents();
            //ドロップ用アイテムリスト(返却box)作成
            List<ItemStack> dropitem = new ArrayList<ItemStack>();
            //カウント用
            int big = 0;
            int reg = 0;
            //for文で１個ずつ対象アイテムか見る
            //ガチャ景品交換インベントリを一個ずつ見ていくfor文
            for (ItemStack m : item) {
                //無いなら次へ
                if(m == null){
                    continue;
                }else if(SeichiAssist.gachamente){
                    //ガチャシステムメンテナンス中は全て返却する
                    dropitem.add(m);
                    continue;
                }else if(!m.hasItemMeta()){
                    //丁重にお返しする
                    dropitem.add(m);
                    continue;
                }else if(!m.getItemMeta().hasLore()){
                    //丁重にお返しする
                    dropitem.add(m);
                    continue;
                }else if(m.getType().equals(Material.SKULL_ITEM)){
                    //丁重にお返しする
                    dropitem.add(m);
                    continue;
                }
                //ガチャ景品リストにアイテムがあった時にtrueになるフラグ
                boolean flag = false;
                //ガチャ景品リストを一個ずつ見ていくfor文
                for(GachaData gachadata : gachadatalist){
                    if(!gachadata.itemstack.hasItemMeta()){
                        continue;
                    }else if(!gachadata.itemstack.getItemMeta().hasLore()){
                        continue;
                    }
                    //ガチャ景品リストにある商品の場合(Lore=説明文と表示名で判別),無い場合はアイテム返却
                    if(gachadata.compare(m,name)){
                    	if(SeichiAssist.DEBUG){
                    		player.sendMessage(gachadata.itemstack.getItemMeta().getDisplayName());
                    	}
                    //if(gachadata.itemstack.getItemMeta().getLore().equals(m.getItemMeta().getLore())
                           // &&gachadata.itemstack.getItemMeta().getDisplayName().equals(m.getItemMeta().getDisplayName())){
                        flag = true;
                        int amount = m.getAmount();
                        if(gachadata.probability < 0.001){
                            //ギガンティック大当たりの部分
                            //ガチャ券に交換せずそのままアイテムを返す
                            dropitem.add(m);
                        }else if(gachadata.probability < 0.01){
                            //大当たりの部分
                            givegacha += (12*amount);
                            big++;
                        }else if(gachadata.probability < 0.1){
                            //当たりの部分
                            givegacha += (3*amount);
                            reg++;
                        }else{
                            //それ以外アイテム返却(経験値ポーションとかがここにくるはず)
                            dropitem.add(m);
                        }
                        break;
                    }
                }
                //ガチャ景品リストに対象アイテムが無かった場合
                if(!flag){
                    //丁重にお返しする
                    dropitem.add(m);
                }
            }
            //ガチャシステムメンテナンス中は全て返却する
            if(SeichiAssist.gachamente){
                player.sendMessage(ChatColor.RED + "ガチャシステムメンテナンス中の為全てのアイテムを返却します");
            }else if(!(big > 0)&&!(reg > 0)){
                player.sendMessage(ChatColor.YELLOW + "景品を認識しませんでした。全てのアイテムを返却します");
            }else{
                player.sendMessage(ChatColor.GREEN + "大当たり景品を" + big + "個、当たり景品を" + reg + "個認識しました");
            }
            /*
             * step2 非対象商品をインベントリに戻す
             */
            for(ItemStack m : dropitem){
                if(!Util.isPlayerInventryFill(player)){
                    Util.addItem(player,m);
                }else{
                    Util.dropItem(player,m);
                }
            }
            /*
             * step3 ガチャ券をインベントリへ
             */
            ItemStack skull = Util.getExchangeskull(Util.getName(player));
            int count = 0;
            while(givegacha > 0){
                if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
                    Util.addItem(player,skull);
                }else{
                    Util.dropItem(player,skull);
                }
                givegacha--;
                count++;
            }
            if(count > 0){
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                player.sendMessage(ChatColor.GREEN + ""+count+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "を受け取りました");
            }
        }

    }
    //実績メニューの処理
    @EventHandler
    public void onPlayerClickTitleMenuEvent(InventoryClickEvent event){
    	//外枠のクリック処理なら終了
    	if(event.getClickedInventory() == null){
    		return;
    	}

    	ItemStack itemstackcurrent = event.getCurrentItem();
    	InventoryView view = event.getView();
    	HumanEntity he = view.getPlayer();
    	//インベントリを開けたのがプレイヤーではない時終了
    	if(!he.getType().equals(EntityType.PLAYER)){
    		return;
    	}

    	Inventory topinventory = view.getTopInventory();
    	//インベントリが存在しない時終了
    	if(topinventory == null){
    		return;
    	}
    	//インベントリサイズが36でない時終了
    	if(topinventory.getSize() != 36){
    		return;
    	}
    	Player player = (Player)he;
    	UUID uuid = player.getUniqueId();
    	PlayerData playerdata = playermap.get(uuid);

    	//経験値変更用のクラスを設定
    	ExperienceManager expman = new ExperienceManager(player);


    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績・二つ名システム")){
    		event.setCancelled(true);

    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		//表示内容をLVと二つ名で切り替えるための処理
			if(itemstackcurrent.getType().equals(Material.REDSTONE_TORCH_ON)){
				if(playerdata.displayTypeLv){
					playerdata.displayTypeLv = false;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.openInventory(MenuInventoryData.getTitleMenuData(player));
				}else{
					playerdata.displayTypeLv = true ;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.openInventory(MenuInventoryData.getTitleMenuData(player));
				}
			}

			//実績「整地神ランキング」を開く
			else if(itemstackcurrent.getType().equals(Material.DIAMOND_PICKAXE)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleRankData(player));
			}

    		//ホームメニューに戻る
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}
    	}


    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地神ランキング」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		if(itemstackcurrent.getType().equals(Material.BEDROCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
    			player.openInventory(MenuInventoryData.getTitleRankData(player));
    		}
    		else if (itemstackcurrent.getType().equals(Material.DIAMOND_BLOCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			if(itemmeta.getDisplayName().contains("No1001「"+ SeichiAssist.config.getTitle(1001) +"」")){
    				playerdata.displayTitleNo = 1001 ;
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle(1001) +"」が設定されました。");
    				player.openInventory(MenuInventoryData.getTitleRankData(player));
    			}
    			else if(itemmeta.getDisplayName().contains("No1002「"+ SeichiAssist.config.getTitle(1002) +"」")){
    				playerdata.displayTitleNo = 1002 ;
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle(1002) +"」が設定されました。");
    				player.openInventory(MenuInventoryData.getTitleRankData(player));
    			}
    			else if(itemmeta.getDisplayName().contains("No1003「"+ SeichiAssist.config.getTitle(1003) +"」")){
    				playerdata.displayTitleNo = 1003 ;
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle(1003) +"」が設定されました。");
    				player.openInventory(MenuInventoryData.getTitleRankData(player));
    			}
    			else if(itemmeta.getDisplayName().contains("No1004「"+ SeichiAssist.config.getTitle(1004) +"」")){
    				playerdata.displayTitleNo = 1004 ;
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle(1004) +"」が設定されました。");
    				player.openInventory(MenuInventoryData.getTitleRankData(player));
    			}
    			else if(itemmeta.getDisplayName().contains("No1005「"+ SeichiAssist.config.getTitle(1005) +"」")){
    				playerdata.displayTitleNo = 1005 ;
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle(1005) +"」が設定されました。");
    				player.openInventory(MenuInventoryData.getTitleRankData(player));
    			}
    			else if(itemmeta.getDisplayName().contains("No1006「"+ SeichiAssist.config.getTitle(1006) +"」")){
    				playerdata.displayTitleNo = 1006 ;
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle(1006) +"」が設定されました。");
    				player.openInventory(MenuInventoryData.getTitleRankData(player));
    			}
    			else if(itemmeta.getDisplayName().contains("No1007「"+ SeichiAssist.config.getTitle(1007) +"」")){
    				playerdata.displayTitleNo = 1007 ;
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle(1007) +"」が設定されました。");
    				player.openInventory(MenuInventoryData.getTitleRankData(player));
    			}
    			else if(itemmeta.getDisplayName().contains("No1008「"+ SeichiAssist.config.getTitle(1008) +"」")){
    				playerdata.displayTitleNo = 1008 ;
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle(1008) +"」が設定されました。");
    				player.openInventory(MenuInventoryData.getTitleRankData(player));
    			}
    			else if(itemmeta.getDisplayName().contains("No1009「"+ SeichiAssist.config.getTitle(1009) +"」")){
    				playerdata.displayTitleNo = 1009 ;
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle(1009) +"」が設定されました。");
    				player.openInventory(MenuInventoryData.getTitleRankData(player));
    			}

    		}
    		//ホームメニューに戻る
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}
    	}
    }
}