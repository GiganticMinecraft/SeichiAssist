package com.github.unchama.seichiassist.listener;

import com.github.unchama.seasonalevents.events.valentine.Valentine;
import com.github.unchama.seichiassist.*;
import com.github.unchama.seichiassist.achievement.SeichiAchievement;
import com.github.unchama.seichiassist.data.*;
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.minestack.HistoryData;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.seichiassist.task.CoolDownTask;
import com.github.unchama.seichiassist.task.VotingFairyTask;
import com.github.unchama.seichiassist.util.exp.ExperienceManager;
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.util.collection.ImmutableListFactory;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.unchama.util.ActionStatus.Fail;

public class PlayerInventoryListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.Companion.getPlayermap();
	List<GachaPrize> gachadatalist = SeichiAssist.Companion.getGachadatalist();
	SeichiAssist plugin = SeichiAssist.Companion.getInstance();
	private Config config = SeichiAssist.Companion.getSeichiAssistConfig();
	private DatabaseGateway databaseGateway = SeichiAssist.Companion.getDatabaseGateway();
	//サーバー選択メニュー
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
		if(he.getType() != EntityType.PLAYER){
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

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_RED + "" + ChatColor.BOLD + "サーバーを選択してください")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}
			ItemMeta meta = itemstackcurrent.getItemMeta();

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			ByteArrayDataOutput byteArrayDataOutput = ByteStreams
					.newDataOutput();
			//ページ変更処理
			if(meta.getDisplayName().contains("アルカディアサーバー")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s1");
				player.sendPluginMessage(SeichiAssist.Companion.getInstance(), "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("エデンサーバー")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s2");
				player.sendPluginMessage(SeichiAssist.Companion.getInstance(), "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("ヴァルハラサーバー")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s3");
				player.sendPluginMessage(SeichiAssist.Companion.getInstance(), "BungeeCord",
						byteArrayDataOutput.toByteArray());
			}else if(meta.getDisplayName().contains("公共施設サーバー")){
				byteArrayDataOutput.writeUTF("Connect");
				byteArrayDataOutput.writeUTF("s7");
				player.sendPluginMessage(SeichiAssist.Companion.getInstance(), "BungeeCord",
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
		if(he.getType() != EntityType.PLAYER){
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

		final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;
		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニュー")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			//経験値変更用のクラスを設定
			ExperienceManager expman = new ExperienceManager(player);

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			//ページ変更処理
			if (itemstackcurrent.getType() == Material.NETHER_STAR) {
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

			}else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData2(player));
			}

			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
			}

			else if(itemstackcurrent.getType() == Material.CHEST){
				//レベルが足りない場合処理終了
				if( playerdata.getLevel() < SeichiAssist.Companion.getSeichiAssistConfig().getMineStacklevel(1)){
					player.sendMessage(ChatColor.GREEN + "整地レベルが"+ SeichiAssist.Companion.getSeichiAssistConfig().getMineStacklevel(1)+ "以上必要です");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					return;
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMainMenu(player));
			}
			//スキルメニューを開く
			else if(itemstackcurrent.getType() == Material.ENCHANTED_BOOK){
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
			}
			//整地神番付を開く
			else if(itemstackcurrent.getType() == Material.COOKIE && itemstackcurrent.getItemMeta().getDisplayName().contains("整地神")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getRankingList(player,0));
			}
			//整地神番付を開く
			else if(itemstackcurrent.getType() == Material.COOKIE && itemstackcurrent.getItemMeta().getDisplayName().contains("ログイン神")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getRankingList_playtick(player,0));
			}
			//整地神番付を開く
			else if(itemstackcurrent.getType() == Material.COOKIE && itemstackcurrent.getItemMeta().getDisplayName().contains("投票神")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getRankingList_p_vote(player,0));
			}

			//溜まったガチャ券をインベントリへ
			else if(isSkull
					&& itemstackcurrent.getItemMeta().getDisplayName().equals(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地報酬ガチャ券を受け取る")){
				//連打防止クールダウン処理
				if (playerdata.getGachacooldownflag()) {
					//連打による負荷防止の為クールダウン処理
					new CoolDownTask(player,false,false,true).runTaskLater(plugin,20);
				} else {
					return;
				}

				ItemStack skull = Util.getskull(Util.getName(player));
				int count = 0;
				while(playerdata.getGachapoint() >= config.getGachaPresentInterval() && count < 576){
					playerdata.setGachapoint(playerdata.getGachapoint() - config.getGachaPresentInterval());
					if(player.getInventory().contains(skull) || !Util.isPlayerInventoryFull(player)){
						Util.addItem(player,skull);
					}else{
						Util.dropItem(player,skull);
					}
					count++;
				}
				//プレイヤーデータを更新
				playerdata.setLastgachapoint(playerdata.getGachapoint());

				if(count > 0){
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + count + "枚" + ChatColor.WHITE + "プレゼントフォーユー");
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
				}

				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemmeta.setLore(MenuInventoryData.GachaGetButtonLore(playerdata));
				itemstackcurrent.setItemMeta(itemmeta);
			}

			//運営からのガチャ券受け取り
			else if(isSkull
					&& itemstackcurrent.getItemMeta().getDisplayName().equals(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "運営からのガチャ券を受け取る")){

				//nは最新のnumofsorryforbugの値になる(上限値576個)
				int n = databaseGateway.playerDataManipulator.givePlayerBug(player,playerdata);
				//0だったら処理終了
				if(n == 0){
					return;
				}

				ItemStack skull = Util.getForBugskull(Util.getName(player));
				int count = 0;
				while(n > 0){
					playerdata.setNumofsorryforbug(playerdata.getNumofsorryforbug() - 1);
					if(player.getInventory().contains(skull) || !Util.isPlayerInventoryFull(player)){
						Util.addItem(player,skull);
					}else{
						Util.dropItem(player,skull);
					}
					n--;
					count++;
				}

				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
				player.sendMessage(ChatColor.GREEN + "運営チームから" + count + "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "を受け取りました");

				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemmeta.setLore(MenuInventoryData.SorryGachaGetButtonLore(playerdata));
				itemstackcurrent.setItemMeta(itemmeta);
			}

			//経験値を消費してプレイヤーの頭を召喚
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_Villager")){
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
				skullmeta.setOwningPlayer(player);
				//バレンタイン中(イベント中かどうかの判断はSeasonalEvent側で行う)
				skullmeta = Valentine.playerHeadLore(skullmeta);
				skull.setItemMeta(skullmeta);

				//渡すか、落とすか
				if(player.getInventory().contains(skull) || !Util.isPlayerInventoryFull(player)){
					Util.addItem(player,skull);
				}else{
					Util.dropItem(player,skull);
				}
				player.sendMessage(ChatColor.GOLD + "経験値10000を消費して自分の頭を召喚しました");
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			}

			else if(itemstackcurrent.getType() == Material.STONE_BUTTON){
				playerdata.setGachaflag(!playerdata.getGachaflag());
				if(playerdata.getGachaflag()){
					player.sendMessage(ChatColor.GREEN + "毎分のガチャ券受け取り:ON");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					ItemMeta itemmeta = itemstackcurrent.getItemMeta();
					List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.GREEN + "毎分受け取ります"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更"
							);
					itemmeta.setLore(lore);
					itemstackcurrent.setItemMeta(itemmeta);
				}else{
					player.sendMessage(ChatColor.RED + "毎分のガチャ券受け取り:OFF");
					player.sendMessage(ChatColor.GREEN + "ガチャ券受け取りボタンを押すともらえます");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1, 1);
					ItemMeta itemmeta = itemstackcurrent.getItemMeta();
					List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.RED + "後でまとめて受け取ります"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更"
							);
					itemmeta.setLore(lore);
					itemstackcurrent.setItemMeta(itemmeta);
				}
			}

			else if(itemstackcurrent.getType() == Material.DIAMOND_PICKAXE){
				// TODO this fragment is deleted.
			} else if(itemstackcurrent.getType() == Material.FLINT_AND_STEEL){
				// 死亡メッセージ表示トグル
				playerdata.setDispkilllogflag(!playerdata.getDispkilllogflag());
				if(playerdata.getDispkilllogflag()){
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
			else if(itemstackcurrent.getType() == Material.JUKEBOX){
				if(playerdata.getEverysoundflag() && playerdata.getEverymessageflag()){
					playerdata.setEverysoundflag(!playerdata.getEverysoundflag());
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "消音可能な全体通知音を消音します");
				}
				else if(!playerdata.getEverysoundflag() && playerdata.getEverymessageflag()){
					playerdata.setEverymessageflag(!playerdata.getEverymessageflag());
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "非表示可能な全体メッセージを非表示にします");
				}
				else {
					playerdata.setEverysoundflag(!playerdata.getEverysoundflag());
					playerdata.setEverymessageflag(!playerdata.getEverymessageflag());
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.GREEN + "非表示/消音設定を解除しました");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.dispWinSoundToggleMeta(playerdata,itemmeta));
			}

			//追加
			else if(itemstackcurrent.getType() == Material.BARRIER){
				// ワールドガード保護表示トグル
				playerdata.setDispworldguardlogflag(!playerdata.getDispworldguardlogflag());
				if(playerdata.getDispworldguardlogflag()){
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "ワールドガード保護メッセージ:表示");
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "ワールドガード保護メッセージ:隠す");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.dispWorldGuardLogToggleMeta(playerdata,itemmeta));
			}

			else if(itemstackcurrent.getType() == Material.IRON_SWORD){
				// 死亡メッセージ表示トグル
				playerdata.setPvpflag(!playerdata.getPvpflag());
				if(playerdata.getPvpflag()){
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "PvP:ON");
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "PvP:OFF");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.dispPvPToggleMeta(playerdata,itemmeta));
			}

			else if(isSkull && itemstackcurrent.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.getName() + "の統計データ")){
				// 整地量表示トグル
				playerdata.toggleExpBarVisibility();
				playerdata.notifyExpBarVisibility();
				SkullMeta skullmeta = (SkullMeta)itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.dispExpBarToggleMeta(playerdata,skullmeta));
			}

			else if(itemstackcurrent.getType() == Material.BEACON){
				// spawnコマンド実行
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/spawn");
			}
			//HomeMenu
			else if(itemstackcurrent.getType() == Material.BED){
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 1.5);
				player.openInventory(MenuInventoryData.getHomeMenuData(player));
			}

			else if(itemstackcurrent.getType() == Material.COMPASS){
				// /rtp コマンド実行
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 1.5);
				player.chat("/rtp");
			}

			else if(itemstackcurrent.getType() == Material.WORKBENCH){
				// /fc craftコマンド実行
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/fc craft");
			}

			else if(itemstackcurrent.getType() == Material.BOOK){
				// wikiリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + config.getUrl("official"));
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType() == Material.PAPER){
				// 運営方針とルールリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + config.getUrl("rule"));
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType() == Material.MAP){
				// 鯖マップリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + config.getUrl("map"));
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();

			}

			else if(itemstackcurrent.getType() == Material.SIGN){
				//JMSリンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + config.getUrl("jms")
						);
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType() == Material.ENDER_PORTAL_FRAME){
				//ver0.3.2 四次元ポケットを開く
				//レベルが足りない場合処理終了
				if( playerdata.getLevel() < SeichiAssist.Companion.getSeichiAssistConfig().getPassivePortalInventorylevel()){
					player.sendMessage(ChatColor.GREEN + "4次元ポケットを開くには整地レベルが"+ SeichiAssist.Companion.getSeichiAssistConfig().getPassivePortalInventorylevel()+ "以上必要です");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					return;
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, 1, (float) 0.1);

				//レベルに応じたポケットサイズ変更処理
				//アイテム消失を防ぐ為、現在のサイズよりも四次元ポケットサイズが大きくなる場合のみ拡張処理する
				if(playerdata.getInventory().getSize() < playerdata.getPocketSize()){
					//現在の四次元ポケットの中身を取得
					ItemStack[] item = playerdata.getInventory().getContents();
					//新しいサイズの四次元ポケットを作成
					Inventory newsizepocket = Bukkit.getServer().createInventory(null,playerdata.getPocketSize(),ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "4次元ポケット");
					//for文で一個ずつ新しいサイズのポケットに入れてく
					int i = 0;
					for (ItemStack m : item) {
						newsizepocket.setItem(i, m);
						i++;
					}
					//出来たら置き換える
					playerdata.setInventory(newsizepocket);
				}
				//インベントリを開く
				player.openInventory(playerdata.getInventory());
			}


			else if(itemstackcurrent.getType() == Material.ENDER_CHEST){
				//レベルが足りない場合処理終了
				if( playerdata.getLevel() < SeichiAssist.Companion.getSeichiAssistConfig().getDokodemoEnderlevel()){
					player.sendMessage(ChatColor.GREEN + "整地レベルが"+ SeichiAssist.Companion.getSeichiAssistConfig().getDokodemoEnderlevel()+ "以上必要です");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					return;
				}
				//どこでもエンダーチェストを開く
				player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, 1, (float) 1.0);
				player.openInventory(player.getEnderChest());
			}



			else if(itemstackcurrent.getType() == Material.BUCKET){
				//ゴミ箱を開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 1.5);
				//インベントリを開く
				player.openInventory(Bukkit.createInventory(null, 9*4 ,ChatColor.RED + "" + ChatColor.BOLD + "ゴミ箱(取扱注意)"));
			}


			else if(itemstackcurrent.getType() == Material.NOTE_BLOCK){
				//ガチャ景品交換システムを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 0.5);
				//インベントリを開く
				player.openInventory(Bukkit.createInventory(null, 9*4 ,ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "交換したい景品を入れてください"));
			}

			else if(itemstackcurrent.getType() == Material.END_CRYSTAL){
				//実績メニューを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				//インベントリを開く
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
			}

			else if(itemstackcurrent.getType() == Material.DIAMOND_ORE){
				//鉱石・交換券変換システムを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 0.5);
				//インベントリを開く
				player.openInventory(Bukkit.createInventory(null, 9*4 ,ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "交換したい鉱石を入れてください"));
			}

			else if(itemstackcurrent.getType() == Material.GOLDEN_APPLE){
				//椎名林檎変換システムを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 0.5);
				//インベントリを開く
				player.openInventory(Bukkit.createInventory(null, 9*4 ,ChatColor.GOLD + "" + ChatColor.BOLD + "椎名林檎と交換したい景品を入れてネ"));
			}

			else if(itemstackcurrent.getType() == Material.DIAMOND_AXE && itemstackcurrent.getItemMeta().getDisplayName().contains("限定タイタン")){
				//椎名林檎変換システムを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, (float) 0.5);
				//インベントリを開く
				player.openInventory(Bukkit.createInventory(null, 9*4 ,ChatColor.GOLD + "" + ChatColor.BOLD + "修繕したい限定タイタンを入れてネ"));
			}

			// インベントリ共有ボタン
			else if(itemstackcurrent.getType() == Material.TRAPPED_CHEST &&
					itemstackcurrent.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "インベントリ共有")){
				player.chat("/shareinv");
				itemstackcurrent.setItemMeta(MenuInventoryData.dispShareInvMeta(playerdata));
			}

			else if(itemstackcurrent.getType() == Material.DIAMOND){
				//投票ptメニューを開く
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				//インベントリを開く
				player.openInventory(MenuInventoryData.getVotingMenuData(player));
			} else if (itemstackcurrent.getType() == Material.TRAPPED_CHEST) {
				if (!Valentine.isInEvent) {
					return;
				}
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, (float) 0.5);
				Valentine.giveChoco(player);
				playerdata.setHasChocoGave(true);
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}
			final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			//ページ変更処理
			// ->
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
			}

			else if(itemstackcurrent.getType() == Material.DIAMOND_PICKAXE){
				// 複数破壊トグル

				if(playerdata.getLevel() >= SeichiAssist.Companion.getSeichiAssistConfig().getMultipleIDBlockBreaklevel()){
					playerdata.setMultipleidbreakflag(!playerdata.getMultipleidbreakflag());
					if(playerdata.getMultipleidbreakflag()){
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

			else if(itemstackcurrent.getType() == Material.DIAMOND_AXE){
				playerdata.setChestflag(false);
				player.sendMessage(ChatColor.GREEN + "スキルでのチェスト破壊を無効化しました。");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player));
			}
			else if(itemstackcurrent.getType() == Material.CHEST){
				playerdata.setChestflag(true);
				player.sendMessage(ChatColor.RED + "スキルでのチェスト破壊を有効化しました。");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player));
			}


			else if(itemstackcurrent.getType() == Material.STICK){
				player.sendMessage(ChatColor.WHITE + "パッシブスキル:" + ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Gigantic" + ChatColor.RED + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Berserk" + ChatColor.WHITE + "はレベル10以上から使用可能です");
				player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
			}

			else if(itemstackcurrent.getType() == Material.WOOD_SWORD || itemstackcurrent.getType() == Material.STONE_SWORD || itemstackcurrent.getType() == Material.GOLD_SWORD || itemstackcurrent.getType() == Material.IRON_SWORD || itemstackcurrent.getType() == Material.DIAMOND_SWORD){
				if(!playerdata.isGBStageUp()){
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
		if(he.getType() != EntityType.PLAYER){
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
			final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;

			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			int type;
			String name;
			int skilllevel;
			//ARROWSKILL
			type = ActiveSkill.ARROW.gettypenum();
			for(skilllevel = 4;skilllevel <= 9 ; skilllevel++){
				name = ActiveSkill.ARROW.getName(skilllevel);
				if(itemstackcurrent.getType() == ActiveSkill.ARROW.getMaterial(skilllevel)){
					PotionMeta potionmeta =(PotionMeta)itemstackcurrent.getItemMeta();
					if(potionmeta.getBasePotionData().getType() == ActiveSkill.ARROW.getPotionType(skilllevel)){
						if(playerdata.getActiveskilldata().skilltype == type
								&& playerdata.getActiveskilldata().skillnum == skilllevel){
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
							player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
							playerdata.getActiveskilldata().skilltype = 0 ;
							playerdata.getActiveskilldata().skillnum = 0 ;
						}else{
							playerdata.getActiveskilldata().updateSkill(player,type,skilllevel,1);
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
				if(itemstackcurrent.getType() == ActiveSkill.MULTI.getMaterial(skilllevel)){
					if(playerdata.getActiveskilldata().skilltype == type
							&& playerdata.getActiveskilldata().skillnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
						playerdata.getActiveskilldata().skilltype = 0 ;
						playerdata.getActiveskilldata().skillnum = 0 ;
					}else{
						playerdata.getActiveskilldata().updateSkill(player,type,skilllevel,1);
						player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + name + "  が選択されました");
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}
			//BREAKSKILL
			type = ActiveSkill.BREAK.gettypenum();
			for(skilllevel = 1;skilllevel <= 9 ; skilllevel++){
				name = ActiveSkill.BREAK.getName(skilllevel);
				if(itemstackcurrent.getType() == ActiveSkill.BREAK.getMaterial(skilllevel)){
					if(playerdata.getActiveskilldata().skilltype == ActiveSkill.BREAK.gettypenum()
							&& playerdata.getActiveskilldata().skillnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
						playerdata.getActiveskilldata().skilltype = 0 ;
						playerdata.getActiveskilldata().skillnum = 0 ;
					}else{
						playerdata.getActiveskilldata().updateSkill(player,type,skilllevel,1);
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
				if(itemstackcurrent.getType() == ActiveSkill.WATERCONDENSE.getMaterial(skilllevel)){
					if(playerdata.getActiveskilldata().assaulttype == type
							&& playerdata.getActiveskilldata().assaultnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
						playerdata.getActiveskilldata().assaulttype = 0 ;
						playerdata.getActiveskilldata().assaultnum = 0 ;
					}else{
						playerdata.getActiveskilldata().updateAssaultSkill(player,type,skilllevel,1);
						player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + name + "  が選択されました");
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}

			//LAVA
			type = ActiveSkill.LAVACONDENSE.gettypenum();
			for(skilllevel = 7;skilllevel <= 9 ; skilllevel++){
				name = ActiveSkill.LAVACONDENSE.getName(skilllevel);
				if(itemstackcurrent.getType() == ActiveSkill.LAVACONDENSE.getMaterial(skilllevel)){
					if(playerdata.getActiveskilldata().assaulttype == type
							&& playerdata.getActiveskilldata().assaultnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
						playerdata.getActiveskilldata().assaulttype = 0 ;
						playerdata.getActiveskilldata().assaultnum = 0 ;
					}else{
						playerdata.getActiveskilldata().updateAssaultSkill(player,type,skilllevel,1);
						player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + name + "  が選択されました");
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}

			type = ActiveSkill.FLUIDCONDENSE.gettypenum();
			skilllevel = 10;
			if(itemstackcurrent.getType() == ActiveSkill.FLUIDCONDENSE.getMaterial(skilllevel)){
				if(playerdata.getActiveskilldata().assaultnum == skilllevel && playerdata.getActiveskilldata().assaulttype == type){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
					playerdata.getActiveskilldata().assaulttype = 0 ;
					playerdata.getActiveskilldata().assaultnum = 0 ;
				}else{
					playerdata.getActiveskilldata().updateAssaultSkill(player,type,skilllevel,1);
					player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + "ヴェンダー・ブリザード" + " が選択されました");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}
			}

			//アサルトアーマー
			type = ActiveSkill.ARMOR.gettypenum();
			skilllevel = 10;
			if(itemstackcurrent.getType() == ActiveSkill.ARMOR.getMaterial(skilllevel)){
				if(playerdata.getActiveskilldata().assaultnum == skilllevel && playerdata.getActiveskilldata().assaulttype == type){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "選択を解除しました");
					playerdata.getActiveskilldata().assaulttype = 0 ;
					playerdata.getActiveskilldata().assaultnum = 0 ;
				}else{
					playerdata.getActiveskilldata().updateAssaultSkill(player,type,skilllevel,1);
					player.sendMessage(ChatColor.DARK_GREEN + "アサルトスキル:" + "アサルト・アーマー" + " が選択されました");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}
			}

			//ページ変更処理
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
			}
			else if(itemstackcurrent.getType() == Material.STONE_BUTTON){
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
					playerdata.getActiveskilldata().reset();
					//スキルポイント更新
					playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
					//リセット音を流す
					player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1, (float) 0.1);
					//メッセージを流す
					player.sendMessage(ChatColor.LIGHT_PURPLE + "アクティブスキルポイントをリセットしました");
					//メニューを開く
					player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
				}
			}
			else if(itemstackcurrent.getType() == Material.GLASS){
				if(playerdata.getActiveskilldata().skilltype == 0 && playerdata.getActiveskilldata().skillnum == 0
				&& playerdata.getActiveskilldata().assaulttype == 0 && playerdata.getActiveskilldata().assaultnum == 0
						){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に全ての選択は削除されています");
				}else{
					playerdata.getActiveskilldata().clearSellect(player);

				}
			}
			else if(itemstackcurrent.getType() == Material.BOOKSHELF){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1, (float) 0.5);
				player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player));
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 0.1);
				player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
				return;
			}else if(itemstackcurrent.getType() == Material.GLASS){
				if(playerdata.getActiveskilldata().effectnum == 0){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else{
					playerdata.getActiveskilldata().effectnum = 0;
					player.sendMessage(ChatColor.GREEN + "エフェクト:未設定  が選択されました");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}
				return;
			}else if(itemstackcurrent.getType() == Material.BOOK_AND_QUILL){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBuyRecordMenuData(player));
				return;
			}else{
				ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
				for (final ActiveSkillEffect activeSkillEffect : skilleffect) {
					if (itemstackcurrent.getType() == activeSkillEffect.getMaterial()) {
						if (playerdata.getActiveskilldata().effectnum == activeSkillEffect.getNum()) {
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
							player.sendMessage(ChatColor.YELLOW + "既に選択されています");
						} else {
							playerdata.getActiveskilldata().effectnum = activeSkillEffect.getNum();
							player.sendMessage(ChatColor.GREEN + "エフェクト:" + activeSkillEffect.getName() + ChatColor.RESET + "" + ChatColor.GREEN + " が選択されました");
							player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
						}
					}
				}
				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
				for (final ActiveSkillPremiumEffect activeSkillPremiumEffect : premiumeffect) {
					if (itemstackcurrent.getType() == activeSkillPremiumEffect.getMaterial()) {
						if (playerdata.getActiveskilldata().effectnum == activeSkillPremiumEffect.getNum()) {
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
							player.sendMessage(ChatColor.YELLOW + "既に選択されています");
						} else {
							playerdata.getActiveskilldata().effectnum = activeSkillPremiumEffect.getNum() + 100;
							player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "プレミアムエフェクト:" + activeSkillPremiumEffect.getName() + ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.BOLD + " が選択されました");
							player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
						}
					}
				}
			}


			//ここからエフェクト開放の処理
			if(itemstackcurrent.getType() == Material.BEDROCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
				for (final ActiveSkillEffect activeSkillEffect : skilleffect) {
					if (itemmeta.getDisplayName().contains(activeSkillEffect.getName())) {
						if (playerdata.getActiveskilldata().effectpoint < activeSkillEffect.getUsePoint()) {
							player.sendMessage(ChatColor.DARK_RED + "エフェクトポイントが足りません");
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.5);
						} else {
							playerdata.getActiveskilldata().obtainedSkillEffects.add(activeSkillEffect);
							player.sendMessage(ChatColor.LIGHT_PURPLE + "エフェクト：" + activeSkillEffect.getName() + ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "" + " を解除しました");
							player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 1.2);
							playerdata.getActiveskilldata().effectpoint -= activeSkillEffect.getUsePoint();
							player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player));
						}
					}
				}
			}
			//ここからプレミアムエフェクト開放の処理
			if(itemstackcurrent.getType() == Material.BEDROCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
				for (final ActiveSkillPremiumEffect activeSkillPremiumEffect : premiumeffect) {
					if (itemmeta.getDisplayName().contains(activeSkillPremiumEffect.getName())) {
						if (playerdata.getActiveskilldata().premiumeffectpoint < activeSkillPremiumEffect.getUsePoint()) {
							player.sendMessage(ChatColor.DARK_RED + "プレミアムエフェクトポイントが足りません");
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.5);
						} else {
							playerdata.getActiveskilldata().obtainedSkillPremiumEffects.add(activeSkillPremiumEffect);
							player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "プレミアムエフェクト：" + activeSkillPremiumEffect.getName() + ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "" + " を解除しました");
							if (databaseGateway.donateDataManipulator.addPremiumEffectBuy(playerdata, activeSkillPremiumEffect) == Fail) {
								player.sendMessage("購入履歴が正しく記録されませんでした。管理者に報告してください。");
							}
							player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 1.2);
							playerdata.getActiveskilldata().premiumeffectpoint -= activeSkillPremiumEffect.getUsePoint();
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(itemstackcurrent.getType() == Material.BEDROCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				int skilllevel;
				int skilltype;
				if(itemmeta.getDisplayName().contains("エビフライ・ドライブ")){
					skilllevel = 4;
					skilltype = 1;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float)0.5);
					}else if(playerdata.getActiveskilldata().breakskill < 3){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(3,3) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float)0.5);
					}else{
						playerdata.getActiveskilldata().arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ホーリー・ショット")){
					skilllevel = 5;
					skilltype = 1;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ツァーリ・ボンバ")){
					skilllevel = 6;
					skilltype = 1;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アーク・ブラスト")){
					skilllevel = 7;
					skilltype = 1;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ファンタズム・レイ")){
					skilllevel = 8;
					skilltype = 1;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("スーパー・ノヴァ")){
					skilllevel = 9;
					skilltype = 1;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().arrowskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().arrowskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						if(playerdata.getActiveskilldata().multiskill == 9 && playerdata.getActiveskilldata().breakskill == 9 && playerdata.getActiveskilldata().watercondenskill == 9 && playerdata.getActiveskilldata().lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.getName() + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("トム・ボウイ")){
					skilllevel = 4;
					skilltype = 2;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < 3){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(3,3) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("サンダー・ストーム")){
					skilllevel = 5;
					skilltype = 2;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("スターライト・ブレイカー")){
					skilllevel = 6;
					skilltype = 2;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アース・ディバイド")){
					skilllevel = 7;
					skilltype = 2;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ヘヴン・ゲイボルグ")){
					skilllevel = 8;
					skilltype = 2;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ディシジョン")){
					skilllevel = 9;
					skilltype = 2;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().multiskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().multiskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						if(playerdata.getActiveskilldata().arrowskill == 9 && playerdata.getActiveskilldata().breakskill == 9 && playerdata.getActiveskilldata().watercondenskill == 9 && playerdata.getActiveskilldata().lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.getName() + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("デュアル・ブレイク")){
					skilllevel = 1;
					skilltype = 3;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("トリアル・ブレイク")){
					skilllevel = 2;
					skilltype = 3;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("エクスプロージョン")){
					skilllevel = 3;
					skilltype = 3;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ミラージュ・フレア")){
					skilllevel = 4;
					skilltype = 3;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ドッ・カーン")){
					skilllevel = 5;
					skilltype = 3;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ギガンティック・ボム")){
					skilllevel = 6;
					skilltype = 3;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ブリリアント・デトネーション")){
					skilllevel = 7;
					skilltype = 3;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("レムリア・インパクト")){
					skilllevel = 8;
					skilltype = 3;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("エターナル・ヴァイス")){
					skilllevel = 9;
					skilltype = 3;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().breakskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						if(playerdata.getActiveskilldata().arrowskill == 9 && playerdata.getActiveskilldata().multiskill == 9 && playerdata.getActiveskilldata().watercondenskill == 9 && playerdata.getActiveskilldata().lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.getName() + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ホワイト・ブレス")){
					skilllevel = 7;
					skilltype = 4;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().breakskill < 3){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(3,3) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().watercondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アブソリュート・ゼロ")){
					skilllevel = 8;
					skilltype = 4;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().watercondenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().watercondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ダイアモンド・ダスト")){
					skilllevel = 9;
					skilltype = 4;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().watercondenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().watercondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						if(playerdata.getActiveskilldata().arrowskill == 9 && playerdata.getActiveskilldata().multiskill == 9 && playerdata.getActiveskilldata().watercondenskill == 9 && playerdata.getActiveskilldata().lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.getName() + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("ラヴァ・コンデンセーション")){
					skilllevel = 7;
					skilltype = 5;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}
					/*else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}*/
					else if(playerdata.getActiveskilldata().breakskill < 3){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(3,3) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().lavacondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("モエラキ・ボールダーズ")){
					skilllevel = 8;
					skilltype = 5;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().lavacondenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().lavacondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("エルト・フェットル")){
					skilllevel = 9;
					skilltype = 5;
					if(playerdata.getActiveskilldata().skillpoint < skilllevel * 10){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else if(playerdata.getActiveskilldata().lavacondenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().lavacondenskill = skilllevel;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype ,skilllevel) + "を解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
						if(playerdata.getActiveskilldata().arrowskill == 9 && playerdata.getActiveskilldata().multiskill == 9 && playerdata.getActiveskilldata().watercondenskill == 9 && playerdata.getActiveskilldata().lavacondenskill == 9){
							player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました");
							Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
							Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.getName() + "が全てのスキルを習得し、アサルトアーマーを解除しました！");
						}
						player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player));
					}
				}else if(itemmeta.getDisplayName().contains("アサルト・アーマー")){

				}else if(itemmeta.getDisplayName().contains("ヴェンダー・ブリザード")){
					if(playerdata.getActiveskilldata().skillpoint < 110){
						player.sendMessage(ChatColor.DARK_RED  + "アクティブスキルポイントが足りません");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}else{
						playerdata.getActiveskilldata().fluidcondenskill = 10;
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + "ヴェンダー・ブリザードを解除しました");
						player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
						playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
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
		if(he.getType() != EntityType.PLAYER){
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
			final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			if(SeichiAssist.Companion.getDEBUG()){
				player.sendMessage("MineStackSize = " + MineStackObjectList.INSTANCE.getMinestacklist().size());
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}

			//ページ変更処理
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}

			if (itemstackcurrent.getType() == Material.DIAMOND_ORE && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 0));
				return;
			}

			if (itemstackcurrent.getType() == Material.ENDER_PEARL && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 1));
				return;
			}

			if (itemstackcurrent.getType() == Material.SEEDS && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 2));
				return;
			}

			if (itemstackcurrent.getType() == Material.SMOOTH_BRICK && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 3));
				return;
			}

			if (itemstackcurrent.getType() == Material.REDSTONE && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 4));
				return;
			}

			if (itemstackcurrent.getType() == Material.GOLDEN_APPLE && !itemstackcurrent.getItemMeta().hasLore()) {
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player, 0, 5));
				return;
			}

			if (itemstackcurrent.getType() == Material.COMPASS) {
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

			if (itemstackcurrent.getType() == Material.IRON_PICKAXE) {
				playerdata.setMinestackflag(!playerdata.getMinestackflag());
				if (playerdata.getMinestackflag()) {
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "対象ブロック自動スタック機能:ON");
				} else {
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "対象ブロック自動スタック機能:OFF");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.MineStackToggleMeta(playerdata,itemmeta));
			}

			for (HistoryData data : playerdata.getHisotryData().getHistoryList()) {
				if (itemstackcurrent.getType() == data.obj.getMaterial()
						&& itemstackcurrent.getDurability() == data.obj.getDurability()) { //MaterialとサブIDが一致

					if (!data.obj.getNameLoreFlag()) {
						/* loreが無いとき */

						//同じ名前の別アイテムに対応するためにインベントリの「解放レベル」を見る
						//このアイテムの解放レベル
						int level = SeichiAssist.Companion.getSeichiAssistConfig().getMineStacklevel(data.obj.getLevel());
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

							final MineStackObj mineStackObj = data.obj;
							final long mineStackObjAmount = playerdata.getMinestack().getStackedAmountOf(mineStackObj);
							itemstack_name = itemstack_name.replaceAll("§[0-9A-Za-z]","");
							minestack_name = minestack_name.replaceAll("§[0-9A-Za-z]","");
							if (itemstack_name.equals(minestack_name)) { //表記はアイテム名だけなのでアイテム名で判定
								final long withdrawnAmount = giveItemStackAndPlayMineStackSound(player, mineStackObjAmount, new ItemStack(data.obj.getMaterial(), 1, (short)data.obj.getDurability()));
								playerdata.getMinestack().subtractStackedAmountOf(mineStackObj, withdrawnAmount);
							}
						}
					} else if (data.obj.getNameLoreFlag() && itemstackcurrent.getItemMeta().hasDisplayName()) { //名前と説明文がある
						//System.out.println("debug AA");
						//同じ名前の別アイテムに対応するためにインベントリの「解放レベル」を見る
						int level = SeichiAssist.Companion.getSeichiAssistConfig().getMineStacklevel(data.obj.getLevel());
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
						if(level==level_){
							String itemstack_name = itemstackcurrent.getItemMeta().getDisplayName();
							String minestack_name = data.obj.getJapaneseName();
							itemstack_name = itemstack_name.replaceAll("§[0-9A-Za-z]","");
							minestack_name = minestack_name.replaceAll("§[0-9A-Za-z]","");

							if (data.obj.getGachaType() == -1) {//ガチャアイテムにはない（がちゃりんご）
								if (itemstack_name.equals(minestack_name)) { //表記はアイテム名だけなのでアイテム名で判定
									final ItemStack itemStackToGive = new ItemStack(data.obj.getMaterial(), 1, (short)data.obj.getDurability());
									final int withdrawnAmount = giveItemStackWithNameLoreAndPlayMineStackSound(player, playerdata.getMinestack().getStackedAmountOf(data.obj), itemStackToGive, -1);

									playerdata.getMinestack().subtractStackedAmountOf(data.obj, withdrawnAmount);
								}
							} else { //ガチャアイテム(処理は同じでも念のためデバッグ用に分離)
								if (data.obj.getGachaType()>=0) {
									if (itemstack_name.equals(minestack_name)) { //表記はアイテム名だけなのでアイテム名で判定
										//盾、バナーの模様判定
										if ((itemstackcurrent.getType() == Material.SHIELD || (itemstackcurrent.getType() == Material.BANNER) ) && data.obj.getItemStack().getType() == itemstackcurrent.getType()){
											BlockStateMeta bs0 = (BlockStateMeta) itemstackcurrent.getItemMeta();
											Banner b0 = (Banner) bs0.getBlockState();
											List<org.bukkit.block.banner.Pattern> p0 = b0.getPatterns();

											BlockStateMeta bs1 = (BlockStateMeta) data.obj.getItemStack().getItemMeta();
											Banner b1 = (Banner) bs1.getBlockState();
											List<org.bukkit.block.banner.Pattern> p1 = b1.getPatterns();

											if (p0.containsAll(p1)) {
												final long currentObjectAmount = playerdata.getMinestack().getStackedAmountOf(data.obj);
												final int withdrawnAmount = giveItemStackWithNameLoreAndPlayMineStackSound(player, currentObjectAmount, new ItemStack(data.obj.getMaterial(), 1, (short)data.obj.getDurability()), data.obj.getGachaType());
												playerdata.getMinestack().subtractStackedAmountOf(data.obj, withdrawnAmount);
											}
										} else {
											final long currentObjectAmount = playerdata.getMinestack().getStackedAmountOf(data.obj);
											final int withdrawnAmount = giveItemStackWithNameLoreAndPlayMineStackSound(player, currentObjectAmount, new ItemStack(data.obj.getMaterial(), 1, (short)data.obj.getDurability()), data.obj.getGachaType());
											playerdata.getMinestack().subtractStackedAmountOf(data.obj, withdrawnAmount);
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
		if(he.getType() != EntityType.PLAYER){
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
		final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;
		//if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack")){
		if (!topinventory.getTitle().contains("メインメニュー") && topinventory.getTitle().contains("MineStack")) {
			/* メインメニュー以外の各種MineStackメニューの際の処理 */
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
				return;
			}

			if(SeichiAssist.Companion.getDEBUG()){
				player.sendMessage("MineStackSize = " + MineStackObjectList.INSTANCE.getMinestacklist().size());
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if (isSkull) {
				SkullMeta skullMeta = (SkullMeta) itemstackcurrent.getItemMeta();
				if (skullMeta.hasOwner()) {
					switch (skullMeta.getOwningPlayer().getUniqueId().toString()) {
						// left
						case "a68f0b64-8d14-4000-a95f-4b9ba14f8df9": {
							player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
							player.openInventory(MenuInventoryData.getMineStackMainMenu(player));
							return;
						}

						// down
						case "68f59b9b-5b0b-4b05-a9f2-e1d1405aa348": {
							/* ArrowDownならば、次ページ移行処理 */
							ItemMeta itemmeta = itemstackcurrent.getItemMeta();
							MineStackMenuTransfer(topinventory, player, itemmeta);
							return;
						}

						// up
						case "fef039ef-e6cd-4987-9c84-26a3e6134277": {
							/* ArrowUpならば、前ページ移行処理 */
							ItemMeta itemmeta = itemstackcurrent.getItemMeta();
							MineStackMenuTransfer(topinventory, player, itemmeta);
							return;
						}
					}
				}
			}

			if (itemstackcurrent.getType() == Material.IRON_PICKAXE) {
				// 対象ブロック自動スタック機能トグル(どのメニューでも)
				playerdata.setMinestackflag(!playerdata.getMinestackflag());
				if (playerdata.getMinestackflag()) {
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.sendMessage(ChatColor.GREEN + "対象ブロック自動スタック機能:ON");
				} else {
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					player.sendMessage(ChatColor.RED + "対象ブロック自動スタック機能:OFF");
				}
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(MenuInventoryData.MineStackToggleMeta(playerdata,itemmeta));
			} else {
				for (int i = 0; i < MineStackObjectList.INSTANCE.getMinestacklist().size(); i++) {
					final MineStackObj mineStackObj = MineStackObjectList.INSTANCE.getMinestacklist().get(i);
					if (itemstackcurrent.getType() == mineStackObj.getMaterial()
							&& itemstackcurrent.getDurability() == mineStackObj.getDurability()) { //MaterialとサブIDが一致

						if (!mineStackObj.getNameLoreFlag()) {
							/* loreが無いとき */

							//同じ名前の別アイテムに対応するためにインベントリの「解放レベル」を見る
							//このアイテムの解放レベル
							int level = SeichiAssist.Companion.getSeichiAssistConfig().getMineStacklevel(mineStackObj.getLevel());
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
								String minestack_name = mineStackObj.getJapaneseName();
								itemstack_name = itemstack_name.replaceAll("§[0-9A-Za-z]","");
								minestack_name = minestack_name.replaceAll("§[0-9A-Za-z]","");
								if (itemstack_name.equals(minestack_name)) { //表記はアイテム名だけなのでアイテム名で判定
									final ItemStack itemStackToGive = new ItemStack(mineStackObj.getMaterial(), 1, (short)mineStackObj.getDurability());
									final int withdrawnAmount = giveItemStackAndPlayMineStackSound(
											player, playerdata.getMinestack().getStackedAmountOf(mineStackObj), itemStackToGive);

									playerdata.getMinestack().subtractStackedAmountOf(mineStackObj, withdrawnAmount);

									open_flag = (Util.getMineStackTypeindex(i) + 1) / 45;
									open_flag_type = mineStackObj.getStackType();
								}
							}
						} else if (mineStackObj.getNameLoreFlag() && itemstackcurrent.getItemMeta().hasDisplayName()) { //名前と説明文がある
							//System.out.println("debug AA");
							//同じ名前の別アイテムに対応するためにインベントリの「解放レベル」を見る
							int level = SeichiAssist.Companion.getSeichiAssistConfig().getMineStacklevel(mineStackObj.getLevel());
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
							if(level==level_){
								String itemstack_name = itemstackcurrent.getItemMeta().getDisplayName();
								String minestack_name = mineStackObj.getJapaneseName();
								itemstack_name = itemstack_name.replaceAll("§[0-9A-Za-z]","");
								minestack_name = minestack_name.replaceAll("§[0-9A-Za-z]","");

								if(mineStackObj.getGachaType()==-1){//ガチャアイテムにはない（がちゃりんご）
									if(itemstack_name.equals(minestack_name)){ //表記はアイテム名だけなのでアイテム名で判定
										final long currentObjectAmount = playerdata.getMinestack().getStackedAmountOf(mineStackObj);
										final int withdrawnAmount = giveItemStackWithNameLoreAndPlayMineStackSound(player, currentObjectAmount, new ItemStack(mineStackObj.getMaterial(), 1, (short)mineStackObj.getDurability()),-1);
										playerdata.getMinestack().subtractStackedAmountOf(mineStackObj, withdrawnAmount);
										open_flag = (Util.getMineStackTypeindex(i)+1)/45;
										open_flag_type = mineStackObj.getStackType();
									}
								} else { //ガチャアイテム(処理は同じでも念のためデバッグ用に分離)
									if(mineStackObj.getGachaType()>=0){
										if(itemstack_name.equals(minestack_name)){ //表記はアイテム名だけなのでアイテム名で判定
											//盾、バナーの模様判定
											if( (itemstackcurrent.getType() == Material.SHIELD || (itemstackcurrent.getType() == Material.BANNER) ) && mineStackObj.getItemStack().getType() == itemstackcurrent.getType()){
												BlockStateMeta bs0 = (BlockStateMeta) itemstackcurrent.getItemMeta();
												Banner b0 = (Banner) bs0.getBlockState();
												List<org.bukkit.block.banner.Pattern> p0 = b0.getPatterns();

												BlockStateMeta bs1 = (BlockStateMeta) mineStackObj.getItemStack().getItemMeta();
												Banner b1 = (Banner) bs1.getBlockState();
												List<org.bukkit.block.banner.Pattern> p1 = b1.getPatterns();

												if(p0.containsAll(p1)){
													final long currentObjectAmount = playerdata.getMinestack().getStackedAmountOf(mineStackObj);
													final int withdrawnAmount = giveItemStackWithNameLoreAndPlayMineStackSound(player, currentObjectAmount, new ItemStack(mineStackObj.getMaterial(), 1, (short)mineStackObj.getDurability()),mineStackObj.getGachaType());
													playerdata.getMinestack().subtractStackedAmountOf(mineStackObj,withdrawnAmount);
													open_flag = (Util.getMineStackTypeindex(i)+1)/45;
													open_flag_type=mineStackObj.getStackType();
												}
											} else {
												final long currentObjectAmount = playerdata.getMinestack().getStackedAmountOf(mineStackObj);
												final int withdrawnAmount = giveItemStackWithNameLoreAndPlayMineStackSound(player, currentObjectAmount, new ItemStack(mineStackObj.getMaterial(), 1, (short)mineStackObj.getDurability()),mineStackObj.getGachaType());
												playerdata.getMinestack().subtractStackedAmountOf(mineStackObj,withdrawnAmount);
												open_flag = (Util.getMineStackTypeindex(i)+1)/45;
												open_flag_type=mineStackObj.getStackType();
											}
										}
									}
								}
							}
						}
						if (mineStackObj.getGachaType() == -1) {
							playerdata.getHisotryData().add(i, mineStackObj);
						}
					}

				}
			}

			if (open_flag != -1) {
				player.openInventory(MenuInventoryData.getMineStackMenu(player, open_flag, open_flag_type));
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
			if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "鉱石系MineStack")){
				player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display - 1,0));
			} else if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "ドロップ系MineStack")){
				player.openInventory(MenuInventoryData.getMineStackMenu(player, page_display - 1 ,1));
			} else if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "農業・食料系MineStack")){
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
		if(he.getType() != EntityType.PLAYER){
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

		final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;
		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地神ランキング")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
			}
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("整地神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList(player, page_display-1));
				}
			}
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
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
		if(he.getType() != EntityType.PLAYER){
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

		final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;
		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ログイン神ランキング")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
			}
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("ログイン神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_playtick(player, page_display-1));
				}
			}
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
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
		if(he.getType() != EntityType.PLAYER){
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

		final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;
		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票神ランキング")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
			}
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("投票神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_p_vote(player, page_display-1));
				}
			}
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
			}
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown")){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				if(itemmeta.getDisplayName().contains("寄付神ランキング") &&
						itemmeta.getDisplayName().contains("ページ目") ){//移動するページの種類を判定
					int page_display = Integer.parseInt(itemmeta.getDisplayName().replaceAll("[^0-9]","")); //数字以外を全て消す

					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
					player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(player, page_display-1));
				}
			}
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp")){
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			//ページ変更処理
			if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player));
			}
		}
	}

	/**
	 * 指定されたアイテムスタックをプレーヤーに与え、音を鳴らす
	 *
	 * @return 実際に与えたアイテム数
 	 */
	private int giveItemStackAndPlayMineStackSound(final Player player,
													final long requestedAmount,
													final ItemStack itemstack) {
		final int maximumItemStackSize = itemstack.getMaxStackSize();
		final int grantAmount = (int)Math.min(maximumItemStackSize, requestedAmount);

		itemstack.setAmount(grantAmount);

		if(!Util.isPlayerInventoryFull(player)){
			Util.addItem(player,itemstack);
		}else{
			Util.dropItem(player,itemstack);
		}

		final Sound soundTypeToPlay = Sound.BLOCK_STONE_BUTTON_CLICK_ON;
		final float soundPitch = requestedAmount >= maximumItemStackSize ? 1.0f : 0.5f;

		player.playSound(player.getLocation(), soundTypeToPlay, 1, soundPitch);

		return grantAmount;
	}

	/**
	 * 指定されたアイテムスタックをプレーヤーに与え、音を鳴らす
	 * 名前、説明文付き専用
	 * @return 実際に与えたアイテム数
	 */
	private int giveItemStackWithNameLoreAndPlayMineStackSound(Player player,
															   long requestedAmount,
															   ItemStack itemstack,
															   int num){
		if (num == -1) {//がちゃりんごの場合
			itemstack = new ItemStack(Material.GOLDEN_APPLE,1);
			ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);

			meta.setDisplayName(StaticGachaPrizeFactory.getGachaRingoName());
			List<String> lore = StaticGachaPrizeFactory.getGachaRingoLore();
			meta.setLore(lore);
			itemstack.setItemMeta(meta);

			meta.setDisplayName(StaticGachaPrizeFactory.getGachaRingoName());
			meta.setLore(StaticGachaPrizeFactory.getGachaRingoLore());
		} else if (num>=0) { //他のガチャアイテムの場合 -2以下は他のアイテムに対応させる
			MineStackGachaData g = SeichiAssist.Companion.getMsgachadatalist().get(num).copy();
			UUID uuid = player.getUniqueId();
			PlayerData playerdata = playermap.get(uuid);
			String name = playerdata.getName();
			if(g.getProbability() < 0.1){ //ガチャアイテムに名前を付与
				g.appendOwnerLore(name);
			}
			itemstack = new ItemStack(g.getItemStack()); //この1行だけで問題なく動くのかテスト
		}

		return giveItemStackAndPlayMineStackSound(player, requestedAmount, itemstack);
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
		String name = playerdata.getName();
		Inventory inventory = event.getInventory();

		//インベントリサイズが36でない時終了
		if(inventory.getSize() != 36){
			return;
		}
		if(inventory.getTitle().equals(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "交換したい景品を入れてください")){
			int givegacha = 0;
			/*
			 * step1 for文でinventory内に対象商品がないか検索
			 * あったらdurabilityに応じてgivegachaを増やし、非対象商品は返却boxへ
			 */
			//ガチャ景品交換インベントリの中身を取得
			ItemStack[] item = inventory.getContents();
			//ドロップ用アイテムリスト(返却box)作成
			List<ItemStack> dropitem = new ArrayList<>();
			//カウント用
			int big = 0;
			int reg = 0;
			//for文で１個ずつ対象アイテムか見る
			//ガチャ景品交換インベントリを一個ずつ見ていくfor文
			for (ItemStack m : item) {
				//無いなら次へ
				if(m == null){
					continue;
				}else if(SeichiAssist.Companion.getGachamente()){
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
				}else if(m.getType() == Material.SKULL_ITEM){
					//丁重にお返しする
					dropitem.add(m);
					continue;
				}
				//ガチャ景品リストにアイテムがあった時にtrueになるフラグ
				boolean flag = false;
				//ガチャ景品リストを一個ずつ見ていくfor文
				for(GachaPrize gachadata : gachadatalist){
					if(!gachadata.getItemStack().hasItemMeta()){
						continue;
					}else if(!gachadata.getItemStack().getItemMeta().hasLore()){
						continue;
					}
					//ガチャ景品リストにある商品の場合(Lore=説明文と表示名で判別),無い場合はアイテム返却
					if(gachadata.compare(m,name)){
						if(SeichiAssist.Companion.getDEBUG()){
							player.sendMessage(gachadata.getItemStack().getItemMeta().getDisplayName());
						}
						flag = true;
						int amount = m.getAmount();
						if(gachadata.getProbability() < 0.001){
							//ギガンティック大当たりの部分
							//ガチャ券に交換せずそのままアイテムを返す
							dropitem.add(m);
						}else if(gachadata.getProbability() < 0.01){
							//大当たりの部分
							givegacha += (12*amount);
							big++;
						}else if(gachadata.getProbability() < 0.1){
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
			if(SeichiAssist.Companion.getGachamente()){
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
				if(!Util.isPlayerInventoryFull(player)){
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
				if(player.getInventory().contains(skull) || !Util.isPlayerInventoryFull(player)){
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
		if(he.getType() != EntityType.PLAYER){
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

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;

			//表示内容をLVに変更
			if(itemstackcurrent.getType() == Material.REDSTONE_TORCH_ON){
				playerdata.setDisplayTitle1No(0);
				playerdata.setDisplayTitle2No(0);
				playerdata.setDisplayTitle3No(0);
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
			}

			//予約付与システム受け取り処理
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_Present2")){
				SeichiAchievement.tryAchieve(player, playerdata.getGiveachvNo());
				playerdata.setGiveachvNo(0);
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
			}

			//「二つ名組合せシステム」を開く
			else if(itemstackcurrent.getType() == Material.ANVIL){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
			}

			//カテゴリ「整地」を開く
			else if(itemstackcurrent.getType() == Material.GOLD_PICKAXE){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleSeichi(player));
			}

			/*
			//カテゴリ「建築」を開く ※未実装
			else if(itemstackcurrent.getType().equals(Material.WOODEN_DOOR)){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.titlepage = 1 ;
				player.openInventory(MenuInventoryData.getTitleBuild(player));
			}
			*/

			//カテゴリ「ログイン」を開く
			else if(itemstackcurrent.getType() == Material.COMPASS){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleLogin(player));
			}

			//カテゴリ「やりこみ」を開く
			else if(itemstackcurrent.getType() == Material.BLAZE_POWDER){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleSuperTry(player));
			}

			//カテゴリ「特殊」を開く
			else if(itemstackcurrent.getType() == Material.EYE_OF_ENDER){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleSpecial(player));
			}

			//ホームメニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}
		}

		final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;
		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「整地」")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			//クリックしたボタンに応じた各処理内容の記述ここから

			//実績「整地量」
			if(itemstackcurrent.getType() == Material.IRON_PICKAXE){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleAmountData(player));
			}

			//実績「整地神ランキング」
			if(itemstackcurrent.getType() == Material.DIAMOND_PICKAXE){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleRankData(player));
			}

			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}

		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「建築」")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			//クリックしたボタンに応じた各処理内容の記述ここから

			//実績未実装のカテゴリです。

			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}

		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「ログイン」")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			//クリックしたボタンに応じた各処理内容の記述ここから

			//実績「参加時間」を開く
			else if(itemstackcurrent.getType() == Material.COMPASS){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleTimeData(player));
			}

			//実績「通算ログイン」を開く
			else if(itemstackcurrent.getType() == Material.BOOK){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleJoinAmountData(player));
			}

			//実績「連続ログイン」を開く
			else if(itemstackcurrent.getType() == Material.BOOK_AND_QUILL){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleJoinChainData(player));
			}

			//実績「記念日」を開く
			else if(itemstackcurrent.getType() == Material.NETHER_STAR){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleExtraData(player));
			}

			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}

		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「やりこみ」")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			//クリックしたボタンに応じた各処理内容の記述ここから

			//実績未実装のカテゴリです。

			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}

		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「特殊」")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			//クリックしたボタンに応じた各処理内容の記述ここから

			//実績「公式イベント」を開く
			else if(itemstackcurrent.getType() == Material.BLAZE_POWDER){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleEventData(player));
			}

			//実績「JMS投票数」を開く
			else if(itemstackcurrent.getType() == Material.YELLOW_FLOWER){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleSupportData(player));
			}

			//実績「極秘任務」を開く
			else if(itemstackcurrent.getType() == Material.DIAMOND_BARDING){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(1);
				player.openInventory(MenuInventoryData.getTitleSecretData(player));
			}

			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleMenuData(player));
				return;
			}

		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せシステム")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			//実績ポイント最新化
			if(itemstackcurrent.getType() == Material.EMERALD_ORE){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.setAchvPointMAX(0);
				for(int i=1000 ; i < 9800; i ++ ){
					if(playerdata.getTitleFlags().get(i)){
						playerdata.setAchvPointMAX(playerdata.getAchvPointMAX() + 10);
					}
				}
				playerdata.setAchvPoint((playerdata.getAchvPointMAX() + (playerdata.getAchvChangenum() * 3)) - playerdata.getAchvPointUSE());
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
			}

			//エフェクトポイント→実績ポイント変換
			if(itemstackcurrent.getType() == Material.EMERALD){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				//不足してたらスルー
				if(playerdata.getActiveskilldata().effectpoint < 10){
					player.sendMessage("エフェクトポイントが不足しています。");
				}else {
					playerdata.setAchvChangenum(playerdata.getAchvChangenum() + 1);
					playerdata.getActiveskilldata().effectpoint -= 10 ;
				}
				//データ最新化
				playerdata.setAchvPointMAX(0);
				for(int i=1000 ; i < 9800; i ++ ){
					if(playerdata.getTitleFlags().get(i)){
						playerdata.setAchvPointMAX(playerdata.getAchvPointMAX() + 10);
					}
				}
				playerdata.setAchvPoint((playerdata.getAchvPointMAX() + (playerdata.getAchvChangenum() * 3)) - playerdata.getAchvPointUSE());

				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));


			}

			//パーツショップ
			if(itemstackcurrent.getType() == Material.ITEM_FRAME){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setTitleShopData(player));
			}

			//前パーツ
			if(itemstackcurrent.getType() == Material.WATER_BUCKET){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitle1Data(player));
			}

			//中パーツ
			if(itemstackcurrent.getType() == Material.MILK_BUCKET){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitle2Data(player));
			}

			//後パーツ
			if(itemstackcurrent.getType() == Material.LAVA_BUCKET){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitle3Data(player));
			}

			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}


			else if (itemstackcurrent.getType() == Material.WATER_BUCKET){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

				String forcheck = SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(Integer.parseInt(itemmeta.getDisplayName()))
									+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(playerdata.getDisplayTitle2No())
									+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(playerdata.getDisplayTitle3No());
				if(forcheck.length() < 9){
					playerdata.setDisplayTitle1No(Integer.parseInt(itemmeta.getDisplayName()));
					player.sendMessage("前パーツ「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(playerdata.getDisplayTitle1No()) +"」をセットしました。");
				}else {
					player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。");
				}
			}

			//パーツ未選択に
			else if(itemstackcurrent.getType() == Material.GRASS){
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.setDisplayTitle1No(0);
				player.sendMessage("前パーツの選択を解除しました。");
				return;
			}

			//組み合わせメイン
			else if(itemstackcurrent.getType() == Material.BARRIER){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
				return;
			}

			//次ページ
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}


			else if (itemstackcurrent.getType() == Material.MILK_BUCKET){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

				String forcheck = SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(playerdata.getDisplayTitle1No())
									+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(Integer.parseInt(itemmeta.getDisplayName()))
									+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(playerdata.getDisplayTitle3No());
				if(forcheck.length() < 9){
					playerdata.setDisplayTitle2No(Integer.parseInt(itemmeta.getDisplayName()));
					player.sendMessage("中パーツ「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(playerdata.getDisplayTitle2No()) +"」をセットしました。");
				}else {
					player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。");
				}
			}

			//パーツ未選択に
			else if(itemstackcurrent.getType() == Material.GRASS){
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.setDisplayTitle2No(0);
				player.sendMessage("中パーツの選択を解除しました。");
				return;
			}

			//組み合わせメインへ移動
			else if(itemstackcurrent.getType() == Material.BARRIER){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
				return;
			}

			//次ページ
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}


			else if (itemstackcurrent.getType() == Material.LAVA_BUCKET){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

				String forcheck = SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(playerdata.getDisplayTitle1No())
									+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(playerdata.getDisplayTitle2No())
									+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(Integer.parseInt(itemmeta.getDisplayName()));
				if(forcheck.length() < 9){
					playerdata.setDisplayTitle3No(Integer.parseInt(itemmeta.getDisplayName()));
					player.sendMessage("後パーツ「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(playerdata.getDisplayTitle3No()) +"」をセットしました。");
				}else {
					player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。");
				}
			}

			//パーツ未選択に
			else if(itemstackcurrent.getType() == Material.GRASS){
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.setDisplayTitle3No(0);
				player.sendMessage("後パーツの選択を解除しました。");
				return;
			}

			//組み合わせメイン
			else if(itemstackcurrent.getType() == Material.BARRIER){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
				return;
			}

			//次ページ
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			//実績ポイント最新化
			if(itemstackcurrent.getType() == Material.EMERALD_ORE){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.setAchvPointMAX(0);
				for(int i=1000 ; i < 9800; i ++ ){
					if(playerdata.getTitleFlags().get(i)){
						playerdata.setAchvPointMAX(playerdata.getAchvPointMAX() + 10);
					}
				}
				playerdata.setAchvPoint((playerdata.getAchvPointMAX() + (playerdata.getAchvChangenum() * 3)) - playerdata.getAchvPointUSE());
				playerdata.setSamepageflag(true);
				player.openInventory(MenuInventoryData.setTitleShopData(player));
			}

			//購入処理
			if(itemstackcurrent.getType() == Material.BEDROCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);

				if(Integer.parseInt(itemmeta.getDisplayName()) < 9900 ){
					if(playerdata.getAchvPoint() < 20){
						player.sendMessage("実績ポイントが不足しています。");
					}else {
						playerdata.getTitleFlags().set(Integer.parseInt(itemmeta.getDisplayName()));
						playerdata.setAchvPoint(playerdata.getAchvPoint() - 20);
						playerdata.setAchvPointUSE(playerdata.getAchvPointUSE() + 20);
						player.sendMessage("パーツ「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(Integer.parseInt(itemmeta.getDisplayName())) + "」を購入しました。");
						playerdata.setSamepageflag(true);
						player.openInventory(MenuInventoryData.setTitleShopData(player));
					}
				}else {
					if(playerdata.getAchvPoint() < 35){
						player.sendMessage("実績ポイントが不足しています。");
					}else {
						playerdata.getTitleFlags().set(Integer.parseInt(itemmeta.getDisplayName()));
						playerdata.setAchvPoint(playerdata.getAchvPoint() - 35);
						playerdata.setAchvPointUSE(playerdata.getAchvPointUSE() + 35);
						player.sendMessage("パーツ「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(Integer.parseInt(itemmeta.getDisplayName())) + "」を購入しました。");
						playerdata.setSamepageflag(true);
						player.openInventory(MenuInventoryData.setTitleShopData(player));
					}
				}


			}


			//組み合わせメイン
			else if(itemstackcurrent.getType() == Material.BARRIER){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.setFreeTitleMainData(player));
				return;
			}

			//次ページ
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType() == Material.BEDROCK){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
				player.openInventory(MenuInventoryData.getTitleRankData(player));
			}
			else if (itemstackcurrent.getType() == Material.DIAMOND_BLOCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No1001「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1001) +"」")){
					playerdata.setDisplayTitle1No(1001);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1001) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1002「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1002) +"」")){
					playerdata.setDisplayTitle1No(1002);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1002) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1003「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1003) +"」")){
					playerdata.setDisplayTitle1No(1003);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1003) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1004「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1004) +"」")){
					playerdata.setDisplayTitle1No(1004);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1004) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1010「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1010) +"」")){
					playerdata.setDisplayTitle1No(1010);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1010) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1011「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1011)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9904) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1011) +"」")){
					playerdata.setDisplayTitle1No(1011);
					playerdata.setDisplayTitle2No(9904);
					playerdata.setDisplayTitle3No(1011);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1011)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9904) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1011) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1012「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1012)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1012) +"」")){
					playerdata.setDisplayTitle1No(1012);
					playerdata.setDisplayTitle2No(9901);
					playerdata.setDisplayTitle3No(1012);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1012)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1012) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1005「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1005)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1005) + "」")){
					playerdata.setDisplayTitle1No(1005);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(1005);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1005)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1005) + "」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1006「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1006) +"」")){
					playerdata.setDisplayTitle1No(1006);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1006) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1007「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1007)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9904) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1007) +"」")){
					playerdata.setDisplayTitle1No(1007);
					playerdata.setDisplayTitle2No(9904);
					playerdata.setDisplayTitle3No(1007);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1007)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9904) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1007) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1008「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1008)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1008) +"」")){
					playerdata.setDisplayTitle1No(1008);
					playerdata.setDisplayTitle2No(9901);
					playerdata.setDisplayTitle3No(1008);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1008)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1008) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No1009「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1009)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9909) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1009) +"」")){
					playerdata.setDisplayTitle1No(1009);
					playerdata.setDisplayTitle2No(9909);
					playerdata.setDisplayTitle3No(1009);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(1009)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9909) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(1009) +"」が設定されました。");
				}
				player.openInventory(MenuInventoryData.getTitleRankData(player));

			}
			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleSeichi(player));
				return;
			}
		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地量」")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType() == Material.BEDROCK){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
				player.openInventory(MenuInventoryData.getTitleAmountData(player));
			}
			else if (itemstackcurrent.getType() == Material.DIAMOND_BLOCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No3001「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3001) +"」")){
					playerdata.setDisplayTitle1No(3001);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3001) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3002「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3002)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3002) +"」")){
					playerdata.setDisplayTitle1No(3002);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(3002);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3002)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3002) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3003「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3003) +"」")){
					playerdata.setDisplayTitle1No(3003);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3003) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3004「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3004)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9902) + "」")){
					playerdata.setDisplayTitle1No(3004);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3004) +
							SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9902) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3005「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3005)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3005) + "」")){
					playerdata.setDisplayTitle1No(3005);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(3005);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3005)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3005) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3006「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3006) +"」")){
					playerdata.setDisplayTitle1No(3006);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3006) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3007「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3007)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + "」")){
					playerdata.setDisplayTitle1No(3007);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3007) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3008「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3008) +"」")){
					playerdata.setDisplayTitle1No(3008);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3008) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3009「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3009)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3009) + "」")){
					playerdata.setDisplayTitle1No(3009);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(3009);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3009)
							+  SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3009) + "」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3010「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3010)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9909) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3010) + "」")){
					playerdata.setDisplayTitle1No(3010);
					playerdata.setDisplayTitle2No(9909);
					playerdata.setDisplayTitle3No(3010);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3010)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9909) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3010) + "」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3011「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3011) +"」")){
					playerdata.setDisplayTitle1No(3011);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3011) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3012「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3012)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3012) + "」")){
					playerdata.setDisplayTitle1No(3012);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(3012);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3012)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3012) + "」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3013「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3013)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3013) + "」")){
					playerdata.setDisplayTitle1No(3013);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(3013);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3013)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3013) + "」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3014「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3014)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9909) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3014) + "」")){
					playerdata.setDisplayTitle1No(3014);
					playerdata.setDisplayTitle2No(9909);
					playerdata.setDisplayTitle3No(3014);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3014)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9909) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(3014) + "」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3015「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3015) +"」")){
					playerdata.setDisplayTitle1No(3015);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3015) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3016「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3016) +"」")){
					playerdata.setDisplayTitle1No(3016);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3016) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3017「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3017) +"」")){
					playerdata.setDisplayTitle1No(3017);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3017) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3018「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3018) +"」")){
					playerdata.setDisplayTitle1No(3018);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3018) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No3019「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3019) +"」")){
					playerdata.setDisplayTitle1No(3019);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(3019) +"」が設定されました。");
				}
				player.openInventory(MenuInventoryData.getTitleAmountData(player));

			}
			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleSeichi(player));
				return;
			}
		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「参加時間」")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType() == Material.BEDROCK){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
				player.openInventory(MenuInventoryData.getTitleTimeData(player));
			}
			else if (itemstackcurrent.getType() == Material.DIAMOND_BLOCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No4001「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4001)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4001) +"」")){
					playerdata.setDisplayTitle1No(4001);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(4001);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4001)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4001) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4002「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4002)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4002) +"」")){
					playerdata.setDisplayTitle1No(4002);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4002);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4002)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4002) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4003「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4003)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4003) +"」")){
					playerdata.setDisplayTitle1No(4003);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4003);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4003)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4003) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4004「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4004)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4004) +"」")){
					playerdata.setDisplayTitle1No(4004);
					playerdata.setDisplayTitle2No(9005);
					playerdata.setDisplayTitle3No(4004);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4004)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4004) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4005「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4005)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4005) +"」")){
					playerdata.setDisplayTitle1No(4005);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4005);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4005)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4005) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4006「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4006)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4006) +"」")){
					playerdata.setDisplayTitle1No(4006);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(4006);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4006)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4006) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4007「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4007)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4007) +"」")){
					playerdata.setDisplayTitle1No(4007);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4007);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4007)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4007) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4008「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4008)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4008) +"」")){
					playerdata.setDisplayTitle1No(4008);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4008);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4008)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4008) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4009「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4009)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4009) +"」")){
					playerdata.setDisplayTitle1No(4009);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4009);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4009)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4009) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4010「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4010)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4010) +"」")){
					playerdata.setDisplayTitle1No(4010);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(4010);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4010)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4010) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4011「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4011)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4011) +"」")){
					playerdata.setDisplayTitle1No(4011);
					playerdata.setDisplayTitle2No(9901);
					playerdata.setDisplayTitle3No(4011);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4011)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4011) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4012「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4012)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4012) +"」")){
					playerdata.setDisplayTitle1No(4012);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4012);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4012)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4012) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4013「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4013)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4013) +"」")){
					playerdata.setDisplayTitle1No(4013);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4013);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4013)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4013) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4014「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4014)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4014) +"」")){
					playerdata.setDisplayTitle1No(4014);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(4014);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4014)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4014) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4015「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4015) +"」")){
					playerdata.setDisplayTitle1No(4015);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4015) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4016「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4016)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4016) +"」")){
					playerdata.setDisplayTitle1No(4016);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4016);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4016)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4016) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4017「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4017) +"」")){
					playerdata.setDisplayTitle1No(4017);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4017) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4018「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4018)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4018) +"」")){
					playerdata.setDisplayTitle1No(4018);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4018);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4018)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4018) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4019「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4019)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4019) +"」")){
					playerdata.setDisplayTitle1No(4019);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4019);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4019)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4019) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4020「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4020)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4020) +"」")){
					playerdata.setDisplayTitle1No(4020);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4020);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4020)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4020) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4021「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4021)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4021) +"」")){
					playerdata.setDisplayTitle1No(4021);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4021);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4021)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4021) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4022「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4022)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9903) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4022) +"」")){
					playerdata.setDisplayTitle1No(4022);
					playerdata.setDisplayTitle2No(9903);
					playerdata.setDisplayTitle3No(4022);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4022)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9903) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4022) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No4023「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4023)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4023) +"」")){
					playerdata.setDisplayTitle1No(4023);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(4023);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(4023)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(4023) +"」が設定されました。");
				}
				player.openInventory(MenuInventoryData.getTitleTimeData(player));
			}
			else if(itemstackcurrent.getType() == Material.EMERALD_BLOCK){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.getTitleFlags().set(8003);
				player.sendMessage("お疲れ様でした！今日のお給料の代わりに二つ名をどうぞ！");
				player.openInventory(MenuInventoryData.getTitleTimeData(player));
			}
			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleLogin(player));
				return;
			}
		}


		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「通算ログイン」")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType() == Material.BEDROCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
				player.openInventory(MenuInventoryData.getTitleJoinAmountData(player));
			}
			else if (itemstackcurrent.getType() == Material.DIAMOND_BLOCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No5101「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5101)
						+  SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5101) + "」")){
					playerdata.setDisplayTitle1No(5101);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5101);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5101)
							+  SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5101)+"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5102「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5102)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9907) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5102) + "」")){
					playerdata.setDisplayTitle1No(5102);
					playerdata.setDisplayTitle2No(9907);
					playerdata.setDisplayTitle3No(5102);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5102)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9907) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5102)+"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5103「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5103)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + "」")){
					playerdata.setDisplayTitle1No(5103);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5103)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5104「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5104)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5104) +"」")){
					playerdata.setDisplayTitle1No(5104);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(5104);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5104)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5104) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5105「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5105)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9907) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5105) +"」")){
					playerdata.setDisplayTitle1No(5105);
					playerdata.setDisplayTitle2No(9907);
					playerdata.setDisplayTitle3No(5105);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5105)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9907) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5105) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5106「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5106)+"」")){
					playerdata.setDisplayTitle1No(5106);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5106)+"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5107「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5107)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9909) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5107) +"」")){
					playerdata.setDisplayTitle1No(5107);
					playerdata.setDisplayTitle2No(9909);
					playerdata.setDisplayTitle3No(5107);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5107)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9909) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5107) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5108「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5108)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5108) +"」")){
					playerdata.setDisplayTitle1No(5108);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5108);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5108)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5108) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5109「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5109)+"」")){
					playerdata.setDisplayTitle1No(5109);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5109)+"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5110「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5110)+"」")){
					playerdata.setDisplayTitle1No(5110);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5110)+"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5111「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5111)+"」")){
					playerdata.setDisplayTitle1No(5111);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5111)+"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5112「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5112)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5112) +"」")){
					playerdata.setDisplayTitle1No(5112);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5112);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5112)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5112) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5113「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5113)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5113) +"」")){
					playerdata.setDisplayTitle1No(5113);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(5113);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5113)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5113) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5114「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5114)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5114) +"」")){
					playerdata.setDisplayTitle1No(5114);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5114);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5114)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5114) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5115「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5115)+"」")){
					playerdata.setDisplayTitle1No(5115);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5115)+"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5116「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5116)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5116) +"」")){
					playerdata.setDisplayTitle1No(5116);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(5116);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5116)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5116) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5117「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5117)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5117) +"」")){
					playerdata.setDisplayTitle1No(5117);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5117);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5117)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5117) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5118「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5118)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5118) +"」")){
					playerdata.setDisplayTitle1No(5118);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5118);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5118)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5118) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5119「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5119)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5119) +"」")){
					playerdata.setDisplayTitle1No(5119);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(5119);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5119)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5119) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5120「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5120)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(5120) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5120) +"」")){
					playerdata.setDisplayTitle1No(5120);
					playerdata.setDisplayTitle2No(5120);
					playerdata.setDisplayTitle3No(5120);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5120)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(5120) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5120) +"」が設定されました。");
				}

				player.openInventory(MenuInventoryData.getTitleJoinAmountData(player));

			}
			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleLogin(player));
				return;
			}
		}


		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「連続ログイン」")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType() == Material.BEDROCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
				player.openInventory(MenuInventoryData.getTitleJoinChainData(player));
			}
			else if (itemstackcurrent.getType() == Material.DIAMOND_BLOCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No5001「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5001)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(5001) + "」")){
					playerdata.setDisplayTitle1No(5001);
					playerdata.setDisplayTitle2No(5001);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5001)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(5001) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5002「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5002)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5002) +"」")){
					playerdata.setDisplayTitle1No(5002);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5002);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5002)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5002) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5003「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5003)+"」")){
					playerdata.setDisplayTitle1No(5003);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5003)+"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5004「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5004)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5004) +"」")){
					playerdata.setDisplayTitle1No(5004);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5004);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5004)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5004) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5005「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5005)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5005) +"」")){
					playerdata.setDisplayTitle1No(5005);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5005);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5005)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5005) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5006「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5006)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5006) +"」")){
					playerdata.setDisplayTitle1No(5006);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(5006);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5006)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(5006) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5007「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5007)+"」")){
					playerdata.setDisplayTitle1No(5007);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5007)+"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No5008「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5008)
						+  SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + "」")){
					playerdata.setDisplayTitle1No(5008);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(5008)
							+  SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905)+"」が設定されました。");
				}

				player.openInventory(MenuInventoryData.getTitleJoinChainData(player));

			}
			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleLogin(player));
				return;
			}
		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「JMS投票数」")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType() == Material.BEDROCK){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。");
				player.openInventory(MenuInventoryData.getTitleSupportData(player));
			}
			else if (itemstackcurrent.getType() == Material.DIAMOND_BLOCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No6001「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6001) +"」")){
					playerdata.setDisplayTitle1No(6001);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6001) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No6002「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6002)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(6002) +"」")){
					playerdata.setDisplayTitle1No(6002);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(6002);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6002)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(6002) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No6003「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6003) +"」")){
					playerdata.setDisplayTitle1No(6003);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6003) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No6004「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6004)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9903) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(6004) +"」")){
					playerdata.setDisplayTitle1No(6004);
					playerdata.setDisplayTitle2No(9903);
					playerdata.setDisplayTitle3No(6004);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6004)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9903) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(6004) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No6005「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6005)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + "」")){
					playerdata.setDisplayTitle1No(6005);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6005)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No6006「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6006)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(6006) +"」")){
					playerdata.setDisplayTitle1No(6006);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(6006);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6006)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(6006) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No6007「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6007)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9902) +"」")){
					playerdata.setDisplayTitle1No(6007);
					playerdata.setDisplayTitle2No(9902);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6007)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9902) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No6008「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6008) +"」")){
					playerdata.setDisplayTitle1No(6008);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(6008) +"」が設定されました。");
				}
				player.openInventory(MenuInventoryData.getTitleSupportData(player));
			}
			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleSpecial(player));
				return;
			}
		}
		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「公式イベント」")){
			event.setCancelled(true);

			//実績解除処理部分の読みこみ
			//TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType() == Material.BEDROCK){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage("この実績は配布解禁式です。運営チームからの配布タイミングを逃さないようご注意ください。");
				player.openInventory(MenuInventoryData.getTitleEventData(player));
			}
			else if (itemstackcurrent.getType() == Material.DIAMOND_BLOCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No7001「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7001)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7001) +"」")){
					playerdata.setDisplayTitle1No(7001);
					playerdata.setDisplayTitle2No(9901);
					playerdata.setDisplayTitle3No(7001);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7001)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7001) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7002「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7002)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7002) +"」")){
					playerdata.setDisplayTitle1No(7002);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7002);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7002)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7002) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7003「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7003)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7003) +"」")){
					playerdata.setDisplayTitle1No(7003);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7003);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7003)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7003) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7004「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(7004) +"」")){
					playerdata.setDisplayTitle1No(0);
					playerdata.setDisplayTitle2No(7004);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(7004) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7005「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7005)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9902) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7005) +"」")){
					playerdata.setDisplayTitle1No(7005);
					playerdata.setDisplayTitle2No(9902);
					playerdata.setDisplayTitle3No(7005);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7005)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9902) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7005) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7006「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7006)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7006) +"」")){
					playerdata.setDisplayTitle1No(7006);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7006);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7006)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7006) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7007「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7007)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7007) +"」")){
					playerdata.setDisplayTitle1No(7007);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7007);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7007)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7007) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7008「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7008)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7008) +"」")){
					playerdata.setDisplayTitle1No(7008);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7008);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7008)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7008) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7009「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7009)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7009) +"」")){
					playerdata.setDisplayTitle1No(7009);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7009);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7009)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7009) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7010「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7010)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7010) +"」")){
					playerdata.setDisplayTitle1No(7010);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7010);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7010)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7010) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7011「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7011)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7011) +"」")){
					playerdata.setDisplayTitle1No(7011);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7011);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7011)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7011) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7012「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7012)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7012) +"」")){
					playerdata.setDisplayTitle1No(7012);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7012);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7012)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7012) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7013「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7013) + "」")){
					playerdata.setDisplayTitle1No(7013);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7013) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7014「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7014) + "」")){
					playerdata.setDisplayTitle1No(7014);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7014) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7015「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7015)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9904) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7015) +"」")){
					playerdata.setDisplayTitle1No(7015);
					playerdata.setDisplayTitle2No(9904);
					playerdata.setDisplayTitle3No(7015);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7015)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9904) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7015) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7016「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7016)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7016) +"」")){
					playerdata.setDisplayTitle1No(7016);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7016);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7016)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7016) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7017「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7017)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7017) +"」")){
					playerdata.setDisplayTitle1No(7017);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7017);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7017)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7017) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7018「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7018)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9904) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7018) +"」")){
					playerdata.setDisplayTitle1No(7018);
					playerdata.setDisplayTitle2No(9904);
					playerdata.setDisplayTitle3No(7018);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7018)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9904) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7018) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7019「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7019)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7019) + "」")){
					playerdata.setDisplayTitle1No(7019);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7019);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7019)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7019) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7020「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7020)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7020) +"」")){
					playerdata.setDisplayTitle1No(7020);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7020);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7020)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7020) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7021「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7021)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7021) +"」")){
					playerdata.setDisplayTitle1No(7021);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7021);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7021)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7021) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7022「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7022)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7022) +"」")){
					playerdata.setDisplayTitle1No(7022);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7022);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7022)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7022) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7023「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7023)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7023) +"」")){
					playerdata.setDisplayTitle1No(7023);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7023);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7023)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7023) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7024「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7024)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7024) +"」")){
					playerdata.setDisplayTitle1No(7024);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7024);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7024)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7024) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7025「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7025)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7025) +"」")){
					playerdata.setDisplayTitle1No(7025);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7025);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7025)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7025) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7026「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7026)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7026) +"」")){
					playerdata.setDisplayTitle1No(7026);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7026);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7026)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7026) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7027「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7027)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7027) +"」")){
					playerdata.setDisplayTitle1No(7027);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7027);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7027)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7027) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7901「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7901)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(7901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7901) +"」")){
					playerdata.setDisplayTitle1No(7901);
					playerdata.setDisplayTitle2No(7901);
					playerdata.setDisplayTitle3No(7901);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7901)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(7901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7901) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7902「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7902)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7902) +"」")){
					playerdata.setDisplayTitle1No(7902);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7902);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7902)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7902) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7903「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7903)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7903) +"」")){
					playerdata.setDisplayTitle1No(7903);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(7903);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7903)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7903) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7904「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7904)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9907) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7904) +"」")){
					playerdata.setDisplayTitle1No(7904);
					playerdata.setDisplayTitle2No(9907);
					playerdata.setDisplayTitle3No(7904);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7904)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9907) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7904) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7905「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7905)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7905) +"」")){
					playerdata.setDisplayTitle1No(7905);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7905);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7905)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7905) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No7906「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7906)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7906) +"」")){
					playerdata.setDisplayTitle1No(7906);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(7906);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(7906)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(7906) +"」が設定されました。");
				}
				player.openInventory(MenuInventoryData.getTitleEventData(player));

			}
			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleSpecial(player));
				return;
			}
		}


		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「記念日」")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(itemstackcurrent.getType() == Material.BEDROCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No9001「???」")){
					SeichiAchievement.tryAchieve(player,9001);
				}else if(itemmeta.getDisplayName().contains("No9002「???」")){
					SeichiAchievement.tryAchieve(player,9002);
				}else if(itemmeta.getDisplayName().contains("No9003「???」")){
					SeichiAchievement.tryAchieve(player,9003);
				}else if(itemmeta.getDisplayName().contains("No9004「???」")){
					SeichiAchievement.tryAchieve(player,9004);
				}else if(itemmeta.getDisplayName().contains("No9005「???」")){
					SeichiAchievement.tryAchieve(player,9005);
				}else if(itemmeta.getDisplayName().contains("No9006「???」")){
					SeichiAchievement.tryAchieve(player,9006);
				}else if(itemmeta.getDisplayName().contains("No9007「???」")){
					SeichiAchievement.tryAchieve(player,9007);
				}else if(itemmeta.getDisplayName().contains("No9008「???」")){
					SeichiAchievement.tryAchieve(player,9008);
				}else if(itemmeta.getDisplayName().contains("No9009「???」")){
					SeichiAchievement.tryAchieve(player,9009);
				}else if(itemmeta.getDisplayName().contains("No9010「???」")){
					SeichiAchievement.tryAchieve(player,9010);
				}else if(itemmeta.getDisplayName().contains("No9011「???」")){
					SeichiAchievement.tryAchieve(player,9011);
				}else if(itemmeta.getDisplayName().contains("No9012「???」")){
					SeichiAchievement.tryAchieve(player,9012);
				}else if(itemmeta.getDisplayName().contains("No9013「???」")){
					SeichiAchievement.tryAchieve(player,9013);
				}else if(itemmeta.getDisplayName().contains("No9014「???」")){
					SeichiAchievement.tryAchieve(player,9014);
				}else if(itemmeta.getDisplayName().contains("No9015「???」")){
					SeichiAchievement.tryAchieve(player,9015);
				}else if(itemmeta.getDisplayName().contains("No9016「???」")){
					SeichiAchievement.tryAchieve(player,9016);
				}else if(itemmeta.getDisplayName().contains("No9017「???」")){
					SeichiAchievement.tryAchieve(player,9017);
				}else if(itemmeta.getDisplayName().contains("No9018「???」")){
					SeichiAchievement.tryAchieve(player,9018);
				}else if(itemmeta.getDisplayName().contains("No9019「???」")){
					SeichiAchievement.tryAchieve(player,9019);
				}else if(itemmeta.getDisplayName().contains("No9020「???」")){
					SeichiAchievement.tryAchieve(player,9020);
				}else if(itemmeta.getDisplayName().contains("No9021「???」")){
					SeichiAchievement.tryAchieve(player,9021);
				}else if(itemmeta.getDisplayName().contains("No9022「???」")){
					SeichiAchievement.tryAchieve(player,9022);
				}else if(itemmeta.getDisplayName().contains("No9023「???」")){
					SeichiAchievement.tryAchieve(player,9023);
				}else if(itemmeta.getDisplayName().contains("No9024「???」")){
					SeichiAchievement.tryAchieve(player,9024);
				}else if(itemmeta.getDisplayName().contains("No9025「???」")){
					SeichiAchievement.tryAchieve(player,9025);
				}else if(itemmeta.getDisplayName().contains("No9026「???」")){
					SeichiAchievement.tryAchieve(player,9026);
				}else if(itemmeta.getDisplayName().contains("No9027「???」")){
					SeichiAchievement.tryAchieve(player,9027);
				}else if(itemmeta.getDisplayName().contains("No9028「???」")){
					SeichiAchievement.tryAchieve(player,9028);
				}else if(itemmeta.getDisplayName().contains("No9029「???」")){
					SeichiAchievement.tryAchieve(player,9029);
				}else if(itemmeta.getDisplayName().contains("No9030「???」")){
					SeichiAchievement.tryAchieve(player,9030);
				}else if(itemmeta.getDisplayName().contains("No9031「???」")){
					SeichiAchievement.tryAchieve(player,9031);
				}else if(itemmeta.getDisplayName().contains("No9032「???」")){
					SeichiAchievement.tryAchieve(player,9032);
				}else if(itemmeta.getDisplayName().contains("No9033「???」")){
					SeichiAchievement.tryAchieve(player,9033);
				}else if(itemmeta.getDisplayName().contains("No9034「???」")){
					SeichiAchievement.tryAchieve(player,9034);
				}else if(itemmeta.getDisplayName().contains("No9035「???」")){
					SeichiAchievement.tryAchieve(player,9035);
				}else if(itemmeta.getDisplayName().contains("No9036「???」")){
					SeichiAchievement.tryAchieve(player,9036);
				}

				player.openInventory(MenuInventoryData.getTitleExtraData(player));
			}
			else if (itemstackcurrent.getType() == Material.DIAMOND_BLOCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No9001「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9001) +"」")){
					playerdata.setDisplayTitle1No(9001);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9001) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9002「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9002)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9002) +"」")){
					playerdata.setDisplayTitle1No(9002);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9002);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9002)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9002) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9003「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9003) +"」")){
					playerdata.setDisplayTitle1No(9003);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9003) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9004「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9004)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9004) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9004) +"」")){
					playerdata.setDisplayTitle1No(9004);
					playerdata.setDisplayTitle2No(9004);
					playerdata.setDisplayTitle3No(9004);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9004)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9004) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9004) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9005「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9005)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9005) +"」")){
					playerdata.setDisplayTitle1No(9005);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9005);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9005)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9005) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9006「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9006) +"」")){
					playerdata.setDisplayTitle1No(9006);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9006) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9007「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9007) +"」")){
					playerdata.setDisplayTitle1No(9007);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9007) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9008「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9008)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9008) +"」")){
					playerdata.setDisplayTitle1No(9008);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9008);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9008)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9008) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9009「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9009) +"」")){
					playerdata.setDisplayTitle1No(9009);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9009) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9010「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9010)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9903) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9010) +"」")){
					playerdata.setDisplayTitle1No(9010);
					playerdata.setDisplayTitle2No(9903);
					playerdata.setDisplayTitle3No(9010);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9010)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9903) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9010) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9011「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9011)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9011) +"」")){
					playerdata.setDisplayTitle1No(9011);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9011);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9011)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9011) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9012「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9012)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9012) +"」")){
					playerdata.setDisplayTitle1No(9012);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9012);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9012)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9012) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9013「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9013) +"」")){
					playerdata.setDisplayTitle1No(9013);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9013) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9014「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9014) +"」")){
					playerdata.setDisplayTitle1No(0);
					playerdata.setDisplayTitle2No(9014);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9014) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9015「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9015)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9015) +"」")){
					playerdata.setDisplayTitle1No(9015);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9015);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9015)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9015) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9016「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9016)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9016) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9016) +"」")){
					playerdata.setDisplayTitle1No(9016);
					playerdata.setDisplayTitle2No(9016);
					playerdata.setDisplayTitle3No(9016);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9016)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9016) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9016) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9017「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9017)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9017) +"」")){
					playerdata.setDisplayTitle1No(9017);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9017);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9017)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9017) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9018「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9018) +"」")){
					playerdata.setDisplayTitle1No(9018);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(0);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9018) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9019「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9019)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9019) + "」")){
					playerdata.setDisplayTitle1No(9019);
					playerdata.setDisplayTitle2No(9901);
					playerdata.setDisplayTitle3No(9019);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9019)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9019) + "」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9020「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9020)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9020) +"」")){
					playerdata.setDisplayTitle1No(9020);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9020);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9020)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9020) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9021「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9021)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9021) +"」")){
					playerdata.setDisplayTitle1No(9021);
					playerdata.setDisplayTitle2No(9901);
					playerdata.setDisplayTitle3No(9021);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9021)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9901) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9021) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9022「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9022)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9022) +"」")){
					playerdata.setDisplayTitle1No(9022);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9022);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9022)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9022) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9023「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9023)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9023) +"」")){
					playerdata.setDisplayTitle1No(9023);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9023);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9023)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9023) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9024「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9024)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9024) +"」")){
					playerdata.setDisplayTitle1No(9024);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9024);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9024)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9024) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9025「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9025)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9025) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9025) +"」")){
					playerdata.setDisplayTitle1No(9025);
					playerdata.setDisplayTitle2No(9025);
					playerdata.setDisplayTitle3No(9025);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9025)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9025) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9025) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9026「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9026)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9026) +"」")){
					playerdata.setDisplayTitle1No(9026);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9026);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9026)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9026) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9027「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9027)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9027) +"」")){
					playerdata.setDisplayTitle1No(9027);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9027);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9027)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9027) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9028「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9028)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9028) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9028) +"」")){
					playerdata.setDisplayTitle1No(9028);
					playerdata.setDisplayTitle2No(9028);
					playerdata.setDisplayTitle3No(9028);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9028)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9028) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9028) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9029「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9029)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9029) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9029) +"」")){
					playerdata.setDisplayTitle1No(9029);
					playerdata.setDisplayTitle2No(9029);
					playerdata.setDisplayTitle3No(9029);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9029)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9029) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9029) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9030「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9030)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9030) +"」")){
					playerdata.setDisplayTitle1No(9030);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(9030);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9030)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9030) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9031「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9031)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9908) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9031) +"」")){
					playerdata.setDisplayTitle1No(9031);
					playerdata.setDisplayTitle2No(9908);
					playerdata.setDisplayTitle3No(9031);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9031)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9908) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9031) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9032「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9032)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9032) +"」")){
					playerdata.setDisplayTitle1No(9032);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9032);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9032)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9032) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9033「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9033)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9903) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9033) +"」")){
					playerdata.setDisplayTitle1No(9033);
					playerdata.setDisplayTitle2No(9903);
					playerdata.setDisplayTitle3No(9033);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9033)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9903) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9033) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9034「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9034)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9034) +"」")){
					playerdata.setDisplayTitle1No(9034);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9034);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9034)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9034) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9035「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9035)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9035) +"」")){
					playerdata.setDisplayTitle1No(9035);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(9035);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9035)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9035) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No9036「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9036)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9036) +"」")){
					playerdata.setDisplayTitle1No(9036);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(9036);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(9036)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(9036) +"」が設定されました。");
				}
				player.openInventory(MenuInventoryData.getTitleExtraData(player));
			}
			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleLogin(player));
				return;
			}

			//次ページ
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowRight")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				playerdata.setTitlepage(playerdata.getTitlepage() + 1);
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType() == Material.BEDROCK){
				//ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage("この実績は「極秘実績」です。いろいろやってみましょう！");
				player.openInventory(MenuInventoryData.getTitleSecretData(player));
			}
			else if (itemstackcurrent.getType() == Material.DIAMOND_BLOCK){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(itemmeta.getDisplayName().contains("No8001「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(8001)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(8001) +"」")){
					playerdata.setDisplayTitle1No(8001);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(8001);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(8001)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(8001) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No8002「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(8002)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(8002) +"」")){
					playerdata.setDisplayTitle1No(8002);
					playerdata.setDisplayTitle2No(9905);
					playerdata.setDisplayTitle3No(8002);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(8002)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle2(9905) + SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(8002) +"」が設定されました。");
				}
				else if(itemmeta.getDisplayName().contains("No8003「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(8003)
						+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(8003) +"」")){
					playerdata.setDisplayTitle1No(8003);
					playerdata.setDisplayTitle2No(0);
					playerdata.setDisplayTitle3No(8003);
					player.sendMessage("二つ名「"+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle1(8003)
							+ SeichiAssist.Companion.getSeichiAssistConfig().getTitle3(8003) +"」が設定されました。");
				}
				player.openInventory(MenuInventoryData.getTitleSecretData(player));

			}
			//実績メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getTitleSpecial(player));
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
			List<ItemStack> dropitem = new ArrayList<>();
			//余剰鉱石返却用アイテムリスト
			List<ItemStack> retore = new ArrayList<>();
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
				else if(m.getType() == Material.COAL_ORE){
					//石炭なら個数分だけcoaloreを増やす(以下同様)
					coalore += m.getAmount();
					continue;
				}
				else if(m.getType() == Material.IRON_ORE){
					ironore += m.getAmount();
					continue;
				}
				else if(m.getType() == Material.GOLD_ORE){
					goldore += m.getAmount();
					continue;
				}
				else if(m.getType() == Material.LAPIS_ORE){
					lapisore += m.getAmount();
					continue;
				}
				else if(m.getType() == Material.DIAMOND_ORE){
					diamondore += m.getAmount();
					continue;
				}
				else if(m.getType() == Material.REDSTONE_ORE){
					redstoneore += m.getAmount();
					continue;
				}
				else if(m.getType() == Material.EMERALD_ORE){
					emeraldore += m.getAmount();
					continue;
				}
				else if(m.getType() == Material.QUARTZ_ORE){
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
			ItemStack exchangeticket = new ItemStack(Material.PAPER);
			ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
			itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "交換券");
			itemmeta.addEnchant(Enchantment.PROTECTION_FIRE, 1, false);
			itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			exchangeticket.setItemMeta(itemmeta);

			int count = 0;
			while(giveticket > 0){
				if(player.getInventory().contains(exchangeticket) || !Util.isPlayerInventoryFull(player)){
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
				if(!Util.isPlayerInventoryFull(player)){
					Util.addItem(player,m);
				}else{
					Util.dropItem(player,m);
				}
			}
			for(ItemStack m : retore){
				if(!Util.isPlayerInventoryFull(player)){
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
		String name = playerdata.getName();
		Inventory inventory = event.getInventory();

		//インベントリサイズが36でない時終了
		if(inventory.getSize() != 36){
			return;
		}
		if(inventory.getTitle().equals(ChatColor.GOLD + "" + ChatColor.BOLD + "椎名林檎と交換したい景品を入れてネ")){
			int giveringo = 0;
			/*
			 * step1 for文でinventory内に対象商品がないか検索
			 * あったらdurabilityに応じてgivegachaを増やし、非対象商品は返却boxへ
			 */
			//ガチャ景品交換インベントリの中身を取得
			ItemStack[] item = inventory.getContents();
			//ドロップ用アイテムリスト(返却box)作成
			List<ItemStack> dropitem = new ArrayList<>();
			//カウント用
			int giga = 0;
			//for文で１個ずつ対象アイテムか見る
			//ガチャ景品交換インベントリを一個ずつ見ていくfor文
			for (ItemStack m : item) {
				//無いなら次へ
				if(m == null){
					continue;
				}else if(SeichiAssist.Companion.getGachamente()){
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
				}else if(m.getType() == Material.SKULL_ITEM){
					//丁重にお返しする
					dropitem.add(m);
					continue;
				}
				//ガチャ景品リストにアイテムがあった時にtrueになるフラグ
				boolean flag = false;
				//ガチャ景品リストを一個ずつ見ていくfor文
				for(GachaPrize gachadata : gachadatalist){
					if(!gachadata.getItemStack().hasItemMeta()){
						continue;
					}else if(!gachadata.getItemStack().getItemMeta().hasLore()){
						continue;
					}
					//ガチャ景品リストにある商品の場合(Lore=説明文と表示名で判別),無い場合はアイテム返却
					if(gachadata.compare(m,name)){
						if(SeichiAssist.Companion.getDEBUG()){
							player.sendMessage(gachadata.getItemStack().getItemMeta().getDisplayName());
						}
						flag = true;
						int amount = m.getAmount();
						if(gachadata.getProbability() < 0.001){
							//ギガンティック大当たりの部分
							//1個につき椎名林檎n個と交換する
							giveringo += (SeichiAssist.Companion.getSeichiAssistConfig().rateGiganticToRingo()*amount);
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
			if(SeichiAssist.Companion.getGachamente()){
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
				if(!Util.isPlayerInventoryFull(player)){
					Util.addItem(player,m);
				}else{
					Util.dropItem(player,m);
				}
			}
			/*
			 * step3 椎名林檎をインベントリへ
			 */
			ItemStack ringo = StaticGachaPrizeFactory.getMaxRingo(Util.getName(player));
			int count = 0;
			while(giveringo > 0){
				if(player.getInventory().contains(ringo) || !Util.isPlayerInventoryFull(player)){
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

	@EventHandler
	public void onTitanRepairEvent(InventoryCloseEvent event){
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
		if(inventory.getTitle().equals(ChatColor.GOLD + "" + ChatColor.BOLD + "修繕したい限定タイタンを入れてネ")){
			//インベントリの中身を取得
			ItemStack[] item = inventory.getContents();

			int count = 0;
			//for文で１個ずつ対象アイテムか見る
			//インベントリを一個ずつ見ていくfor文
			for (ItemStack m : item) {
				//無いなら次へ
				if(m == null){
					continue;
				}
				if(m.getItemMeta().hasLore()){
					if (Util.isLimitedTitanItem(m)){
						m.setDurability((short) 1);
						count ++ ;
					}
				}

				if(!Util.isPlayerInventoryFull(player)) {
					 Util.addItem(player,m);
				 }else {
					 Util.dropItem(player,m);
				 }
			}
			if(count < 1) {
				player.sendMessage(ChatColor.GREEN + "限定タイタンを認識しませんでした。すべてのアイテムを返却します");
			}else {
				player.sendMessage(ChatColor.GREEN + "限定タイタンを" + count + "個認識し、修繕しました。");
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
		if(he.getType() != EntityType.PLAYER){
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
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票ptメニュー")){
			event.setCancelled(true);

			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			final boolean isSkull = itemstackcurrent.getType() == Material.SKULL_ITEM;

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			//投票pt受取
			if(itemstackcurrent.getType() == Material.DIAMOND){
				//nは特典をまだ受け取ってない投票分
				int n = databaseGateway.playerDataManipulator.compareVotePoint(player,playerdata);
				//投票数に変化が無ければ処理終了
				if(n == 0){
					return;
				}
				//先にp_voteの値を更新しておく
				playerdata.setP_givenvote(playerdata.getP_givenvote() + n);

				int count = 0;
				while(n > 0){
					//ここに投票1回につきプレゼントする特典の処理を書く

					//ガチャ券プレゼント処理
					ItemStack skull = Util.getVoteskull(Util.getName(player));
					for (int i = 0; i < 10; i++){
						if(player.getInventory().contains(skull) || !Util.isPlayerInventoryFull(player)){
							Util.addItem(player,skull);
						}else{
							Util.dropItem(player,skull);
						}
					}

					//ピッケルプレゼント処理(レベル50になるまで)
					if(playerdata.getLevel() < 50){
						ItemStack pickaxe = ItemData.getSuperPickaxe(1);
						if (Util.isPlayerInventoryFull(player)) {
							Util.dropItem(player, pickaxe);
						} else {
							Util.addItem(player, pickaxe);
						}
					}

				  //投票ギフト処理(レベル50から)
					if(playerdata.getLevel() >= 50){
						ItemStack gift = ItemData.getVotingGift(1);
						if (Util.isPlayerInventoryFull(player)) {
							Util.dropItem(player, gift);
						} else {
							Util.addItem(player, gift);
						}
					}
					//エフェクトポイント加算処理
					playerdata.getActiveskilldata().effectpoint += 10;

					n--;
					count++;
				}

				player.sendMessage(ChatColor.GOLD + "投票特典" + ChatColor.WHITE + "(" + count + "票分)を受け取りました");
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);

				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemstackcurrent.setItemMeta(itemmeta);
				player.openInventory(MenuInventoryData.getVotingMenuData(player));
			}

			else if(itemstackcurrent.getType() == Material.BOOK_AND_QUILL){
				// 投票リンク表示
				player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "https://minecraft.jp/servers/54d3529e4ddda180780041a7/vote");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			//棒メニューに戻る
			else if(isSkull && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
			}

			//妖精時間トグル
			else if (itemstackcurrent.getType() == Material.WATCH){
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.setToggleVotingFairy(playerdata.getToggleVotingFairy() % 4 + 1);
				player.openInventory(MenuInventoryData.getVotingMenuData(player));
			}
			//妖精リンゴトグル
			else if (itemstackcurrent.getType() == Material.PAPER){
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.setToggleGiveApple(playerdata.getToggleGiveApple() % 4 + 1);
				player.openInventory(MenuInventoryData.getVotingMenuData(player));
			}
			//妖精音トグル
			else if (itemstackcurrent.getType() == Material.JUKEBOX){
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				playerdata.setToggleVFSound(!playerdata.getToggleVFSound());
				player.openInventory(MenuInventoryData.getVotingMenuData(player));
			}

			//妖精召喚
			else if (itemstackcurrent.getType() == Material.GHAST_TEAR){
				player.closeInventory();

				//プレイヤーレベルが10に達していないとき
				if(playerdata.getLevel() < 10){
					player.sendMessage(ChatColor.GOLD + "プレイヤーレベルが足りません") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
					return;
				}

				//既に妖精召喚している場合終了
				if(playerdata.getUsingVotingFairy()){
					player.sendMessage(ChatColor.GOLD + "既に妖精を召喚しています") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
					return;
				}

				//投票ptが足りない場合終了
				if( playerdata.getActiveskilldata().effectpoint < playerdata.getToggleVotingFairy() *2 ){
					player.sendMessage(ChatColor.GOLD + "投票ptが足りません") ;
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;
					return;
				}

				VotingFairyListener.summon(player);
				player.closeInventory() ;
			}

			else if (itemstackcurrent.getType() == Material.COMPASS) {
				VotingFairyTask.speak(player, "僕は" + Util.showHour(playerdata.getVotingFairyEndTime()) + "には帰るよー。", playerdata.getToggleVFSound());
				player.closeInventory();
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
		if(he.getType() != EntityType.PLAYER){
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

			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
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
				playerdata.setSelectHomeNum(0);
				player.openInventory(MenuInventoryData.getCheckSetHomeMenuData(player));
			}
			for(int x = 1; x <= SeichiAssist.Companion.getSeichiAssistConfig().getSubHomeMax() ; x++){
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
					playerdata.setSelectHomeNum(x);
					player.openInventory(MenuInventoryData.getCheckSetHomeMenuData(player));
				}
			}

		}
		else if(topinventory.getTitle().contains("ホームポイントを変更しますか?")){
			event.setCancelled(true);

			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			if(itemmeta.getDisplayName().contains("変更する")){
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(playerdata.getSelectHomeNum() == 0) player.chat("/sethome");
				else player.chat("/subhome set " + playerdata.getSelectHomeNum());
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
		if(he.getType() != EntityType.PLAYER){
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
			if (itemstackcurrent.getType() == Material.NETHER_STAR){
				playerdata.setGBstage(playerdata.getGBstage() + 1);
				playerdata.setGBlevel(0);
				playerdata.setGBexp(0);
				playerdata.setGBStageUp(false);
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
