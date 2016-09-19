package com.github.unchama.seichiassist.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.MenuInventoryData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class PlayerInventoryListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
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
				player.openInventory(MenuInventoryData.getMineStackMenu(player));
				return;
			}
			//スキルメニューを開く
			else if(itemstackcurrent.getType().equals(Material.ENCHANTED_BOOK)){
				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				//アクティブスキルとパッシブスキルの分岐
				if(itemmeta.getDisplayName().contains("アクティブ")){
					player.openInventory(MenuInventoryData.getActiveSkillMenuData(player));
				}else if(itemmeta.getDisplayName().contains("パッシブ")){
					player.sendMessage("未実装ナリよ");
					//player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player));
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
				ItemStack skull = Util.getskull(Util.getName(player));
				int count = 0;
				while(playerdata.gachapoint >= config.getGachaPresentInterval()){
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

			//詫びガチャ券をインベントリへ
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("whitecat_haru")){

				playerdata.giveSorryForBug(player);

				ItemMeta itemmeta = itemstackcurrent.getItemMeta();
				itemmeta.setLore(MenuInventoryData.SorryGachaGetButtonLore(playerdata));
				itemstackcurrent.setItemMeta(itemmeta);
			}

			//投票特典受け取り
			else if(itemstackcurrent.getType().equals(Material.DIAMOND)){

				int n = sql.compareVotePoint(playerdata);
				//投票数に変化が無ければ処理終了
				if(n == 0){
					return;
				}
				//先にp_voteの値を更新しておく
				playerdata.p_givenvote += n;

				while(n > 0){
					//ここに投票1回につきプレゼントする特典の処理を書く

					//ガチャ券プレゼント処理
					ItemStack skull = Util.getskull(Util.getName(player));
					for (int i = 0; i < 10; i++){
						if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
							Util.addItem(player,skull);
						}else{
							Util.dropItem(player,skull);
						}
					}

					//ピッケルプレゼント処理(レベル30になるまで)
					if(playerdata.level < 30){
						ItemStack itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
						ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
						itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Thanks for Voting!");
						List<String> lore = Arrays.asList("投票特典でもらえるピッケルです"
								, "整地レベルが30になるまで毎回貰えます"
								);
						itemmeta.addEnchant(Enchantment.DIG_SPEED, 3, true);
						itemmeta.addEnchant(Enchantment.DURABILITY, 5, true);
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
						if(!Util.isPlayerInventryFill(player)){
							Util.addItem(player,itemstack);
						}else{
							Util.dropItem(player,itemstack);
						}
					}

					//エフェクトポイント加算処理
					playerdata.activeskilldata.effectpoint++;

					n--;
				}

				player.sendMessage(ChatColor.GOLD + "投票特典" + ChatColor.WHITE + "を受け取りました");
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

			else if(itemstackcurrent.getType().equals(Material.WHEAT)){
				// farmassist toggleコマンド実行
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/farmassist toggle");
			}
			else if(itemstackcurrent.getType().equals(Material.LOG)){
				// treeassist toggleコマンド実行
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/treeassist toggle");
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
				player.chat("/sethome");
			}

			else if(itemstackcurrent.getType().equals(Material.COMPASS)){
				// homeコマンド実行
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/home");
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
				if (selection == null) {
					player.sendMessage(ChatColor.RED + "先に木の斧で範囲を指定してからこのボタンを押してください");
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
				player.sendMessage(ChatColor.GRAY + "複数ページある場合は " + ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.BOLD + "/rg list ページNo\n"
				+ ChatColor.RESET + "" +  ChatColor.GRAY + "で2ページ目以降を開いてください\n"
				+ ChatColor.DARK_GREEN + "解説ページ→" + ChatColor.UNDERLINE + "http://seichi.click/d/WorldGuard");

				player.chat("/rg list");
			}


			else if(itemstackcurrent.getType().equals(Material.NETHER_STAR)){
				// hubコマンド実行
				// player.chat("/hub");
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.sendMessage(ChatColor.RESET + "" +  ChatColor.GRAY + "Tキーを押して/hubと入力してEnterキーを押してください");
			}


			else if(itemstackcurrent.getType().equals(Material.BOOK)){
				// wikiリンク表示
				player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "http://seichi.click");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType().equals(Material.BOOK_AND_QUILL)){
				// 投票リンク表示
				player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "https://minecraft.jp/servers/seichi.click/vote");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType().equals(Material.PAPER)){
				// 運営方針とルールリンク表示
				player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "http://seichi.click/d/%b1%bf%b1%c4%ca%fd%bf%cb%a4%c8%a5%eb%a1%bc%a5%eb");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();
			}

			else if(itemstackcurrent.getType().equals(Material.MAP)){
				// 鯖マップリンク表示
				player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "http://seichi.click/d/DynmapLinks");
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.closeInventory();

			}

			else if(itemstackcurrent.getType().equals(Material.SIGN)){
				// 掲示板リンク表示
				player.sendMessage(ChatColor.DARK_GRAY + "開いたら下の方までスクロールしてください\n"
						+ ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "https://minecraft.jp/servers/seichi.click"
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
			//ARROWSKILL
			for(int skilllevel = 4;skilllevel <= 9 ; skilllevel++){

				if(itemstackcurrent.getType().equals(ActiveSkill.ARROW.getMaterial(skilllevel))){
					PotionMeta potionmeta =(PotionMeta)itemstackcurrent.getItemMeta();
					if(potionmeta.getBasePotionData().getType().equals(ActiveSkill.ARROW.getPotionType(skilllevel))){
						if(playerdata.activeskilldata.skilltype == ActiveSkill.ARROW.gettypenum()
								&& playerdata.activeskilldata.skillnum == skilllevel){
							player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
							player.sendMessage(ChatColor.YELLOW + "既に選択されています");
						}else{
							playerdata.activeskilldata.skilltype = ActiveSkill.ARROW.gettypenum();
							playerdata.activeskilldata.skillnum = skilllevel;
							player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + ActiveSkill.ARROW.getName(skilllevel) + "  が選択されました");
							playerdata.activeskilldata.mineflagnum = 1;
							player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
						}
					}
				}
			}
			//MULTISKILL
			for(int skilllevel = 4;skilllevel <= 9 ; skilllevel++){
				if(itemstackcurrent.getType().equals(ActiveSkill.MULTI.getMaterial(skilllevel))){
					if(playerdata.activeskilldata.skilltype == ActiveSkill.MULTI.gettypenum()
							&& playerdata.activeskilldata.skillnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "既に選択されています");
					}else{
						playerdata.activeskilldata.skilltype = ActiveSkill.MULTI.gettypenum();
						playerdata.activeskilldata.skillnum = skilllevel;
						player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + ActiveSkill.MULTI.getName(skilllevel) + "  が選択されました");
						playerdata.activeskilldata.mineflagnum = 1;
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}
			//BREAKSKILL
			for(int skilllevel = 1;skilllevel <= 9 ; skilllevel++){
				if(itemstackcurrent.getType().equals(ActiveSkill.BREAK.getMaterial(skilllevel))){
					if(playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum()
							&& playerdata.activeskilldata.skillnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "既に選択されています");
					}else{
						playerdata.activeskilldata.skilltype = ActiveSkill.BREAK.gettypenum();
						playerdata.activeskilldata.skillnum = skilllevel;
						player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + ActiveSkill.BREAK.getName(skilllevel) + "  が選択されました");
						playerdata.activeskilldata.mineflagnum = 1;
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
				}
			}
			//CONDENSKILL
			for(int skilllevel = 4;skilllevel <= 9 ; skilllevel++){
				if(itemstackcurrent.getType().equals(ActiveSkill.CONDENSE.getMaterial(skilllevel))){
					if(playerdata.activeskilldata.skilltype == ActiveSkill.CONDENSE.gettypenum()
							&& playerdata.activeskilldata.skillnum == skilllevel){
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
						player.sendMessage(ChatColor.YELLOW + "既に選択されています");
					}else{
						playerdata.activeskilldata.skilltype = ActiveSkill.CONDENSE.gettypenum();
						playerdata.activeskilldata.skillnum = skilllevel;
						player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + ActiveSkill.CONDENSE.getName(skilllevel) + "  が選択されました");
						playerdata.activeskilldata.mineflagnum = 1;
						player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
					}
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
			else if(itemstackcurrent.getType().equals(Material.GLASS)){
				if(playerdata.activeskilldata.skilltype == 0 && playerdata.activeskilldata.skillnum == 0){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else{
					playerdata.activeskilldata.skilltype = 0;
					playerdata.activeskilldata.skillnum = 0;
					player.sendMessage(ChatColor.GREEN + "アクティブスキル:未設定  が選択されました");
					playerdata.activeskilldata.mineflagnum = 1;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
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
		//インベントリサイズが36でない時終了
		if(topinventory.getSize() != 36){
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
							player.sendMessage(ChatColor.LIGHT_PURPLE+ "" + ChatColor.BOLD + "エフェクト：" + skilleffect[i].getName() + ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE+ "" + ChatColor.BOLD + "" + " を解除しました");
							player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2);
							playerdata.activeskilldata.effectpoint -= skilleffect[i].getUsePoint();
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

		if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack")){
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

			//dirt
			else if(itemstackcurrent.getType().equals(Material.DIRT)){
				playerdata.minestack.dirt = giveMineStack(player,playerdata.minestack.dirt,Material.DIRT);
			}

			//grass
			else if(itemstackcurrent.getType().equals(Material.GRASS)){
				playerdata.minestack.grass = giveMineStack(player,playerdata.minestack.grass,Material.GRASS);
			}

			//gravel
			else if(itemstackcurrent.getType().equals(Material.GRAVEL)){
				playerdata.minestack.gravel = giveMineStack(player,playerdata.minestack.gravel,Material.GRAVEL);
			}

			//cobblestone
			else if(itemstackcurrent.getType().equals(Material.COBBLESTONE)){
				playerdata.minestack.cobblestone = giveMineStack(player,playerdata.minestack.cobblestone,Material.COBBLESTONE);
			}

			//stone
			else if(itemstackcurrent.getType().equals(Material.STONE)){
				playerdata.minestack.stone = giveMineStack(player,playerdata.minestack.stone,Material.STONE);
			}

			//sand
			else if(itemstackcurrent.getType().equals(Material.SAND)){
				playerdata.minestack.sand = giveMineStack(player,playerdata.minestack.sand,Material.SAND);
			}
			//packed_ice
			else if(itemstackcurrent.getType().equals(Material.PACKED_ICE)){
				playerdata.minestack.packed_ice = giveMineStack(player,playerdata.minestack.packed_ice,Material.PACKED_ICE);
			}
			//sandstone
			else if(itemstackcurrent.getType().equals(Material.SANDSTONE)){
				playerdata.minestack.sandstone = giveMineStack(player,playerdata.minestack.sandstone,Material.SANDSTONE);
			}

			//netherrack
			else if(itemstackcurrent.getType().equals(Material.NETHERRACK)){
				playerdata.minestack.netherrack = giveMineStack(player,playerdata.minestack.netherrack,Material.NETHERRACK);
			}

			//soul_sand
			else if(itemstackcurrent.getType().equals(Material.SOUL_SAND)){
				playerdata.minestack.soul_sand = giveMineStack(player,playerdata.minestack.soul_sand,Material.SOUL_SAND);
			}

			//magma
			else if(itemstackcurrent.getType().equals(Material.MAGMA)){
				playerdata.minestack.magma = giveMineStack(player,playerdata.minestack.magma,Material.MAGMA);
			}

			//ender_stone
			else if(itemstackcurrent.getType().equals(Material.ENDER_STONE)){
				playerdata.minestack.ender_stone = giveMineStack(player,playerdata.minestack.ender_stone,Material.ENDER_STONE);
			}

			//coal
			else if(itemstackcurrent.getType().equals(Material.COAL)){
				playerdata.minestack.coal = giveMineStack(player,playerdata.minestack.coal,Material.COAL);
			}

			//coal_ore
			else if(itemstackcurrent.getType().equals(Material.COAL_ORE)){
				playerdata.minestack.coal_ore = giveMineStack(player,playerdata.minestack.coal_ore,Material.COAL_ORE);
			}

			//iron_ore
			else if(itemstackcurrent.getType().equals(Material.IRON_ORE)){
				playerdata.minestack.iron_ore = giveMineStack(player,playerdata.minestack.iron_ore,Material.IRON_ORE);
			}

			//quartz
			else if(itemstackcurrent.getType().equals(Material.QUARTZ)){
				playerdata.minestack.quartz = giveMineStack(player,playerdata.minestack.quartz,Material.QUARTZ);
			}

			//quartz_ore
			else if(itemstackcurrent.getType().equals(Material.QUARTZ_ORE)){
				playerdata.minestack.quartz_ore = giveMineStack(player,playerdata.minestack.quartz_ore,Material.QUARTZ_ORE);
			}
		}
	}
	//ランキングメニュー処理
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
		if(topinventory.getSize() != 36){
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
		}
	}
	//minestackの1stack付与の処理
	private int giveMineStack(Player player,int minestack,Material type){
		if(minestack >= 64){
			minestack -= 64;
			ItemStack itemstack = new ItemStack(type,64);
			if(!Util.isPlayerInventryFill(player)){
				Util.addItem(player,itemstack);
			}else{
				Util.dropItem(player,itemstack);
			}
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
		}else{
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
		}
		return minestack;
	}

	/*
	//プレイヤーがアクティブスキル選択インベントリを閉じた時に実行
	@EventHandler
	public void onPlayerActiveSkillSellectCloseEvent(InventoryCloseEvent event){
		HumanEntity he = event.getPlayer();
		Inventory inventory = event.getInventory();

		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}
		//インベントリサイズが36でない時終了
		if(inventory.getSize() != 36){
			return;
		}
		if(inventory.getTitle().equals(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "木の棒メニュー")){
			Player player = (Player)he;
			PlayerInventory pinventory = player.getInventory();
			ItemStack itemstack = pinventory.getItemInMainHand();
			if(itemstack.getType().equals(Material.STICK)){
				//閉まる音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_CLOSE, 1, (float) 0.1);
			}
		}
	}
	*/


}