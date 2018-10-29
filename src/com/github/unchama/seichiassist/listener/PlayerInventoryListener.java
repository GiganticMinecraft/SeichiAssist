package com.github.unchama.seichiassist.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.unchama.seasonalevents.events.valentine.Valentine;
import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.ActiveSkillInventoryData;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.ItemData;
import com.github.unchama.seichiassist.data.Mana;
import com.github.unchama.seichiassist.data.MenuInventoryData;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.minestack.HistoryData;
import com.github.unchama.seichiassist.task.CoolDownTaskRunnable;
import com.github.unchama.seichiassist.task.TitleUnlockTaskRunnable;
import com.github.unchama.seichiassist.task.VotingFairyTaskRunnable;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class PlayerInventoryListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	List<GachaData> gachadatalist = SeichiAssist.gachadatalist;
	SeichiAssist plugin = SeichiAssist.plugin;
	private Config config = SeichiAssist.config;
	private Sql sql = SeichiAssist.sql;
	//サーバ選択メニュー
	@EventHandler
	public void onPlayerClickServerSwitchMenuEvent(InventoryClickEvent event){
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
		if(topinventory.getSize() != 2*9){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_RED + "" + ChatColor.BOLD + "サーバーを選択してください")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}
			ItemMeta meta = itemstackcurrent.getItemMeta();

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			ByteArrayDataOutput byteArrayDataOutput = ByteStreams
					.newDataOutput();
			//ページ変更処理
			if(meta.getDisplayName().contains("アルカディアサーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s1");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("エデンサーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s2");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("ヴァルハラサーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s3");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("第1整地専用特設サーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s5");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("第2整地専用特設サーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s6");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("クリエイティブサーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("cre");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("イベントサーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("eve");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("第1βテストサーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("g1");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("第2βテストサーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("g2");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("公共施設サーバ")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s7");
				player.sendPluginMessage(SeichiAssist.plugin, "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}
		}
	}
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
			if (itemstackcurrent.getType().equals(Material.NETHER_STAR)) {
				if (itemstackcurrent.getItemMeta().getDisplayName().equals(
						org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.UNDERLINE + "" + org.bukkit.ChatColor.BOLD + "サーバー間移動メニュー")) {
					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.6F, 1.5F);
					player.openInventory(MenuInventoryData.getServerSwitchMenu(player));
				}

				if (itemstackcurrent.getItemMeta().getDisplayName().equals(
						org.bukkit.ChatColor.YELLOW + "" + org.bukkit.ChatColor.UNDERLINE + "" + org.bukkit.ChatColor.BOLD + "ロビーサーバーへ移動")) {
					// hubコマンド実行
					// player.chat("/hub");
					player.closeInventory();
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.RESET + "" +  ChatColor.GRAY + "/hubと入力してEnterを押してください");
				}

			}else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
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
				player.openInventory(MenuInventoryData.getMineStackMainMenu(player));
				return;
			}
			//スキルメニューを開く
			else if(itemstackcurrent.getType().equals(Material.ENCHANTED_BOOK)){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				//アクティブスキルとパッシブスキルの分岐
				if(itemmeta.getDisplayName().contains("アクティブ")){
					player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
				}else if(itemmeta.getDisplayName().contains("パッシブ")){
					//player.sendMessage("未実装ナリよ");
					player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player));
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 0.8);
				return;
			}
			//整地神番付を開く
			else if(itemstackcurrent.getType().equals(Material.COOKIE) && itemstackcurrent.getItemMeta().getDisplayName().contains("整地神")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getRankingList(player,0));
				return;
			}
			//整地神番付を開く
			else if(itemstackcurrent.getType().equals(Material.COOKIE) && itemstackcurrent.getItemMeta().getDisplayName().contains("ログイン神")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getRankingList_playtick(player,0));
				return;
			}
			//整地神番付を開く
			else if(itemstackcurrent.getType().equals(Material.COOKIE) && itemstackcurrent.getItemMeta().getDisplayName().contains("投票神")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getRankingList_p_vote(player,0));
				return;
			}
			/*
			else if(itemstackcurrent.getType().equals(Material.COOKIE) && itemstackcurrent.getItemMeta().getDisplayName().contains("寄付神")){
				if(SeichiAssist.DEBUG){
					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(player,0));
					return;
				}
			}
			*/

			//溜まったガチャ券をインベントリへ
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM)
					&& itemstackcurrent.getItemMeta().getDisplayName().equals(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地報酬ガチャ券を受け取る")){
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
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM)
					&& itemstackcurrent.getItemMeta().getDisplayName().equals(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "運営からのガチャ券を受け取る")){

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
				//バレンタイン中(イベント中かどうかの判断はSeasonalEvent側で行う)
				skullmeta = Valentine.playerHeadLore(skullmeta);
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
                /* effectflagは0->無制限,1->200,2->400,3->600,4->off
                 * これを0->無制限,1->127,2->200,3->400,4->600,5->offに変更する。
                 */
				playerdata.effectflag = (playerdata.effectflag + 1) % 6;
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

				// 採掘速度上昇量計算
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

				int maxSpeed = 0;
				if (playerdata.effectflag == 0) {
                    maxSpeed = 25565;
                    player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON");
                } else if (playerdata.effectflag == 1) {
				    maxSpeed = 127;
				    player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(127制限)");
				} else if (playerdata.effectflag == 2) {
					maxSpeed = 200;
					player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(200制限)");
				} else if (playerdata.effectflag == 3) {
					maxSpeed = 400;
					player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(400制限)");
				} else if (playerdata.effectflag == 4) {
					maxSpeed = 600;
					player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(600制限)");
				} else {
					player.sendMessage(ChatColor.RED + "採掘速度上昇効果:OFF");
				}

				//effect追加の処理
				//実際のeffect値が0より小さいときはeffectを適用しない
				if(minespeedlv < 0 || maxSpeed == 0){
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true);
				}else{
					if(minespeedlv > maxSpeed) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, maxSpeed, false, false), true);
					}else{
						player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, minespeedlv, false, false), true);
					}
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

			//全体通知トグル
			else if(itemstackcurrent.getType().equals(Material.JUKEBOX)){
				if(playerdata.everysoundflag && playerdata.everymessageflag){
					playerdata.everysoundflag = !playerdata.everysoundflag;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "消音可能な全体通知音を消音します");
				}
				else if(!playerdata.everysoundflag && playerdata.everymessageflag){
					playerdata.everymessageflag = !playerdata.everymessageflag;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "非表示可能な全体メッセージを非表示にします");
				}
				else {
					playerdata.everysoundflag = !playerdata.everysoundflag;
					playerdata.everymessageflag = !playerdata.everymessageflag;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.GREEN + "非表示/消音設定を解除しました");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.dispWinSoundToggleMeta(playerdata,itemmeta));
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

			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && itemstackcurrent.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.name + "の統計データ")){
				// 整地量表示トグル
				playerdata.expbar.setVisible(!playerdata.expbar.isVisible());
				if(playerdata.expbar.isVisible()){
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "整地量バー表示");
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "整地量バー非表示");
				}
				SkullMeta skullmeta = (SkullMeta)itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.dispExpBarToggleMeta(playerdata,skullmeta));
			}

			else if(itemstackcurrent.getType().equals(Material.BEACON)){
				// spawnコマンド実行
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/spawn");
			}
			//HomeMenu
			else if(itemstackcurrent.getType().equals(Material.BED)){
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 1.5);
				player.openInventory(MenuInventoryData.getHomeMenuData(player));
			}

			else if(itemstackcurrent.getType().equals(Material.WORKBENCH)){
				// /fc craftコマンド実行
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/fc craft");
			}

			else if(itemstackcurrent.getType().equals(Material.BOOK)){
				// wikiリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "http://seichi.click");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType().equals(Material.PAPER)){
				// 運営方針とルールリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "https://seichi.click/wiki/%E9%81%8B%E5%96%B6%E6%96%B9%E9%87%9D%E3%81%A8%E3%83%AB%E3%83%BC%E3%83%AB");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType().equals(Material.MAP)){
				// 鯖マップリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "https://seichi.click/wiki/DynmapLinks");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();

			}

			else if(itemstackcurrent.getType().equals(Material.SIGN)){
				//JMSリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "https://minecraft.jp/servers/54d3529e4ddda180780041a7"
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

			else if(itemstackcurrent.getType().equals(Material.DIAMOND_ORE)){
				//鉱石・交換券変換システムを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 0.5);
				//インベントリを開く
				player.openInventory(SeichiAssist.plugin.getServer().createInventory(null, 9*4 ,ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "交換したい鉱石を入れてください"));
			}

			else if(itemstackcurrent.getType().equals(Material.GOLDEN_APPLE)){
				//椎名林檎変換システムを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 0.5);
				//インベントリを開く
				player.openInventory(SeichiAssist.plugin.getServer().createInventory(null, 9*4 ,ChatColor.GOLD + "" + ChatColor.BOLD + "椎名林檎と交換したい景品を入れてネ"));
			}

			// インベントリ共有ボタン
			else if(itemstackcurrent.getType().equals(Material.TRAPPED_CHEST)&&
					itemstackcurrent.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "インベントリ共有")){
				player.chat("/shareinv");
				itemstackcurrent.setItemMeta(MenuInventoryData.dispShareInvMeta(playerdata));
			}

			else if(itemstackcurrent.getType().equals(Material.DIAMOND)){
				//投票ptメニューを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				//インベントリを開く
				player.openInventory(MenuInventoryData.getVotingMenuData(player));
			} else if (itemstackcurrent.getType().equals(Material.TRAPPED_CHEST)) {
				if (!Valentine.isInEvent) {
					return;
				}
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, (float) 0.5);
				Valentine.giveChoco(player);
				playerdata.hasChocoGave = true;
				player.sendMessage(ChatColor.AQUA + "チョコチップクッキーを付与しました。");
				player.openInventory(MenuInventoryData.getMenuData(player));
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
		//ExperienceManager expman = new ExperienceManager(player);


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

			else if(itemstackcurrent.getType().equals(Material.DIAMOND_AXE)){
				playerdata.chestflag = false;
				player.sendMessage(ChatColor.GREEN + "スキルでのチェスト破壊を無効化しました。");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player));
			}
			else if(itemstackcurrent.getType().equals(Material.CHEST)){
				playerdata.chestflag = true;
				player.sendMessage(ChatColor.RED + "スキルでのチェスト破壊を有効化しました。");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player));
			}


			else if(itemstackcurrent.getType().equals(Material.STICK)){
				player.sendMessage(ChatColor.WHITE + "パッシブスキル:" + ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Gigantic" + ChatColor.RED + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Berserk" + ChatColor.WHITE + "はレベル10以上から使用可能です");
				player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
			}

			else if(itemstackcurrent.getType().equals(Material.WOOD_SWORD) || itemstackcurrent.getType().equals(Material.STONE_SWORD) || itemstackcurrent.getType().equals(Material.GOLD_SWORD) || itemstackcurrent.getType().equals(Material.IRON_SWORD) || itemstackcurrent.getType().equals(Material.DIAMOND_SWORD)){
				if(!playerdata.isGBStageUp){
					player.sendMessage(ChatColor.RED + "進化条件を満たしていません");
				}else {
					player.openInventory(MenuInventoryData.getGiganticBerserkEvolutionMenu(player));
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
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
		//インベントリサイズが45でない時終了
		if(topinventory.getSize() != 45){
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
							player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
							playerdata.activeskilldata.skilltype = 0 ;
							playerdata.activeskilldata.skillnum = 0 ;
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
						player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
						playerdata.activeskilldata.skilltype = 0 ;
						playerdata.activeskilldata.skillnum = 0 ;
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
						player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
						playerdata.activeskilldata.skilltype = 0 ;
						playerdata.activeskilldata.skillnum = 0 ;
					}else{
						playerdata.activeskilldata.updataSkill(player,type,skilllevel,1);
						player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + name + "  が選択されました");
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}
			//CONDENSKILL
			//WATER
			type = ActiveSkill.WATERCONDENSE.gettypenum();
			for(skilllevel = 7;skilllevel <= 9 ; skilllevel++){
				name = ActiveSkill.WATERCONDENSE.getName(skilllevel);
				if(itemstackcurrent.getType().equals(ActiveSkill.WATERCONDENSE.getMaterial(skilllevel))){
					if(playerdata.activeskilldata.assaulttype == type
							&& playerdata.activeskilldata.assaultnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
						playerdata.activeskilldata.assaulttype = 0 ;
						playerdata.activeskilldata.assaultnum = 0 ;
					}else{
						playerdata.activeskilldata.updataAssaultSkill(player,type,skilllevel,1);
						player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + name + "  が選択されました");
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}

			//LAVA
			type = ActiveSkill.LAVACONDENSE.gettypenum();
			for(skilllevel = 7;skilllevel <= 9 ; skilllevel++){
				name = ActiveSkill.LAVACONDENSE.getName(skilllevel);
				if(itemstackcurrent.getType().equals(ActiveSkill.LAVACONDENSE.getMaterial(skilllevel))){
					if(playerdata.activeskilldata.assaulttype == type
							&& playerdata.activeskilldata.assaultnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
						playerdata.activeskilldata.assaulttype = 0 ;
						playerdata.activeskilldata.assaultnum = 0 ;
					}else{
						playerdata.activeskilldata.updataAssaultSkill(player,type,skilllevel,1);
						player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + name + "  が選択されました");
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}

			type = ActiveSkill.FLUIDCONDENSE.gettypenum();
			skilllevel = 10;
			if(itemstackcurrent.getType().equals(ActiveSkill.FLUIDCONDENSE.getMaterial(skilllevel))){
				if(playerdata.activeskilldata.assaultnum == skilllevel && playerdata.activeskilldata.assaulttype == type){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
					playerdata.activeskilldata.assaulttype = 0 ;
					playerdata.activeskilldata.assaultnum = 0 ;
				}else{
					playerdata.activeskilldata.updataAssaultSkill(player,type,skilllevel,1);
					player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + "ヴェンダー・ブリザード" + " が選択されました");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}
			}

			//アサルトアーマー
			type = ActiveSkill.ARMOR.gettypenum();
			skilllevel = 10;
			if(itemstackcurrent.getType().equals(ActiveSkill.ARMOR.getMaterial(skilllevel))){
				if(playerdata.activeskilldata.assaultnum == skilllevel && playerdata.activeskilldata.assaulttype == type){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
					playerdata.activeskilldata.assaulttype = 0 ;
					playerdata.activeskilldata.assaultnum = 0 ;
				}else{
					playerdata.activeskilldata.updataAssaultSkill(player,type,skilllevel,1);
					player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + "アサルト・アーマー" + " が選択されました");
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
					player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
				player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
		if(topinventory.getSize() != 45){
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						if(playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						if(playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
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
						if(playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ホワイト・ブレス")){
					skilllevel = 7;
					skilltype = 4;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.breakskill < 3){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(3,3) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.watercondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アブソリュート・ゼロ")){
					skilllevel = 8;
					skilltype = 4;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.watercondenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.watercondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ダイアモンド・ダスト")){
					skilllevel = 9;
					skilltype = 4;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.watercondenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.watercondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						if(playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ラヴァ・コンデンセーション")){
					skilllevel = 7;
					skilltype = 5;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}
					/*else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}*/
					else if(playerdata.activeskilldata.breakskill < 3){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(3,3) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.lavacondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("モエラキ・ボールダーズ")){
					skilllevel = 8;
					skilltype = 5;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.lavacondenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.lavacondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("エルト・フェットル")){
					skilllevel = 9;
					skilltype = 5;
					if(playerdata.activeskilldata.skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.activeskilldata.lavacondenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.lavacondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						if(playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アサルト・アーマー")){

				}else if(itemmeta.getDisplayName().contains("ヴェンダー・ブリザード")){
					if(playerdata.activeskilldata.skillpoint < 110){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.activeskilldata.fluidcondenskill = 10;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + "ヴェンダー・ブリザードを解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}


			}
		}
	}

	//マインスタックメインメニュー
	@EventHandler
	public void onPlayerClickMineStackMainMenuEvent(InventoryClickEvent event){
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

		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackメインメニュー")){
		//if(topinventory.getTitle().contains("MineStack")){
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

			//ページ変更処理
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}

			if (itemstackcurrent.getType().equals(Material.STONE) && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 0));
				return;
			}

			if (itemstackcurrent.getType().equals(Material.ENDER_PEARL) && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 1));
				return;
			}

			if (itemstackcurrent.getType().equals(Material.SEEDS) && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 2));
				return;
			}

			if (itemstackcurrent.getType().equals(Material.SMOOTH_BRICK) && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 3));
				return;
			}

			if (itemstackcurrent.getType().equals(Material.REDSTONE) && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 4));
				return;
			}

			if (itemstackcurrent.getType().equals(Material.GOLDEN_APPLE) && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 5));
				return;
			}

			if (itemstackcurrent.getType().equals(Material.COMPASS)) {
			    /*
				player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, (float) 1);
				playerdata.isSearching = true;
				player.closeInventory();
				player.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "<MineStackアイテム検索>");
				player.sendMessage(ChatColor.GRAY + "検索したいアイテムの名前を" + ChatColor.RED + "日本語で" + ChatColor.GRAY + "入力しよう");
				player.sendMessage(ChatColor.GRAY + "前方一部分でも大丈夫！(前方一致で検索します)");
				player.sendMessage(ChatColor.GREEN + "検索を終了したいときはendと入力してください");
				*/
			}

			for (HistoryData data : playerdata.hisotryData.getHistoryList()) {
				if (itemstackcurrent.getType().equals(data.obj.getMaterial())
						&& itemstackcurrent.getDurability() == data.obj.getDurability()) { //MaterialとサブIDが一致

					if (!data.obj.getNameloreflag()) {
						/* loreが無いとき */

						//同じ名前の別アイテムに対応するためにインベントリの「解放レベル」を見る
						//このアイテムの解放レベル
						int level = SeichiAssist.config.getMineStacklevel(data.obj.getLevel());
						int level_ = 0;
						//String temp = null;
						for (int j = 0; j < itemstackcurrent.getItemMeta().getLore().size(); j++) {
							/* loreを1行ずつ見ていく。Lv表記を抽出し,level_変数に代入しておく */
							String lore = itemstackcurrent.getItemMeta().getLore().get(j);
							Pattern p = Pattern.compile(".*Lv[0-9]+以上でスタック可能.*");
							Matcher m = p.matcher(lore);
							if (m.matches()) {
								String matchstr = lore.replaceAll("^.*Lv","");
								level_ = Integer.parseInt(matchstr.replaceAll("[^0-9]+","")); //数字以外を全て消す
								break;
							}
						}

						if (level==level_) {
							/* 開放レベルとクリックしたMineStackボタンのLvが同じとき */
							String itemstack_name = itemstackcurrent.getItemMeta().getDisplayName();
							String minestack_name = data.obj.getJapaneseName();
							itemstack_name = itemstack_name.replaceAll("§[0-9A-Za-z]","");
							minestack_name = minestack_name.replaceAll("§[0-9A-Za-z]","");
							if (itemstack_name.equals(minestack_name)) { //表記はアイテム名だけなのでアイテム名で判定
								playerdata.minestack.setNum(data.index, (giveMineStack(player, playerdata.minestack.getNum(data.index) ,new ItemStack(data.obj.getMaterial(), 1, (short)data.obj.getDurability()))));
							}
						}
					} else if (data.obj.getNameloreflag() && itemstackcurrent.getItemMeta().hasDisplayName()) { //名前と説明文がある
						//System.out.println("debug AA");
						//同じ名前の別アイテムに対応するためにインベントリの「解放レベル」を見る
						int level = SeichiAssist.config.getMineStacklevel(data.obj.getLevel());
						int level_ = 0;
						//String temp = null;
						for (int j = 0; j < itemstackcurrent.getItemMeta().getLore().size(); j++){
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
							String minestack_name = data.obj.getJapaneseName();
							itemstack_name = itemstack_name.replaceAll("§[0-9A-Za-z]","");
							minestack_name = minestack_name.replaceAll("§[0-9A-Za-z]","");
							//System.out.println(itemstack_name);
							//System.out.println(minestack_name);

							if (data.obj.getGachatype() == -1) {//ガチャアイテムにはない（がちゃりんご）
								if (itemstack_name.equals(minestack_name)) { //表記はアイテム名だけなのでアイテム名で判定
									playerdata.minestack.setNum(data.index, (giveMineStackNameLore(player,playerdata.minestack.getNum(data.index),new ItemStack(data.obj.getMaterial(), 1, (short)data.obj.getDurability()),-1)));
								}
							} else { //ガチャアイテム(処理は同じでも念のためデバッグ用に分離)
								if (data.obj.getGachatype()>=0) {
									if (itemstack_name.equals(minestack_name)) { //表記はアイテム名だけなのでアイテム名で判定
										//盾、バナーの模様判定
										if ((itemstackcurrent.getType().equals(Material.SHIELD) || (itemstackcurrent.getType().equals(Material.BANNER)) ) && data.obj.getItemStack().getType().equals(itemstackcurrent.getType())){
											BlockStateMeta bs0 = (BlockStateMeta) itemstackcurrent.getItemMeta();
											Banner b0 = (Banner) bs0.getBlockState();
											List<org.bukkit.block.banner.Pattern> p0 = b0.getPatterns();

											BlockStateMeta bs1 = (BlockStateMeta) data.obj.getItemStack().getItemMeta();
											Banner b1 = (Banner) bs1.getBlockState();
											List<org.bukkit.block.banner.Pattern> p1 = b1.getPatterns();

											if (p0.containsAll(p1)) {
												playerdata.minestack.setNum(data.index, (giveMineStackNameLore(player, playerdata.minestack.getNum(data.index), new ItemStack(data.obj.getMaterial(), 1, (short)data.obj.getDurability()), data.obj.getGachatype())));
											}
										} else {
											playerdata.minestack.setNum(data.index, (giveMineStackNameLore(player, playerdata.minestack.getNum(data.index), new ItemStack(data.obj.getMaterial(), 1, (short)data.obj.getDurability()), data.obj.getGachatype())));
										}
									}
								}
							}
						}
					}
					player.openInventory(MenuInventoryData.getMineStackMainMenu(player));
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
		int open_flag_type=-1;

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

		//if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack")){
		if (!topinventory.getTitle().contains("メインメニュー") && topinventory.getTitle().contains("MineStack")) {
			/* メインメニュー以外の各種MineStackメニューの際の処理 */
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
				return;
			}

			if(SeichiAssist.DEBUG){
				player.sendMessage("MineStackSize = " + SeichiAssist.minestacklist.size());
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")) {
				/* ArrowLeftを使っているのはメインメニューに戻るボタンのみ。その部分の処理 */
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMainMenu(player));
				return;
			}

			//追加
			if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown")) {
				/* ArrowDownならば、次ページ移行処理 */
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				MineStackMenuTransfer(topinventory, player, itemmeta);
				return;
			}

			//追加
			if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")) {
				/* ArrowUpならば、前ページ移行処理 */
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				MineStackMenuTransfer(topinventory, player, itemmeta);
				return;
			}

			if (itemstackcurrent.getType().equals(Material.IRON_PICKAXE)) {
				// 対象ブロック自動スタック機能トグル(どのメニューでも)
				playerdata.minestackflag = !playerdata.minestackflag;
				if (playerdata.minestackflag) {
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "対象ブロック自動スタック機能:ON");
				} else {
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "対象ブロック自動スタック機能:OFF");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.MineStackToggleMeta(playerdata,itemmeta));
			} else {
				for (int i = 0; i < SeichiAssist.minestacklist.size(); i++) {
					if (itemstackcurrent.getType().equals(SeichiAssist.minestacklist.get(i).getMaterial())
							&& itemstackcurrent.getDurability() == SeichiAssist.minestacklist.get(i).getDurability()) { //MaterialとサブIDが一致

						if (!SeichiAssist.minestacklist.get(i).getNameloreflag()) {
							/* loreが無いとき */

							//同じ名前の別アイテムに対応するためにインベントリの「解放レベル」を見る
							//このアイテムの解放レベル
							int level = SeichiAssist.config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel());
							int level_ = 0;
							//String temp = null;
							for (int j = 0; j < itemstackcurrent.getItemMeta().getLore().size(); j++) {
								/* loreを1行ずつ見ていく。Lv表記を抽出し,level_変数に代入しておく */
								String lore = itemstackcurrent.getItemMeta().getLore().get(j);
						        Pattern p = Pattern.compile(".*Lv[0-9]+以上でスタック可能.*");
						        Matcher m = p.matcher(lore);
						        if (m.matches()) {
						        	String matchstr = lore.replaceAll("^.*Lv","");
						        	level_ = Integer.parseInt(matchstr.replaceAll("[^0-9]+","")); //数字以外を全て消す
						        	break;
						        }
							}

							if (level==level_) {
								/* 開放レベルとクリックしたMineStackボタンのLvが同じとき */
								String itemstack_name = itemstackcurrent.getItemMeta().getDisplayName();
								String minestack_name = SeichiAssist.minestacklist.get(i).getJapaneseName();
								itemstack_name = itemstack_name.replaceAll("§[0-9A-Za-z]","");
								minestack_name = minestack_name.replaceAll("§[0-9A-Za-z]","");
								if (itemstack_name.equals(minestack_name)) { //表記はアイテム名だけなのでアイテム名で判定
									playerdata.minestack.setNum(i, (giveMineStack(player,playerdata.minestack.getNum(i),new ItemStack(SeichiAssist.minestacklist.get(i).getMaterial(), 1, (short)SeichiAssist.minestacklist.get(i).getDurability() ))) );
									open_flag = (Util.getMineStackTypeindex(i) + 1) / 45;
									open_flag_type = SeichiAssist.minestacklist.get(i).getStacktype();
								}
							}
						} else if (SeichiAssist.minestacklist.get(i).getNameloreflag() && itemstackcurrent.getItemMeta().hasDisplayName()) { //名前と説明文がある
							//System.out.println("debug AA");
							//同じ名前の別アイテムに対応するためにインベントリの「解放レベル」を見る
							int level = SeichiAssist.config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel());
							int level_ = 0;
							//String temp = null;
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
										open_flag = (Util.getMineStackTypeindex(i)+1)/45;
										open_flag_type=SeichiAssist.minestacklist.get(i).getStacktype();
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
													open_flag = (Util.getMineStackTypeindex(i)+1)/45;
													open_flag_type=SeichiAssist.minestacklist.get(i).getStacktype();
												}
											} else {
												playerdata.minestack.setNum(i, (giveMineStackNameLore(player,playerdata.minestack.getNum(i),new ItemStack(SeichiAssist.minestacklist.get(i).getMaterial(), 1, (short)SeichiAssist.minestacklist.get(i).getDurability()),SeichiAssist.minestacklist.get(i).getGachatype())));
												open_flag = (Util.getMineStackTypeindex(i)+1)/45;
												open_flag_type=SeichiAssist.minestacklist.get(i).getStacktype();
											}
										}
									}
								}
							}
						}
						if (SeichiAssist.minestacklist.get(i).getGachatype() == -1) {
							playerdata.hisotryData.add(i, SeichiAssist.minestacklist.get(i));
						}
					}

				}
			}

			if (open_flag != -1) {
				player.openInventory(MenuInventoryData.getMineStackMenu(player, open_flag, open_flag_type));
				open_flag = -1;
			}
		}
	}

	/**
	 * MineStackのメニューを移動させます
	 * @param topinventory インベントリ(名前判断用)
	 * @param player プレイヤー
	 * @param itemmeta クリックしたボタンのItemMeta
	 */
	private void MineStackMenuTransfer(Inventory topinventory, Player player, ItemMeta itemmeta) {
		if(itemmeta.getDisplayName().contains("MineStack") &&
				itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
			int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

			//開く音を再生
			player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
			if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "採掘系MineStack")){
				player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display - 1,0));
			} else if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "ドロップ系MineStack")){
				player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display - 1 ,1));
			} else if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "農業系MineStack")){
				player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display - 1,2));
			} else if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "建築系MineStack")){
				player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display - 1,3));
			} else if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "レッドストーン・移動系MineStack")){
				player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display - 1,4));
			} else if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "ガチャ系MineStack")){
				player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display - 1,5));
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
				if(itemmeta.getDisplayName().contains("整地神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList(player, page_display-1));
				}
			}
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("整地神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList(player, page_display-1));
				}
			}
		}
	}

	//ランキングメニュー
	@EventHandler
	public void onPlayerClickSeichiRankingMenuEvent1(InventoryClickEvent event){
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
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ログイン神ランキング")){
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
				if(itemmeta.getDisplayName().contains("ログイン神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_playtick(player, page_display-1));
				}
			}
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("ログイン神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_playtick(player, page_display-1));
				}
			}
		}
	}

	//ランキングメニュー
	@EventHandler
	public void onPlayerClickSeichiRankingMenuEvent2(InventoryClickEvent event){
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
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票神ランキング")){
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
				if(itemmeta.getDisplayName().contains("投票神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_p_vote(player, page_display-1));
				}
			}
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("投票神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_p_vote(player, page_display-1));
				}
			}
		}
	}

	//ランキングメニュー
	@EventHandler
	public void onPlayerClickSeichiRankingMenuEvent3(InventoryClickEvent event){
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
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "寄付神ランキング")){
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
				if(itemmeta.getDisplayName().contains("寄付神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(player, page_display-1));
				}
			}
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("寄付神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(player, page_display-1));
				}
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
//	//minestackの1stack付与の処理
//	private int giveMineStack(Player player,int minestack,Material type){
//		if(minestack >= type.getMaxStackSize()){ //スタックサイズが64でないアイテムにも対応
//			ItemStack itemstack = new ItemStack(type,type.getMaxStackSize());
//			if(!Util.isPlayerInventryFill(player)){
//				Util.addItem(player,itemstack);
//			}else{
//				Util.dropItem(player,itemstack);
//			}
//			minestack -= type.getMaxStackSize();
//			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
//		}else if(minestack == 0){
//			return minestack;
//		}else{
//			ItemStack itemstack = new ItemStack(type,minestack);
//			if(!Util.isPlayerInventryFill(player)){
//				Util.addItem(player,itemstack);
//			}else{
//				Util.dropItem(player,itemstack);
//			}
//			minestack -= minestack;
//			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
//		}
//		return minestack;
//	}

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
    	//ExperienceManager expman = new ExperienceManager(player);


    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績・二つ名システム")){
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

    		//表示内容をLVに変更
			if(itemstackcurrent.getType().equals(Material.REDSTONE_TORCH_ON)){
				playerdata.displayTitle1No = 0 ;
				playerdata.displayTitle2No = 0 ;
				playerdata.displayTitle3No = 0 ;
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
			}

			//予約付与システム受け取り処理
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_Present2")){
				TUTR.TryTitle(player,playerdata.giveachvNo);
				playerdata.giveachvNo = 0 ;
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
			}

			//「二つ名組合せシステム」を開く
			else if(itemstackcurrent.getType().equals(Material.ANVIL)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
			}

			//実績「整地神ランキング」を開く
			else if(itemstackcurrent.getType().equals(Material.DIAMOND_PICKAXE)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage = 1 ;
				player.openInventory(MenuInventoryData.getTitleRankData(player));
			}

			//実績「整地量」を開く
			else if(itemstackcurrent.getType().equals(Material.IRON_PICKAXE)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage = 1 ;
				player.openInventory(MenuInventoryData.getTitleAmountData(player));
			}

			//実績「参加時間」を開く
			else if(itemstackcurrent.getType().equals(Material.COMPASS)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage = 1 ;
				player.openInventory(MenuInventoryData.getTitleTimeData(player));
			}

			//実績「ログイン記録」を開く
			else if(itemstackcurrent.getType().equals(Material.BREAD)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage = 1 ;
				player.openInventory(MenuInventoryData.getTitleJoinData(player));
			}

			//実績「外部支援」を開く
			else if(itemstackcurrent.getType().equals(Material.YELLOW_FLOWER)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage = 1 ;
				player.openInventory(MenuInventoryData.getTitleSupportData(player));
			}

			//実績「公式イベント」を開く
			else if(itemstackcurrent.getType().equals(Material.BLAZE_POWDER )){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage = 1 ;
				player.openInventory(MenuInventoryData.getTitleEventData(player));
			}

			//実績「特殊」を開く
			else if(itemstackcurrent.getType().equals(Material.NETHER_STAR)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage = 1 ;
				player.openInventory(MenuInventoryData.getTitleExtraData(player));
			}

			//実績「極秘任務」を開く
			else if(itemstackcurrent.getType().equals(Material.DIAMOND_BARDING )){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage = 1 ;
				player.openInventory(MenuInventoryData.getTitleSecretData(player));
			}

    		//ホームメニューに戻る
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}
    	}

    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せシステム")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		//実績ポイント最新化
    		if(itemstackcurrent.getType().equals(Material.EMERALD_ORE)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			playerdata.achvPointMAX = 0;
    			for(int i=1000 ; i < 9800; i ++ ){
    				if(playerdata.TitleFlags.get(i)){
    					playerdata.achvPointMAX += 10 ;
    				}
    			}
    			playerdata.achvPoint = (playerdata.achvPointMAX + (playerdata.achvChangenum * 3 )) - playerdata.achvPointUSE ;
    			player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
    		}

    		//エフェクトポイント→実績ポイント変換
    		if(itemstackcurrent.getType().equals(Material.EMERALD)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			//不足してたらスルー
    			if(playerdata.activeskilldata.effectpoint < 10){
    				player.sendMessage("エフェクトポイントが不足しています。");
    			}else {
    				playerdata.achvChangenum ++ ;
    				playerdata.activeskilldata.effectpoint -= 10 ;
    			}
    			//データ最新化
    			playerdata.achvPointMAX = 0;
    			for(int i=1000 ; i < 9800; i ++ ){
    				if(playerdata.TitleFlags.get(i)){
    					playerdata.achvPointMAX += 10 ;
    				}
    			}
    			playerdata.achvPoint = (playerdata.achvPointMAX + (playerdata.achvChangenum * 3 )) - playerdata.achvPointUSE ;

    			player.openInventory(MenuInventoryData.setFreeTitleMainData(player));


    		}

    		//パーツショップ
    		if(itemstackcurrent.getType().equals(Material.ITEM_FRAME)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
    			player.openInventory(MenuInventoryData.setTitleShopData(player));
    		}

    		//前パーツ
    		if(itemstackcurrent.getType().equals(Material.WATER_BUCKET)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
    			player.openInventory(MenuInventoryData.setFreeTitle1Data(player));
    		}

    		//中パーツ
    		if(itemstackcurrent.getType().equals(Material.MILK_BUCKET)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
    			player.openInventory(MenuInventoryData.setFreeTitle2Data(player));
    		}

    		//後パーツ
    		if(itemstackcurrent.getType().equals(Material.LAVA_BUCKET)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
    			player.openInventory(MenuInventoryData.setFreeTitle3Data(player));
    		}

    		//実績メニューに戻る
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}

    	}

    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「前」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}


    		else if (itemstackcurrent.getType().equals(Material.WATER_BUCKET)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

    			String forcheck = SeichiAssist.config.getTitle1(Integer.parseInt(itemmeta.getDisplayName()))
    								+ SeichiAssist.config.getTitle2(playerdata.displayTitle2No)
    								+ SeichiAssist.config.getTitle3(playerdata.displayTitle3No);
    			if(forcheck.length() < 8){
    				playerdata.displayTitle1No = Integer.parseInt(itemmeta.getDisplayName());
    				player.sendMessage("前パーツ「"+ SeichiAssist.config.getTitle1(playerdata.displayTitle1No) +"」をセットしました。");
    			}else {
    				player.sendMessage("全パーツ合計で７文字以内になるよう設定してください。");
    			}
    		}

    		//パーツ未選択に
    		else if(itemstackcurrent.getType().equals(Material.GRASS)){
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.displayTitle1No = 0 ;
				player.sendMessage("前パーツの選択を解除しました。");
				return;
			}

    		//組み合わせメイン
			else if(itemstackcurrent.getType().equals(Material.BARRIER)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
				return;
			}

    		//次ページ
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitle1Data(player));
				return;
			}


    	}

    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「中」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}


    		else if (itemstackcurrent.getType().equals(Material.MILK_BUCKET)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

    			String forcheck = SeichiAssist.config.getTitle1(playerdata.displayTitle1No)
    								+ SeichiAssist.config.getTitle2(Integer.parseInt(itemmeta.getDisplayName()))
    								+ SeichiAssist.config.getTitle3(playerdata.displayTitle3No);
    			if(forcheck.length() < 8){
    				playerdata.displayTitle2No = Integer.parseInt(itemmeta.getDisplayName());
    				player.sendMessage("中パーツ「"+ SeichiAssist.config.getTitle2(playerdata.displayTitle2No) +"」をセットしました。");
    			}else {
    				player.sendMessage("全パーツ合計で７文字以内になるよう設定してください。");
    			}
    		}

    		//パーツ未選択に
    		else if(itemstackcurrent.getType().equals(Material.GRASS)){
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.displayTitle2No = 0 ;
				player.sendMessage("中パーツの選択を解除しました。");
				return;
			}

    		//組み合わせメインへ移動
			else if(itemstackcurrent.getType().equals(Material.BARRIER)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
				return;
			}

    		//次ページ
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitle2Data(player));
				return;
			}


    	}

       	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「後」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}


    		else if (itemstackcurrent.getType().equals(Material.LAVA_BUCKET)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

    			String forcheck = SeichiAssist.config.getTitle1(playerdata.displayTitle1No)
    								+ SeichiAssist.config.getTitle2(playerdata.displayTitle2No)
    								+ SeichiAssist.config.getTitle3(Integer.parseInt(itemmeta.getDisplayName()));
    			if(forcheck.length() < 8){
    				playerdata.displayTitle3No = Integer.parseInt(itemmeta.getDisplayName());
    				player.sendMessage("後パーツ「"+ SeichiAssist.config.getTitle3(playerdata.displayTitle3No) +"」をセットしました。");
    			}else {
    				player.sendMessage("全パーツ合計で７文字以内になるよう設定してください。");
    			}
    		}

    		//パーツ未選択に
    		else if(itemstackcurrent.getType().equals(Material.GRASS)){
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.displayTitle3No = 0 ;
				player.sendMessage("後パーツの選択を解除しました。");
				return;
			}

    		//組み合わせメイン
			else if(itemstackcurrent.getType().equals(Material.BARRIER)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
				return;
			}

    		//次ページ
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitle3Data(player));
				return;
			}


    	}

    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績ポイントショップ")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		//実績ポイント最新化
    		if(itemstackcurrent.getType().equals(Material.EMERALD_ORE)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			playerdata.achvPointMAX = 0;
    			for(int i=1000 ; i < 9800; i ++ ){
    				if(playerdata.TitleFlags.get(i)){
    					playerdata.achvPointMAX += 10 ;
    				}
    			}
    			playerdata.achvPoint = (playerdata.achvPointMAX + (playerdata.achvChangenum * 3 )) - playerdata.achvPointUSE ;
    			playerdata.samepageflag = true;
    			player.openInventory(MenuInventoryData.setTitleShopData(player));
    		}

    		//購入処理
    		if(itemstackcurrent.getType().equals(Material.BEDROCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

    			if(Integer.parseInt(itemmeta.getDisplayName()) < 9900 ){
	    			if(playerdata.achvPoint < 20){
	    				player.sendMessage("実績ポイントが不足しています。");
	    			}else {
	    				playerdata.TitleFlags.set(Integer.parseInt(itemmeta.getDisplayName()));
	    				playerdata.achvPoint -= 20 ;
	    				playerdata.achvPointUSE += 20 ;
	    				player.sendMessage("パーツ「"+ SeichiAssist.config.getTitle1(Integer.parseInt(itemmeta.getDisplayName())) + "」を購入しました。");
	    				playerdata.samepageflag = true;
	    				player.openInventory(MenuInventoryData.setTitleShopData(player));
	    			}
    			}else {
        			if(playerdata.achvPoint < 35){
        				player.sendMessage("実績ポイントが不足しています。");
        			}else {
        				playerdata.TitleFlags.set(Integer.parseInt(itemmeta.getDisplayName()));
        				playerdata.achvPoint -= 35 ;
        				playerdata.achvPointUSE += 35 ;
        				player.sendMessage("パーツ「"+ SeichiAssist.config.getTitle2(Integer.parseInt(itemmeta.getDisplayName())) + "」を購入しました。");
        				playerdata.samepageflag = true;
        				player.openInventory(MenuInventoryData.setTitleShopData(player));
        			}
    			}


    		}


    		//組み合わせメイン
			else if(itemstackcurrent.getType().equals(Material.BARRIER)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
				return;
			}

    		//次ページ
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setTitleShopData(player));
				return;
			}

    	}


    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地神ランキング」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		if(itemstackcurrent.getType().equals(Material.BEDROCK)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
    			player.openInventory(MenuInventoryData.getTitleRankData(player));
    		}
    		else if (itemstackcurrent.getType().equals(Material.DIAMOND_BLOCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			if(itemmeta.getDisplayName().contains("No1001「"+ SeichiAssist.config.getTitle1(1001) +"」")){
    				playerdata.displayTitle1No = 1001 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(1001) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No1002「"+ SeichiAssist.config.getTitle1(1002) +"」")){
    				playerdata.displayTitle1No = 1002 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(1002) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No1003「"+ SeichiAssist.config.getTitle1(1003) +"」")){
    				playerdata.displayTitle1No = 1003 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(1003) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No1004「"+ SeichiAssist.config.getTitle1(1004) +"」")){
    				playerdata.displayTitle1No = 1004 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(1004) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No1005「"+ SeichiAssist.config.getTitle1(1005)
    					+ SeichiAssist.config.getTitle3(1005) + "」")){
    				playerdata.displayTitle1No = 1005 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 1005 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(1005)
    						+ SeichiAssist.config.getTitle3(1005) + "」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No1006「"+ SeichiAssist.config.getTitle1(1006) +"」")){
    				playerdata.displayTitle1No = 1006 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(1006) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No1007「"+ SeichiAssist.config.getTitle1(1007)
    					+ SeichiAssist.config.getTitle2(9904) + SeichiAssist.config.getTitle3(1007) +"」")){
    				playerdata.displayTitle1No = 1007 ;
    				playerdata.displayTitle2No = 9904 ;
    				playerdata.displayTitle3No = 1007 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(1007)
        					+ SeichiAssist.config.getTitle2(9904) + SeichiAssist.config.getTitle3(1007) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No1008「"+ SeichiAssist.config.getTitle1(1008)
    					+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(1008) +"」")){
    				playerdata.displayTitle1No = 1008 ;
    				playerdata.displayTitle2No = 9901 ;
    				playerdata.displayTitle3No = 1008 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(1008)
        					+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(1008) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No1009「"+ SeichiAssist.config.getTitle1(1009)
    					+ SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(1009) +"」")){
    				playerdata.displayTitle1No = 1009 ;
    				playerdata.displayTitle2No = 9909 ;
    				playerdata.displayTitle3No = 1009 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(1009)
        					+ SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(1009) +"」が設定されました。");
    			}
    			player.openInventory(MenuInventoryData.getTitleRankData(player));

    		}
    		//実績メニューに戻る
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}
    	}

    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地量」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		if(itemstackcurrent.getType().equals(Material.BEDROCK)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
    			player.openInventory(MenuInventoryData.getTitleAmountData(player));
    		}
    		else if (itemstackcurrent.getType().equals(Material.DIAMOND_BLOCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			if(itemmeta.getDisplayName().contains("No3001「"+ SeichiAssist.config.getTitle1(3001) +"」")){
    				playerdata.displayTitle1No = 3001 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3001) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3002「"+ SeichiAssist.config.getTitle1(3002)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(3002) +"」")){
    				playerdata.displayTitle1No = 3002 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 3002 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3002)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(3002) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3003「"+ SeichiAssist.config.getTitle1(3003) +"」")){
    				playerdata.displayTitle1No = 3003 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3003) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3004「"+ SeichiAssist.config.getTitle1(3004)
    					+ SeichiAssist.config.getTitle2(9902) + "」")){
    				playerdata.displayTitle1No = 3004 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3004) +
        					SeichiAssist.config.getTitle2(9902) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3005「"+ SeichiAssist.config.getTitle1(3005)
    					+ SeichiAssist.config.getTitle3(3005) + "」")){
    				playerdata.displayTitle1No = 3005 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 3005 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3005)
        					+ SeichiAssist.config.getTitle3(3005) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3006「"+ SeichiAssist.config.getTitle1(3006) +"」")){
    				playerdata.displayTitle1No = 3006 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3006) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3007「"+ SeichiAssist.config.getTitle1(3007)
    					+ SeichiAssist.config.getTitle2(9905) + "」")){
    				playerdata.displayTitle1No = 3007 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3007) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3008「"+ SeichiAssist.config.getTitle1(3008) +"」")){
    				playerdata.displayTitle1No = 3008 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3008) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3009「"+ SeichiAssist.config.getTitle1(3009)
    					+ SeichiAssist.config.getTitle3(3009) + "」")){
    				playerdata.displayTitle1No = 3009 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 3009 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3009)
    						+  SeichiAssist.config.getTitle3(3009) + "」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3010「"+ SeichiAssist.config.getTitle1(3010)
    					+ SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(3010) + "」")){
    				playerdata.displayTitle1No = 3010 ;
    				playerdata.displayTitle2No = 9909 ;
    				playerdata.displayTitle3No = 3010 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3010)
    						+ SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(3010) + "」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No3011「"+ SeichiAssist.config.getTitle1(3011) +"」")){
    				playerdata.displayTitle1No = 3011 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(3011) +"」が設定されました。");
    			}
    			player.openInventory(MenuInventoryData.getTitleAmountData(player));

    		}
    		//実績メニューに戻る
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}
    	}

    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「参加時間」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		if(itemstackcurrent.getType().equals(Material.BEDROCK)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
    			player.openInventory(MenuInventoryData.getTitleTimeData(player));
    		}
    		else if (itemstackcurrent.getType().equals(Material.DIAMOND_BLOCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			if(itemmeta.getDisplayName().contains("No4001「"+ SeichiAssist.config.getTitle1(4001)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4001) +"」")){
    				playerdata.displayTitle1No = 4001 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 4001 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4001)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4001) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No4002「"+ SeichiAssist.config.getTitle1(4002)
    					+ SeichiAssist.config.getTitle3(4002) +"」")){
    				playerdata.displayTitle1No = 4002 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 4002 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4002)
        					+ SeichiAssist.config.getTitle3(4002) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No4003「"+ SeichiAssist.config.getTitle1(4003)
    					+ SeichiAssist.config.getTitle3(4003) +"」")){
    				playerdata.displayTitle1No = 4003 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 4003 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4003)
        					+ SeichiAssist.config.getTitle3(4003) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No4004「"+ SeichiAssist.config.getTitle1(4004)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4004) +"」")){
    				playerdata.displayTitle1No = 4004 ;
    				playerdata.displayTitle2No = 9005 ;
    				playerdata.displayTitle3No = 4004 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4004)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4004) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No4005「"+ SeichiAssist.config.getTitle1(4005)
    					+ SeichiAssist.config.getTitle3(4005) +"」")){
    				playerdata.displayTitle1No = 4005 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 4005 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4005)
        					+ SeichiAssist.config.getTitle3(4005) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No4006「"+ SeichiAssist.config.getTitle1(4006)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4006) +"」")){
    				playerdata.displayTitle1No = 4006 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 4006 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4006)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4006) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No4007「"+ SeichiAssist.config.getTitle1(4007)
    					+ SeichiAssist.config.getTitle3(4007) +"」")){
    				playerdata.displayTitle1No = 4007 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 4007 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4007)
        					+ SeichiAssist.config.getTitle3(4007) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No4008「"+ SeichiAssist.config.getTitle1(4008)
    					+ SeichiAssist.config.getTitle3(4008) +"」")){
    				playerdata.displayTitle1No = 4008 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 4008 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4008)
        					+ SeichiAssist.config.getTitle3(4008) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No4009「"+ SeichiAssist.config.getTitle1(4009)
    					+ SeichiAssist.config.getTitle3(4009) +"」")){
    				playerdata.displayTitle1No = 4009 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 4009 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4009)
        					+ SeichiAssist.config.getTitle3(4009) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No4010「"+ SeichiAssist.config.getTitle1(4010)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4010) +"」")){
    				playerdata.displayTitle1No = 4010 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 4010 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(4010)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4010) +"」が設定されました。");
    			}
    			player.openInventory(MenuInventoryData.getTitleTimeData(player));
    		}
    		else if(itemstackcurrent.getType().equals(Material.EMERALD_BLOCK)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.TitleFlags.set(8003);
				player.sendMessage("お疲れ様でした！今日のお給料の代わりに二つ名をどうぞ！");
				player.openInventory(MenuInventoryData.getTitleTimeData(player));
    		}
    		//実績メニューに戻る
    		else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}
    	}


    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「ログイン記録」")){
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
    			player.openInventory(MenuInventoryData.getTitleJoinData(player));
    		}
    		else if (itemstackcurrent.getType().equals(Material.DIAMOND_BLOCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			if(itemmeta.getDisplayName().contains("No5001「"+ SeichiAssist.config.getTitle1(5001)
    					+ SeichiAssist.config.getTitle2(5001) + "」")){
    				playerdata.displayTitle1No = 5001 ;
    				playerdata.displayTitle2No = 5001 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5001)
        					+ SeichiAssist.config.getTitle2(5001) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5002「"+ SeichiAssist.config.getTitle1(5002)
    					+ SeichiAssist.config.getTitle3(5002) +"」")){
    				playerdata.displayTitle1No = 5002 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 5002 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5002)
        					+ SeichiAssist.config.getTitle3(5002) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5003「"+ SeichiAssist.config.getTitle1(5003)+"」")){
    				playerdata.displayTitle1No = 5003 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5003)+"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5004「"+ SeichiAssist.config.getTitle1(5004)
    					+ SeichiAssist.config.getTitle3(5004) +"」")){
    				playerdata.displayTitle1No = 5004 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 5004 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5004)
        					+ SeichiAssist.config.getTitle3(5004) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5005「"+ SeichiAssist.config.getTitle1(5005)
    					+ SeichiAssist.config.getTitle3(5005) +"」")){
    				playerdata.displayTitle1No = 5005 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 5005 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5005)
        					+ SeichiAssist.config.getTitle3(5005) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5006「"+ SeichiAssist.config.getTitle1(5006)
    					+ SeichiAssist.config.getTitle3(5006) +"」")){
    				playerdata.displayTitle1No = 5006 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 5006 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5006)
        					+ SeichiAssist.config.getTitle3(5006) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5007「"+ SeichiAssist.config.getTitle1(5007)+"」")){
    				playerdata.displayTitle1No = 5007 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5007)+"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5008「"+ SeichiAssist.config.getTitle1(5008)
    					+  SeichiAssist.config.getTitle2(9905) + "」")){
    				playerdata.displayTitle1No = 5008 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5008)
        					+  SeichiAssist.config.getTitle2(9905)+"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5101「"+ SeichiAssist.config.getTitle1(5101)
    					+  SeichiAssist.config.getTitle3(5101) + "」")){
    				playerdata.displayTitle1No = 5101 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 5101 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5101)
        					+  SeichiAssist.config.getTitle3(5101)+"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5102「"+ SeichiAssist.config.getTitle1(5102)
    					+ SeichiAssist.config.getTitle2(9907) + SeichiAssist.config.getTitle3(5102) + "」")){
    				playerdata.displayTitle1No = 5102 ;
    				playerdata.displayTitle2No = 9907 ;
    				playerdata.displayTitle3No = 5102 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5102)
        					+ SeichiAssist.config.getTitle2(9907) + SeichiAssist.config.getTitle3(5102)+"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5103「"+ SeichiAssist.config.getTitle1(5103)
    					+ SeichiAssist.config.getTitle2(9905) + "」")){
    				playerdata.displayTitle1No = 5103 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5103)
        					+ SeichiAssist.config.getTitle2(9905) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5104「"+ SeichiAssist.config.getTitle1(5104)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(5104) +"」")){
    				playerdata.displayTitle1No = 5104 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 5104 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5104)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(5104) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5105「"+ SeichiAssist.config.getTitle1(5105)
    					+ SeichiAssist.config.getTitle2(9907) + SeichiAssist.config.getTitle3(5105) +"」")){
    				playerdata.displayTitle1No = 5105 ;
    				playerdata.displayTitle2No = 9907 ;
    				playerdata.displayTitle3No = 5105 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5105)
        					+ SeichiAssist.config.getTitle2(9907) + SeichiAssist.config.getTitle3(5105) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5106「"+ SeichiAssist.config.getTitle1(5106)+"」")){
    				playerdata.displayTitle1No = 5106 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5106)+"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5107「"+ SeichiAssist.config.getTitle1(5107)
    					+ SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(5107) +"」")){
    				playerdata.displayTitle1No = 5107 ;
    				playerdata.displayTitle2No = 9909 ;
    				playerdata.displayTitle3No = 5107 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5107)
        					+ SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(5107) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5108「"+ SeichiAssist.config.getTitle1(5108)
    					+ SeichiAssist.config.getTitle3(5108) +"」")){
    				playerdata.displayTitle1No = 5108 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 5108 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5108)
        					+ SeichiAssist.config.getTitle3(5108) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5109「"+ SeichiAssist.config.getTitle1(5109)+"」")){
    				playerdata.displayTitle1No = 5109 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5109)+"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5110「"+ SeichiAssist.config.getTitle1(5110)+"」")){
    				playerdata.displayTitle1No = 5110 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5110)+"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No5111「"+ SeichiAssist.config.getTitle1(5111)+"」")){
    				playerdata.displayTitle1No = 5111 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(5111)+"」が設定されました。");
    			}


    			player.openInventory(MenuInventoryData.getTitleJoinData(player));

    		}
    		//実績メニューに戻る
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}
    	}



    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「外部支援」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		if(itemstackcurrent.getType().equals(Material.BEDROCK)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
    			player.openInventory(MenuInventoryData.getTitleSupportData(player));
    		}
    		else if (itemstackcurrent.getType().equals(Material.DIAMOND_BLOCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			if(itemmeta.getDisplayName().contains("No6001「"+ SeichiAssist.config.getTitle1(6001) +"」")){
    				playerdata.displayTitle1No = 6001 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(6001) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No6002「"+ SeichiAssist.config.getTitle1(6002)
    					+ SeichiAssist.config.getTitle3(6002) +"」")){
    				playerdata.displayTitle1No = 6002 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 6002 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(6002)
        					+ SeichiAssist.config.getTitle3(6002) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No6003「"+ SeichiAssist.config.getTitle1(6003) +"」")){
    				playerdata.displayTitle1No = 6003 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(6003) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No6004「"+ SeichiAssist.config.getTitle1(6004)
    					+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(6004) +"」")){
    				playerdata.displayTitle1No = 6004 ;
    				playerdata.displayTitle2No = 9903 ;
    				playerdata.displayTitle3No = 6004 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(6004)
        					+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(6004) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No6005「"+ SeichiAssist.config.getTitle1(6005)
    					+ SeichiAssist.config.getTitle2(9905) + "」")){
    				playerdata.displayTitle1No = 6005 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(6005)
        					+ SeichiAssist.config.getTitle2(9905) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No6006「"+ SeichiAssist.config.getTitle1(6006)
    					+ SeichiAssist.config.getTitle3(6006) +"」")){
    				playerdata.displayTitle1No = 6006 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 6006 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(6006)
        					+ SeichiAssist.config.getTitle3(6006) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No6007「"+ SeichiAssist.config.getTitle1(6007)
    					+ SeichiAssist.config.getTitle2(9902) +"」")){
    				playerdata.displayTitle1No = 6007 ;
    				playerdata.displayTitle2No = 9902 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(6007)
        					+ SeichiAssist.config.getTitle2(9902) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No6008「"+ SeichiAssist.config.getTitle1(6008) +"」")){
    				playerdata.displayTitle1No = 6008 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(6008) +"」が設定されました。");
    			}
    			player.openInventory(MenuInventoryData.getTitleSupportData(player));
    		}
    		//実績メニューに戻る
    		else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}
    	}
    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「公式イベント」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		if(itemstackcurrent.getType().equals(Material.BEDROCK)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			player.sendMessage("この実績は配布解禁式です。運営チームからの配布タイミングを逃さないようご注意ください。");
    			player.openInventory(MenuInventoryData.getTitleEventData(player));
    		}
    		else if (itemstackcurrent.getType().equals(Material.DIAMOND_BLOCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			if(itemmeta.getDisplayName().contains("No7001「"+ SeichiAssist.config.getTitle1(7001)
    					+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(7001) +"」")){
    				playerdata.displayTitle1No = 7001 ;
    				playerdata.displayTitle2No = 9901 ;
    				playerdata.displayTitle3No = 7001 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7001)
        					+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(7001) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7002「"+ SeichiAssist.config.getTitle1(7002)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7002) +"」")){
    				playerdata.displayTitle1No = 7002 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7002 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7002)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7002) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7003「"+ SeichiAssist.config.getTitle1(7003)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7003) +"」")){
    				playerdata.displayTitle1No = 7003 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7003 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7003)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7003) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7004「"+ SeichiAssist.config.getTitle2(7004) +"」")){
    				playerdata.displayTitle1No = 0 ;
    				playerdata.displayTitle2No = 7004 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle2(7004) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7005「"+ SeichiAssist.config.getTitle1(7005)
    					+ SeichiAssist.config.getTitle2(9902) + SeichiAssist.config.getTitle3(7005) +"」")){
    				playerdata.displayTitle1No = 7005 ;
    				playerdata.displayTitle2No = 9902 ;
    				playerdata.displayTitle3No = 7005 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7005)
        					+ SeichiAssist.config.getTitle2(9902) + SeichiAssist.config.getTitle3(7005) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7006「"+ SeichiAssist.config.getTitle1(7006)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7006) +"」")){
    				playerdata.displayTitle1No = 7006 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7006 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7006)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7006) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7007「"+ SeichiAssist.config.getTitle1(7007)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7007) +"」")){
    				playerdata.displayTitle1No = 7007 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7007 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7007)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7007) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7008「"+ SeichiAssist.config.getTitle1(7008)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7008) +"」")){
    				playerdata.displayTitle1No = 7008 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7008 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7008)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7008) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7009「"+ SeichiAssist.config.getTitle1(7009)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7009) +"」")){
    				playerdata.displayTitle1No = 7009 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7009 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7009)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7009) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7010「"+ SeichiAssist.config.getTitle1(7010)
    					+ SeichiAssist.config.getTitle3(7010) +"」")){
    				playerdata.displayTitle1No = 7010 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7010 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7010)
        					+ SeichiAssist.config.getTitle3(7010) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7011「"+ SeichiAssist.config.getTitle1(7011)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7011) +"」")){
    				playerdata.displayTitle1No = 7011 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7011 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7011)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7011) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7012「"+ SeichiAssist.config.getTitle1(7012)
    					+ SeichiAssist.config.getTitle3(7012) +"」")){
    				playerdata.displayTitle1No = 7012 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7012 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7012)
        					+ SeichiAssist.config.getTitle3(7012) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7013「"+ SeichiAssist.config.getTitle1(7013) + "」")){
    				playerdata.displayTitle1No = 7013 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7013) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7014「"+ SeichiAssist.config.getTitle1(7014) + "」")){
    				playerdata.displayTitle1No = 7014 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7014) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7015「"+ SeichiAssist.config.getTitle1(7015)
    					+ SeichiAssist.config.getTitle3(9904) + SeichiAssist.config.getTitle3(7015) +"」")){
    				playerdata.displayTitle1No = 7015 ;
    				playerdata.displayTitle2No = 9904 ;
    				playerdata.displayTitle3No = 7015 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7015)
    						+ SeichiAssist.config.getTitle3(9904) + SeichiAssist.config.getTitle3(7015) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7016「"+ SeichiAssist.config.getTitle1(7016)
    					+ SeichiAssist.config.getTitle3(7016) +"」")){
    				playerdata.displayTitle1No = 7016 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7016 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7016)
        					+ SeichiAssist.config.getTitle3(7016) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7017「"+ SeichiAssist.config.getTitle1(7017)
    					+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7017) +"」")){
    				playerdata.displayTitle1No = 7017 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7017 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7017)
    						+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7017) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7018「"+ SeichiAssist.config.getTitle1(7018)
    					+ SeichiAssist.config.getTitle3(9904) + SeichiAssist.config.getTitle3(7018) +"」")){
    				playerdata.displayTitle1No = 7018 ;
    				playerdata.displayTitle2No = 9904 ;
    				playerdata.displayTitle3No = 7018 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7018)
    						+ SeichiAssist.config.getTitle3(9904) + SeichiAssist.config.getTitle3(7018) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7019「"+ SeichiAssist.config.getTitle1(7019)
    					+ SeichiAssist.config.getTitle3(7019) + "」")){
    				playerdata.displayTitle1No = 7019 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7019 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7019)
    						+ SeichiAssist.config.getTitle3(7019) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7020「"+ SeichiAssist.config.getTitle1(7020)
    					+ SeichiAssist.config.getTitle3(7020) +"」")){
    				playerdata.displayTitle1No = 7020 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7020 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7020)
        					+ SeichiAssist.config.getTitle3(7020) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7021「"+ SeichiAssist.config.getTitle1(7021)
    					+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7021) +"」")){
    				playerdata.displayTitle1No = 7021 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7021 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7021)
    						+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7021) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7022「"+ SeichiAssist.config.getTitle1(7022)
    					+ SeichiAssist.config.getTitle3(7022) +"」")){
    				playerdata.displayTitle1No = 7022 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7022 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7022)
        					+ SeichiAssist.config.getTitle3(7022) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7023「"+ SeichiAssist.config.getTitle1(7023)
    					+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7023) +"」")){
    				playerdata.displayTitle1No = 7023 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7023 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7023)
    						+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7023) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7024「"+ SeichiAssist.config.getTitle1(7024)
    					+ SeichiAssist.config.getTitle3(7024) +"」")){
    				playerdata.displayTitle1No = 7024 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7024 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7024)
        					+ SeichiAssist.config.getTitle3(7024) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7025「"+ SeichiAssist.config.getTitle1(7025)
    					+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7025) +"」")){
    				playerdata.displayTitle1No = 7025 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7025 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7025)
    						+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7025) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7026「"+ SeichiAssist.config.getTitle1(7026)
    					+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7026) +"」")){
    				playerdata.displayTitle1No = 7026 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7026 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7026)
    						+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7026) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7027「"+ SeichiAssist.config.getTitle1(7027)
    					+ SeichiAssist.config.getTitle3(7027) +"」")){
    				playerdata.displayTitle1No = 7027 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7027 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7027)
    						+ SeichiAssist.config.getTitle3(7027) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7901「"+ SeichiAssist.config.getTitle1(7901)
    					+ SeichiAssist.config.getTitle2(7901) + SeichiAssist.config.getTitle3(7901) +"」")){
    				playerdata.displayTitle1No = 7901 ;
    				playerdata.displayTitle2No = 7901 ;
    				playerdata.displayTitle3No = 7901 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7901)
    						+ SeichiAssist.config.getTitle2(7901) + SeichiAssist.config.getTitle3(7901) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7902「"+ SeichiAssist.config.getTitle1(7902)
    					+ SeichiAssist.config.getTitle3(7902) +"」")){
    				playerdata.displayTitle1No = 7902 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7902 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7902)
    						+ SeichiAssist.config.getTitle3(7902) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7903「"+ SeichiAssist.config.getTitle1(7903)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7903) +"」")){
    				playerdata.displayTitle1No = 7903 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 7903 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7903)
    						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7903) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7904「"+ SeichiAssist.config.getTitle1(7904)
    					+ SeichiAssist.config.getTitle2(9907) + SeichiAssist.config.getTitle3(7904) +"」")){
    				playerdata.displayTitle1No = 7904 ;
    				playerdata.displayTitle2No = 9907 ;
    				playerdata.displayTitle3No = 7904 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7904)
    						+ SeichiAssist.config.getTitle2(9907) + SeichiAssist.config.getTitle3(7904) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7905「"+ SeichiAssist.config.getTitle1(7905)
    					+ SeichiAssist.config.getTitle3(7905) +"」")){
    				playerdata.displayTitle1No = 7905 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7905 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7905)
    						+ SeichiAssist.config.getTitle3(7905) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No7906「"+ SeichiAssist.config.getTitle1(7906)
    					+ SeichiAssist.config.getTitle3(7906) +"」")){
    				playerdata.displayTitle1No = 7906 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 7906 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(7906)
    						+ SeichiAssist.config.getTitle3(7906) +"」が設定されました。");
    			}
    			player.openInventory(MenuInventoryData.getTitleEventData(player));

    		}
    		//実績メニューに戻る
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}
    	}


    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「特殊」")){
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
    			if(itemmeta.getDisplayName().contains("No9001「???」")){
    				TUTR.TryTitle(player,9001);
    			}else if(itemmeta.getDisplayName().contains("No9002「???」")){
    				TUTR.TryTitle(player,9002);
    			}else if(itemmeta.getDisplayName().contains("No9003「???」")){
    				TUTR.TryTitle(player,9003);
    			}else if(itemmeta.getDisplayName().contains("No9004「???」")){
    				TUTR.TryTitle(player,9004);
    			}else if(itemmeta.getDisplayName().contains("No9005「???」")){
    				TUTR.TryTitle(player,9005);
    			}else if(itemmeta.getDisplayName().contains("No9006「???」")){
    				TUTR.TryTitle(player,9006);
    			}else if(itemmeta.getDisplayName().contains("No9007「???」")){
    				TUTR.TryTitle(player,9007);
    			}else if(itemmeta.getDisplayName().contains("No9008「???」")){
    				TUTR.TryTitle(player,9008);
    			}else if(itemmeta.getDisplayName().contains("No9009「???」")){
    				TUTR.TryTitle(player,9009);
    			}else if(itemmeta.getDisplayName().contains("No9010「???」")){
    				TUTR.TryTitle(player,9010);
    			}else if(itemmeta.getDisplayName().contains("No9011「???」")){
    				TUTR.TryTitle(player,9011);
    			}else if(itemmeta.getDisplayName().contains("No9012「???」")){
    				TUTR.TryTitle(player,9012);
    			}else if(itemmeta.getDisplayName().contains("No9013「???」")){
    				TUTR.TryTitle(player,9013);
    			}else if(itemmeta.getDisplayName().contains("No9014「???」")){
    				TUTR.TryTitle(player,9014);
    			}else if(itemmeta.getDisplayName().contains("No9015「???」")){
    				TUTR.TryTitle(player,9015);
    			}else if(itemmeta.getDisplayName().contains("No9016「???」")){
    				TUTR.TryTitle(player,9016);
    			}else if(itemmeta.getDisplayName().contains("No9017「???」")){
    				TUTR.TryTitle(player,9017);
    			}else if(itemmeta.getDisplayName().contains("No9018「???」")){
    				TUTR.TryTitle(player,9018);
    			}else if(itemmeta.getDisplayName().contains("No9019「???」")){
    				TUTR.TryTitle(player,9019);
    			}else if(itemmeta.getDisplayName().contains("No9020「???」")){
    				TUTR.TryTitle(player,9020);
    			}else if(itemmeta.getDisplayName().contains("No9021「???」")){
    				TUTR.TryTitle(player,9021);
    			}else if(itemmeta.getDisplayName().contains("No9022「???」")){
    				TUTR.TryTitle(player,9022);
    			}else if(itemmeta.getDisplayName().contains("No9023「???」")){
    				TUTR.TryTitle(player,9023);
    			}else if(itemmeta.getDisplayName().contains("No9024「???」")){
    				TUTR.TryTitle(player,9024);
    			}else if(itemmeta.getDisplayName().contains("No9025「???」")){
    				TUTR.TryTitle(player,9025);
    			}else if(itemmeta.getDisplayName().contains("No9026「???」")){
    				TUTR.TryTitle(player,9026);
    			}else if(itemmeta.getDisplayName().contains("No9027「???」")){
    				TUTR.TryTitle(player,9027);
    			}else if(itemmeta.getDisplayName().contains("No9028「???」")){
    				TUTR.TryTitle(player,9028);
    			}else if(itemmeta.getDisplayName().contains("No9029「???」")){
    				TUTR.TryTitle(player,9029);
    			}else if(itemmeta.getDisplayName().contains("No9030「???」")){
    				TUTR.TryTitle(player,9030);
    			}else if(itemmeta.getDisplayName().contains("No9031「???」")){
    				TUTR.TryTitle(player,9031);
    			}else if(itemmeta.getDisplayName().contains("No9032「???」")){
    				TUTR.TryTitle(player,9032);
    			}else if(itemmeta.getDisplayName().contains("No9033「???」")){
    				TUTR.TryTitle(player,9033);
    			}else if(itemmeta.getDisplayName().contains("No9034「???」")){
    				TUTR.TryTitle(player,9034);
    			}else if(itemmeta.getDisplayName().contains("No9035「???」")){
    				TUTR.TryTitle(player,9035);
    			}else if(itemmeta.getDisplayName().contains("No9036「???」")){
    				TUTR.TryTitle(player,9036);
    			}

    			player.openInventory(MenuInventoryData.getTitleExtraData(player));
    		}
    		else if (itemstackcurrent.getType().equals(Material.DIAMOND_BLOCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			if(itemmeta.getDisplayName().contains("No9001「"+ SeichiAssist.config.getTitle1(9001) +"」")){
    				playerdata.displayTitle1No = 9001 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9001) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9002「"+ SeichiAssist.config.getTitle1(9002)
    					+ SeichiAssist.config.getTitle3(9002) +"」")){
    				playerdata.displayTitle1No = 9002 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9002 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9002)
        					+ SeichiAssist.config.getTitle3(9002) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9003「"+ SeichiAssist.config.getTitle1(9003) +"」")){
    				playerdata.displayTitle1No = 9003 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9003) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9004「"+ SeichiAssist.config.getTitle1(9004)
    					+ SeichiAssist.config.getTitle2(9004) + SeichiAssist.config.getTitle3(9004) +"」")){
    				playerdata.displayTitle1No = 9004 ;
    				playerdata.displayTitle2No = 9004 ;
    				playerdata.displayTitle3No = 9004 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9004)
    						+ SeichiAssist.config.getTitle2(9004) + SeichiAssist.config.getTitle3(9004) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9005「"+ SeichiAssist.config.getTitle1(9005)
    					+ SeichiAssist.config.getTitle3(9005) +"」")){
    				playerdata.displayTitle1No = 9005 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9005 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9005)
        					+ SeichiAssist.config.getTitle3(9005) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9006「"+ SeichiAssist.config.getTitle1(9006) +"」")){
    				playerdata.displayTitle1No = 9006 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9006) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9007「"+ SeichiAssist.config.getTitle1(9007) +"」")){
    				playerdata.displayTitle1No = 9007 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9007) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9008「"+ SeichiAssist.config.getTitle1(9008)
    					+ SeichiAssist.config.getTitle3(9008) +"」")){
    				playerdata.displayTitle1No = 9008 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9008 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9008)
        					+ SeichiAssist.config.getTitle3(9008) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9009「"+ SeichiAssist.config.getTitle1(9009) +"」")){
    				playerdata.displayTitle1No = 9009 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9009) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9010「"+ SeichiAssist.config.getTitle1(9010)
    					+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(9010) +"」")){
    				playerdata.displayTitle1No = 9010 ;
    				playerdata.displayTitle2No = 9903 ;
    				playerdata.displayTitle3No = 9010 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9010)
        					+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(9010) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9011「"+ SeichiAssist.config.getTitle1(9011)
    					+ SeichiAssist.config.getTitle3(9011) +"」")){
    				playerdata.displayTitle1No = 9011 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9011 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9011)
        					+ SeichiAssist.config.getTitle3(9011) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9012「"+ SeichiAssist.config.getTitle1(9012)
    					+ SeichiAssist.config.getTitle3(9012) +"」")){
    				playerdata.displayTitle1No = 9012 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9012 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9012)
        					+ SeichiAssist.config.getTitle3(9012) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9013「"+ SeichiAssist.config.getTitle1(9013) +"」")){
    				playerdata.displayTitle1No = 9013 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9013) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9014「"+ SeichiAssist.config.getTitle2(9014) +"」")){
    				playerdata.displayTitle1No = 0 ;
    				playerdata.displayTitle2No = 9014 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle2(9014) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9015「"+ SeichiAssist.config.getTitle1(9015)
    					+ SeichiAssist.config.getTitle3(9015) +"」")){
    				playerdata.displayTitle1No = 9015 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9015 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9015)
        					+ SeichiAssist.config.getTitle3(9015) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9016「"+ SeichiAssist.config.getTitle1(9016)
    					+ SeichiAssist.config.getTitle2(9016) + SeichiAssist.config.getTitle3(9016) +"」")){
    				playerdata.displayTitle1No = 9016 ;
    				playerdata.displayTitle2No = 9016 ;
    				playerdata.displayTitle3No = 9016 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9016)
        					+ SeichiAssist.config.getTitle2(9016) + SeichiAssist.config.getTitle3(9016) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9017「"+ SeichiAssist.config.getTitle1(9017)
    					+ SeichiAssist.config.getTitle3(9017) +"」")){
    				playerdata.displayTitle1No = 9017 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9017 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9017)
        					+ SeichiAssist.config.getTitle3(9017) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9018「"+ SeichiAssist.config.getTitle1(9018) +"」")){
    				playerdata.displayTitle1No = 9018 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 0 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9018) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9019「"+ SeichiAssist.config.getTitle1(9019)
    					+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(9019) + "」")){
    				playerdata.displayTitle1No = 9019 ;
    				playerdata.displayTitle2No = 9901 ;
    				playerdata.displayTitle3No = 9019 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9019)
    						+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(9019) + "」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9020「"+ SeichiAssist.config.getTitle1(9020)
    					+ SeichiAssist.config.getTitle3(9020) +"」")){
    				playerdata.displayTitle1No = 9020 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9020 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9020)
        					+ SeichiAssist.config.getTitle3(9020) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9021「"+ SeichiAssist.config.getTitle1(9021)
    					+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(9021) +"」")){
    				playerdata.displayTitle1No = 9021 ;
    				playerdata.displayTitle2No = 9901 ;
    				playerdata.displayTitle3No = 9021 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9021)
    						+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(9021) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9022「"+ SeichiAssist.config.getTitle1(9022)
    					+ SeichiAssist.config.getTitle3(9022) +"」")){
    				playerdata.displayTitle1No = 9022 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9022 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9022)
        					+ SeichiAssist.config.getTitle3(9022) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9023「"+ SeichiAssist.config.getTitle1(9023)
    					+ SeichiAssist.config.getTitle3(9023) +"」")){
    				playerdata.displayTitle1No = 9023 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9023 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9023)
        					+ SeichiAssist.config.getTitle3(9023) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9024「"+ SeichiAssist.config.getTitle1(9024)
    					+ SeichiAssist.config.getTitle3(9024) +"」")){
    				playerdata.displayTitle1No = 9024 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9024 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9024)
        					+ SeichiAssist.config.getTitle3(9024) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9025「"+ SeichiAssist.config.getTitle1(9025)
    					+ SeichiAssist.config.getTitle2(9025) + SeichiAssist.config.getTitle3(9025) +"」")){
    				playerdata.displayTitle1No = 9025 ;
    				playerdata.displayTitle2No = 9025 ;
    				playerdata.displayTitle3No = 9025 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9025)
    						+ SeichiAssist.config.getTitle2(9025) + SeichiAssist.config.getTitle3(9025) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9026「"+ SeichiAssist.config.getTitle1(9026)
    					+ SeichiAssist.config.getTitle3(9026) +"」")){
    				playerdata.displayTitle1No = 9026 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9026 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9026)
        					+ SeichiAssist.config.getTitle3(9026) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9027「"+ SeichiAssist.config.getTitle1(9027)
    					+ SeichiAssist.config.getTitle3(9027) +"」")){
    				playerdata.displayTitle1No = 9027 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9027 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9027)
        					+ SeichiAssist.config.getTitle3(9027) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9028「"+ SeichiAssist.config.getTitle1(9028)
    					+ SeichiAssist.config.getTitle2(9028) + SeichiAssist.config.getTitle3(9028) +"」")){
    				playerdata.displayTitle1No = 9028 ;
    				playerdata.displayTitle2No = 9028 ;
    				playerdata.displayTitle3No = 9028 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9028)
    						+ SeichiAssist.config.getTitle2(9028) + SeichiAssist.config.getTitle3(9028) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9029「"+ SeichiAssist.config.getTitle1(9029)
    					+ SeichiAssist.config.getTitle2(9029) + SeichiAssist.config.getTitle3(9029) +"」")){
    				playerdata.displayTitle1No = 9029 ;
    				playerdata.displayTitle2No = 9029 ;
    				playerdata.displayTitle3No = 9029 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9029)
    						+ SeichiAssist.config.getTitle2(9029) + SeichiAssist.config.getTitle3(9029) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9030「"+ SeichiAssist.config.getTitle1(9030)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(9030) +"」")){
    				playerdata.displayTitle1No = 9030 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 9030 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9030)
    						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(9030) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9031「"+ SeichiAssist.config.getTitle1(9031)
    					+ SeichiAssist.config.getTitle2(9908) + SeichiAssist.config.getTitle3(9031) +"」")){
    				playerdata.displayTitle1No = 9031 ;
    				playerdata.displayTitle2No = 9908 ;
    				playerdata.displayTitle3No = 9031 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9031)
    						+ SeichiAssist.config.getTitle2(9908) + SeichiAssist.config.getTitle3(9031) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9032「"+ SeichiAssist.config.getTitle1(9032)
    					+ SeichiAssist.config.getTitle3(9032) +"」")){
    				playerdata.displayTitle1No = 9032 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9032 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9032)
    						+ SeichiAssist.config.getTitle3(9032) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9033「"+ SeichiAssist.config.getTitle1(9033)
    					+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(9033) +"」")){
    				playerdata.displayTitle1No = 9033 ;
    				playerdata.displayTitle2No = 9903 ;
    				playerdata.displayTitle3No = 9033 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9033)
    						+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(9033) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9034「"+ SeichiAssist.config.getTitle1(9034)
    					+ SeichiAssist.config.getTitle3(9034) +"」")){
    				playerdata.displayTitle1No = 9034 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9034 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9034)
    						+ SeichiAssist.config.getTitle3(9034) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9035「"+ SeichiAssist.config.getTitle1(9035)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(9035) +"」")){
    				playerdata.displayTitle1No = 9035 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 9035 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9035)
    						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(9035) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No9036「"+ SeichiAssist.config.getTitle1(9036)
    					+ SeichiAssist.config.getTitle3(9036) +"」")){
    				playerdata.displayTitle1No = 9036 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 9036 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(9036)
    						+ SeichiAssist.config.getTitle3(9036) +"」が設定されました。");
    			}
    			player.openInventory(MenuInventoryData.getTitleExtraData(player));
    		}
    		//実績メニューに戻る
    		else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}

    		//次ページ
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage ++ ;
				player.openInventory(MenuInventoryData.getTitleExtraData(player));
				return;
			}

    	}
    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「極秘任務」")){
    		event.setCancelled(true);

    		//実績解除処理部分の読みこみ
    		//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//プレイヤーインベントリのクリックの場合終了
    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		if(itemstackcurrent.getType().equals(Material.BEDROCK)){
    			//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			player.sendMessage("この実績は「極秘実績」です。いろいろやってみましょう！");
    			player.openInventory(MenuInventoryData.getTitleSecretData(player));
    		}
    		else if (itemstackcurrent.getType().equals(Material.DIAMOND_BLOCK)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			if(itemmeta.getDisplayName().contains("No8001「"+ SeichiAssist.config.getTitle1(8001)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(8001) +"」")){
    				playerdata.displayTitle1No = 8001 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 8001 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(8001)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(8001) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No8002「"+ SeichiAssist.config.getTitle1(8002)
    					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(8002) +"」")){
    				playerdata.displayTitle1No = 8002 ;
    				playerdata.displayTitle2No = 9905 ;
    				playerdata.displayTitle3No = 8002 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(8002)
        					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(8002) +"」が設定されました。");
    			}
    			else if(itemmeta.getDisplayName().contains("No8003「"+ SeichiAssist.config.getTitle1(8003)
    					+ SeichiAssist.config.getTitle3(8003) +"」")){
    				playerdata.displayTitle1No = 8003 ;
    				playerdata.displayTitle2No = 0 ;
    				playerdata.displayTitle3No = 8003 ;
    				player.sendMessage("二つ名「"+ SeichiAssist.config.getTitle1(8003)
        					+ SeichiAssist.config.getTitle3(8003) +"」が設定されました。");
    			}
    			player.openInventory(MenuInventoryData.getTitleSecretData(player));

    		}
    		//実績メニューに戻る
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}
    	}
    }
  //鉱石・交換券変換システム
    @EventHandler
    public void onOreTradeEvent(InventoryCloseEvent event){
        Player player = (Player)event.getPlayer();
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);
		//エラー分岐
		if(playerdata == null){
			return;
		}
        Inventory inventory = event.getInventory();

        //インベントリサイズが36でない時終了
        if(inventory.getSize() != 36){
            return;
        }
        if(inventory.getTitle().equals(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "交換したい鉱石を入れてください")){
            int giveticket = 0;
            /*
             * step1 for文でinventory内の対象商品の個数を計算
             * 非対象商品は返却boxへ
             */
            //ガチャ景品交換インベントリの中身を取得
            ItemStack[] item = inventory.getContents();
            //ドロップ用アイテムリスト(返却box)作成
            List<ItemStack> dropitem = new ArrayList<ItemStack>();
            //余剰鉱石返却用アイテムリスト
            List<ItemStack> retore = new ArrayList<ItemStack>();
            //個数計算用変数(このやり方以外に効率的なやり方があるかもしれません)
            int coalore = 0; //石炭
            int ironore = 0; //鉄
            int goldore = 0; //金
            int lapisore = 0; //ラピスラズリ
            int diamondore = 0; //ダイアモンド
            int redstoneore = 0; //レッドストーン
            int emeraldore = 0; //エメラルド
            int quartzore = 0; //ネザー水晶
            //for文でインベントリ内のアイテムを1つずつ見る
            //鉱石・交換券変換インベントリスロットを1つずつ見る
            for(ItemStack m : item){
            	//ないなら次へ
            	if(m == null){
            		continue;
            	}
            	else if(m.getType().equals(Material.COAL_ORE)){
            		//石炭なら個数分だけcoaloreを増やす(以下同様)
            		coalore += m.getAmount();
            		continue;
            	}
            	else if(m.getType().equals(Material.IRON_ORE)){
            		ironore += m.getAmount();
            		continue;
            	}
            	else if(m.getType().equals(Material.GOLD_ORE)){
            		goldore += m.getAmount();
            		continue;
            	}
            	else if(m.getType().equals(Material.LAPIS_ORE)){
            		lapisore += m.getAmount();
            		continue;
            	}
            	else if(m.getType().equals(Material.DIAMOND_ORE)){
            		diamondore += m.getAmount();
            		continue;
            	}
            	else if(m.getType().equals(Material.REDSTONE_ORE)){
            		redstoneore += m.getAmount();
            		continue;
            	}
            	else if(m.getType().equals(Material.EMERALD_ORE)){
            		emeraldore += m.getAmount();
            		continue;
            	}
            	else if(m.getType().equals(Material.QUARTZ_ORE)){
            		quartzore += m.getAmount();
            		continue;
            	}
            	else{
            		dropitem.add(m);
            	}
            }
            //チケット計算
            giveticket = giveticket + coalore/128 + ironore/64 + goldore/8 + lapisore/8 + diamondore/4 + redstoneore/32 + emeraldore/4 + quartzore/16 ;

            //プレイヤー通知
            if(giveticket == 0){
            	player.sendMessage(ChatColor.YELLOW + "鉱石を認識しなかったか数が不足しています。全てのアイテムを返却します");
            }else{
            	player.sendMessage(ChatColor.DARK_RED + "交換券" + ChatColor.RESET + "" + ChatColor.GREEN + "を" + giveticket + "枚付与しました");
            }
            /*
             * step2 交換券をインベントリへ
             */
            ItemStack exchangeticket = new ItemStack(Material.PAPER);//※交換券の具体的なデータわからなかったので適当にしてますが、直して頂けるとありがたいです。
            ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
            itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "交換券");
            itemmeta.addEnchant(Enchantment.PROTECTION_FIRE, 1, false);
            itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            exchangeticket.setItemMeta(itemmeta);

            int count = 0;
            while(giveticket > 0){
            	if(player.getInventory().contains(exchangeticket) || !Util.isPlayerInventryFill(player)){
            		Util.addItem(player, exchangeticket);
            	}else{
            		Util.dropItem(player, exchangeticket);
            	}
            	giveticket--;
            	count ++;
            }
            if(count > 0){
            	player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
            	player.sendMessage(ChatColor.GREEN + "交換券の付与が終わりました");
            }
            /*
             * step3 非対象商品・余剰鉱石の返却
             */
            if((coalore - coalore/128 *128) != 0){
            	ItemStack c = new ItemStack(Material.COAL_ORE);
            	ItemMeta citemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
            	c.setItemMeta(citemmeta);
            	c.setAmount(coalore - coalore/128 *128);
            	retore.add(c);
            }

            if((ironore - ironore/64 *64) != 0){
            	ItemStack i = new ItemStack(Material.IRON_ORE);
            	ItemMeta iitemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_ORE);
            	i.setItemMeta(iitemmeta);
            	i.setAmount(ironore - ironore/64 *64);
            	retore.add(i);
            }

            if((goldore - goldore/8 *8) != 0){
            	ItemStack g = new ItemStack(Material.GOLD_ORE);
            	ItemMeta gitemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_ORE);
            	g.setItemMeta(gitemmeta);
            	g.setAmount(goldore - goldore/8 *8);
            	retore.add(g);
            }

            if((lapisore - lapisore/8 *8) != 0){
            	ItemStack l = new ItemStack(Material.LAPIS_ORE);
            	ItemMeta litemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAPIS_ORE);
            	l.setItemMeta(litemmeta);
            	l.setAmount(lapisore - lapisore/8 *8);
            	retore.add(l);
            }

            if((diamondore - diamondore/4 *4) != 0){
            	ItemStack d = new ItemStack(Material.DIAMOND_ORE);
            	ItemMeta ditemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_ORE);
            	d.setItemMeta(ditemmeta);
            	d.setAmount(diamondore - diamondore/4 *4);
            	retore.add(d);
            }

            if((redstoneore - redstoneore/32 *32) != 0){
            	ItemStack r = new ItemStack(Material.REDSTONE_ORE);
            	ItemMeta ritemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE_ORE);
            	r.setItemMeta(ritemmeta);
            	r.setAmount(redstoneore - redstoneore/32 *32);
            	retore.add(r);
            }

            if((emeraldore - emeraldore/4 *4) != 0){
            	ItemStack e = new ItemStack(Material.EMERALD_ORE);
            	ItemMeta eitemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
            	e.setItemMeta(eitemmeta);
            	e.setAmount(emeraldore - emeraldore/4 *4);
            	retore.add(e);
            }

            if((quartzore - quartzore/16 *16) != 0){
            	ItemStack q = new ItemStack(Material.QUARTZ_ORE);
            	ItemMeta qitemmeta = Bukkit.getItemFactory().getItemMeta(Material.QUARTZ_ORE);
            	q.setItemMeta(qitemmeta);
            	q.setAmount(quartzore - quartzore/16 *16);
            	retore.add(q);
            }

            //返却処理
            for(ItemStack m : dropitem){
                if(!Util.isPlayerInventryFill(player)){
                    Util.addItem(player,m);
                }else{
                    Util.dropItem(player,m);
                }
            }
            for(ItemStack m : retore){
            	if(!Util.isPlayerInventryFill(player)){
            		Util.addItem(player,m);
            	}else{
            		Util.dropItem(player, m);
            	}
            }
        }
    }

    //ギガンティック→椎名林檎交換システム
    @EventHandler
    public void onGachaRingoEvent(InventoryCloseEvent event){
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
        if(inventory.getTitle().equals(ChatColor.GOLD + "" + ChatColor.BOLD + "椎名林檎と交換したい景品を入れてネ")){
            //PlayerInventory pinventory = player.getInventory();
            //ItemStack itemstack = pinventory.getItemInMainHand();
            int giveringo = 0;
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
            int giga = 0;
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
                            //1個につき椎名林檎n個と交換する
                        	giveringo += (SeichiAssist.config.rateGiganticToRingo()*amount);
                            giga++;
                        }else{
                            //それ以外アイテム返却
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
            }else if(!(giga > 0)){
                player.sendMessage(ChatColor.YELLOW + "ギガンティック大当り景品を認識しませんでした。全てのアイテムを返却します");
            }else{
                player.sendMessage(ChatColor.GREEN + "ギガンティック大当り景品を" + giga + "個認識しました");
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
             * step3 椎名林檎をインベントリへ
             */
            ItemStack ringo = Util.getMaxRingo(Util.getName(player));
            int count = 0;
            while(giveringo > 0){
                if(player.getInventory().contains(ringo) || !Util.isPlayerInventryFill(player)){
                    Util.addItem(player,ringo);
                }else{
                    Util.dropItem(player,ringo);
                }
                giveringo--;
                count++;
            }
            if(count > 0){
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                player.sendMessage(ChatColor.GREEN + ""+count+ "個の" + ChatColor.GOLD + "椎名林檎" + ChatColor.WHITE + "を受け取りました");
            }
        }

    }

    //投票ptメニュー
    @EventHandler
    public void onVotingMenuEvent(InventoryClickEvent event){
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
    	Mana mana = playerdata.activeskilldata.mana;

    	//投票妖精に関する部分の読み込み
    	VotingFairyTaskRunnable VFTR = new VotingFairyTaskRunnable() ;


    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票ptメニュー")){
    		event.setCancelled(true);

    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		//投票pt受取
    		if(itemstackcurrent.getType().equals(Material.DIAMOND)){

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

					//ピッケルプレゼント処理(レベル50になるまで)
					if(playerdata.level < 50){
						ItemStack pickaxe = ItemData.getSuperPickaxe(1);
						if(!Util.isPlayerInventryFill(player)){
							Util.addItem(player, pickaxe);
						}else{
							Util.dropItem(player, pickaxe);
						}
					}

					//エフェクトポイント加算処理
					playerdata.activeskilldata.effectpoint += 10;

					n--;
					count++;
				}

				player.sendMessage(ChatColor.GOLD + "投票特典" + ChatColor.WHITE + "(" + count + "票分)を受け取りました");
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);

				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(itemmeta);
    			player.openInventory(MenuInventoryData.getVotingMenuData(player));
			}

    		else if(itemstackcurrent.getType().equals(Material.BOOK_AND_QUILL)){
				// 投票リンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "https://minecraft.jp/servers/54d3529e4ddda180780041a7/vote");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

    		//妖精召喚
    		else if(itemstackcurrent.getType().equals(Material.GHAST_TEAR)){

    			//プレイヤーレベルが50に達していないとき
    			if(playerdata.level < 50){
    				player.sendMessage(ChatColor.GOLD + "プレイヤーレベルが足りません") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
    				return;
    			}

    			//既に妖精召喚している場合終了
    			if( playerdata.canVotingFairyUse == true ){
    				player.sendMessage(ChatColor.GOLD + "既に妖精を召喚しています") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
    				return;
    			}

    			//投票ptが足りない場合終了
    			if( playerdata.activeskilldata.effectpoint < 10){
    				player.sendMessage(ChatColor.GOLD + "投票ptが足りません") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
    				return;
    			}

    			//召喚した時間を取り出す
    			playerdata.VotingFairyStartTime = new GregorianCalendar(
    					Calendar.getInstance().get(Calendar.YEAR),
    					Calendar.getInstance().get(Calendar.MONTH),
    					Calendar.getInstance().get(Calendar.DATE),
    					Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    					Calendar.getInstance().get(Calendar.MINUTE)
    					);

    			playerdata.VotingFairyEndTime = new GregorianCalendar(
    					Calendar.getInstance().get(Calendar.YEAR),
    					Calendar.getInstance().get(Calendar.MONTH),
    					Calendar.getInstance().get(Calendar.DATE),
    					(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+4),
    					(Calendar.getInstance().get(Calendar.MINUTE)+1)
    					);

    			player.sendMessage(ChatColor.GOLD + "妖精を召喚しました") ;
    			player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2) ;
    			VFTR.SummonFairy(player);
    			player.closeInventory() ;
    		}

    		//ガチャりんご渡し
    		else if(itemstackcurrent.getType().equals(Material.GOLDEN_APPLE)){

        		//妖精を召喚していないとき
	    		if( playerdata.canVotingFairyUse == false ){
	    			player.sendMessage(ChatColor.GOLD + "妖精を召喚してください") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
					return;
	    		}

	    		//渡すメニューを開かせる
	    		player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getPassAppleData(player));
    		}

    		else if(itemstackcurrent.getType().equals(Material.ROTTEN_FLESH)){
    			if(playerdata.canVotingFairyUse == true){
    				player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, (float) 0.1) ;
	    			playerdata.canVotingFairyUse = false ;
	    			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "そっか、、、それじゃ僕はこれで失礼するよ");
	    			player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "妖精は何処かへ行ってしまったようだ...");
	    			playerdata.hasVotingFairyMana = 0 ;
	    			player.closeInventory();
    			}
    			else {
	    			player.sendMessage(ChatColor.GOLD + "妖精を召喚してください") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
    			}
    		}

    		//りんご残量確認
    		else if(itemstackcurrent.getType().equals(Material.BOWL)){
    			if(playerdata.canVotingFairyUse == true){
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				VFTR.askApple(player);
    			}
    			else {
	    			player.sendMessage(ChatColor.GOLD + "妖精を召喚してください") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
					player.closeInventory();
    			}
    		}

    		//時間確認
    		else if(itemstackcurrent.getType().equals(Material.COMPASS)){
    			if(playerdata.canVotingFairyUse == true){
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    				VFTR.askTime(player);
    			}
    			else {
	    			player.sendMessage(ChatColor.GOLD + "妖精を召喚してください") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
					player.closeInventory();
    			}
    		}

    		//棒メニューに戻る
    		else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}

    		//Debug用
    		if(SeichiAssist.DEBUG){
	    		if(itemstackcurrent.getType().equals(Material.GOLD_INGOT)){
	    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	    			playerdata.activeskilldata.effectpoint += 10 ;
	    			player.openInventory(MenuInventoryData.getVotingMenuData(player));
	    		}
	    		else if(itemstackcurrent.getType().equals(Material.IRON_INGOT)){
	    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	    			playerdata.activeskilldata.effectpoint = 0 ;
	    			player.openInventory(MenuInventoryData.getVotingMenuData(player));
	    		}
	    		else if(itemstackcurrent.getType().equals(Material.COAL)){
	    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	    			mana.setMana(0);
	    			player.openInventory(MenuInventoryData.getVotingMenuData(player));
	    		}
	    		else if(itemstackcurrent.getType().equals(Material.EMERALD)){
	    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	    			player.sendMessage(ChatColor.GOLD + "開始時刻: " + Util.showTime(playerdata.VotingFairyStartTime)) ;
    				player.sendMessage(ChatColor.GOLD + "終了時刻: " + Util.showTime(playerdata.VotingFairyEndTime)) ;
	    			player.openInventory(MenuInventoryData.getVotingMenuData(player));
	    		}
    		}
    	}

    	//インベントリ名が以下の時処理
    	if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "渡すガチャりんごの量を決めて下さい")){
    		event.setCancelled(true);

    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}


    		if (itemstackcurrent.getType().equals(Material.PAPER)){
    			ItemMeta itemmeta = itemstackcurrent.getItemMeta();
    			if(itemmeta.getDisplayName().contains("ガチャりんごを" + playerdata.giveApple + "個渡す")){
    				VFTR.GiveApple(player);
    				player.closeInventory();
    			}
    			else if(itemmeta.getDisplayName().contains("渡す量を 1 増やす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple < 999999)
	        			playerdata.giveApple += 1 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    			else if(itemmeta.getDisplayName().contains("渡す量を 10 増やす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple < 999990)
	        			playerdata.giveApple += 10 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    			else if(itemmeta.getDisplayName().contains("渡す量を 100 増やす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple < 999900)
	        			playerdata.giveApple += 100 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    			else if(itemmeta.getDisplayName().contains("渡す量を 1000 増やす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple < 999000)
	        			playerdata.giveApple += 1000 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    			else if(itemmeta.getDisplayName().contains("渡す量を 10000 増やす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple < 990000)
	        			playerdata.giveApple += 10000 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    			else if(itemmeta.getDisplayName().contains("渡す量を 1 減らす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple >= 1)
	        			playerdata.giveApple -= 1 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    			else if(itemmeta.getDisplayName().contains("渡す量を 10 減らす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple >= 10)
	        			playerdata.giveApple -= 10 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    			else if(itemmeta.getDisplayName().contains("渡す量を 100 減らす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple >= 100)
	        			playerdata.giveApple -= 100 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    			else if(itemmeta.getDisplayName().contains("渡す量を 1000 減らす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple >= 1000)
	        			playerdata.giveApple -= 1000 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    			else if(itemmeta.getDisplayName().contains("渡す量を 10000 減らす")){
	        		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
	        		if(playerdata.giveApple >= 10000)
	        			playerdata.giveApple -= 10000 ;
	        		player.openInventory(MenuInventoryData.getPassAppleData(player));
	    		}
    		}
    	}
    }

    @EventHandler
	public void onHomeMenuEvent(InventoryClickEvent event){
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
		//インベントリサイズが27でない時終了
		if(topinventory.getSize() != 27){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);
		ItemMeta itemmeta = itemstackcurrent.getItemMeta();

		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ホームメニュー")){
    		event.setCancelled(true);

    		if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
    			return;
    		}

    		/*
    		 * クリックしたボタンに応じた各処理内容の記述ここから
    		 */

    		if(itemmeta.getDisplayName().contains("ホームポイントにワープ")){
    			player.chat("/home");
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    		}
    		else if(itemmeta.getDisplayName().contains("ホームポイントを設定")){
    			player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
    			playerdata.selectHomeNum = 0;
    			player.openInventory(MenuInventoryData.getCheckSetHomeMenuData(player));
    		}
    		for(int x = 1 ; x <= SeichiAssist.config.getSubHomeMax() ; x++){
    			if(itemmeta.getDisplayName().contains("サブホームポイント"+ (x) + "にワープ")){
    				player.chat("/subhome " + (x));
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			}
    			else if(itemmeta.getDisplayName().contains("サブホームポイント"+ (x) + "の情報")){
    				player.chat("/subhome name " + (x));
    				player.closeInventory();
    				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			}
    			else if(itemmeta.getDisplayName().contains("サブホームポイント"+ (x) + "を設定")){
        			player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
        			playerdata.selectHomeNum = x;
        			player.openInventory(MenuInventoryData.getCheckSetHomeMenuData(player));
    			}
    		}

		}
		else if(topinventory.getTitle().contains("ホームポイントを変更しますか?")){
			event.setCancelled(true);

			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			if(itemmeta.getDisplayName().contains("変更する")){
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			if(playerdata.selectHomeNum == 0) player.chat("/sethome");
    			else player.chat("/subhome set " + playerdata.selectHomeNum);
    			player.closeInventory();
    		}
    		else if(itemmeta.getDisplayName().contains("変更しない")){
    			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    			player.closeInventory();
    		}
		}
	}

    @EventHandler
	public void onGiganticBerserkMenuEvent(InventoryClickEvent event){
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
		//インベントリサイズが54でない時終了
		if(topinventory.getSize() != 54){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "スキルを進化させますか?")){
    		event.setCancelled(true);
    		if (itemstackcurrent.getType().equals(Material.NETHER_STAR)){
    			playerdata.GBstage ++ ;
    			playerdata.GBlevel = 0;
    			playerdata.GBexp = 0;
    			playerdata.isGBStageUp = false;
    			player.playSound(player.getLocation(), Sound.BLOCK_END_GATEWAY_SPAWN, 1, (float) 0.5);
    			player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_AMBIENT, 1, (float) 0.8);
    			player.openInventory(MenuInventoryData.getGiganticBerserkEvolution2Menu(player));
    		}
		}
		else if(topinventory.getTitle().equals(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "スキルを進化させました")){
			event.setCancelled(true);
		}
    }
}
