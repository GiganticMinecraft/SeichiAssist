package com.github.unchama.seichiassist.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.MenuInventoryData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class PlayerInventoryListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Config config = SeichiAssist.config;

	/*
	//プレイヤーが4次元ポケットを閉じた時に実行
	@EventHandler
	public void onPlayerPortalCloseEvent(InventoryCloseEvent event){
		HumanEntity he = event.getPlayer();
		Inventory inventory = event.getInventory();

		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}
		//インベントリサイズが２７でない時終了
		if(inventory.getSize() != 27){
			return;
		}
		if(inventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "4次元ポケット")){
			Player player = (Player)he;
			PlayerInventory pinventory = player.getInventory();
			ItemStack itemstack = pinventory.getItemInMainHand();
			if(itemstack.getType().equals(Material.ENDER_PORTAL_FRAME)){
				//閉まる音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_CLOSE, 1, (float) 0.1);
			}
		}
	}
	*/

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
			}else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft")){
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));
				return;
			}else if(itemstackcurrent.getType().equals(Material.CHEST)){
				//レベルが足りない場合処理終了
				if( playerdata.level < SeichiAssist.config.getMineStacklevel()){
					player.sendMessage(ChatColor.GREEN + "整地レベルが"+SeichiAssist.config.getMineStacklevel()+ "以上必要です");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
					return;
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMineStackMenu(player));
				return;
			}

			else if(itemstackcurrent.getType().equals(Material.COAL_ORE)){
				if(playerdata.activenum == ActiveSkill.DUALBREAK.getNum()){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else if(playerdata.level >= config.getDualBreaklevel()){
					playerdata.activenum = ActiveSkill.DUALBREAK.getNum();
					player.sendMessage(ChatColor.GREEN + "アクティブスキル:デュアルブレイク  が選択されました");
					player.sendMessage(ChatColor.YELLOW + "アクティブスキルはピッケルorシャベルor斧を持った状態で\nShift(スニーク)+右クリックでスキルのONOFFを変更出来ます");
					playerdata.activemineflagnum = 1;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}else{
					player.sendMessage(ChatColor.RED + "必要整地レベルが足りません");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
				}
			}

			else if(itemstackcurrent.getType().equals(Material.IRON_ORE)){
				if(playerdata.activenum == ActiveSkill.TRIALBREAK.getNum()){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else if(playerdata.level >= config.getTrialBreaklevel() && playerdata.activenum != ActiveSkill.TRIALBREAK.getNum()){
					playerdata.activenum = ActiveSkill.TRIALBREAK.getNum();
					player.sendMessage(ChatColor.GREEN + "アクティブスキル:トリアルブレイク が選択されました");
					player.sendMessage(ChatColor.YELLOW + "アクティブスキルはピッケルorシャベルor斧を持った状態で\nShift(スニーク)+右クリックでスキルのONOFFを変更出来ます");

					playerdata.activemineflagnum = 1;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}else{
					player.sendMessage(ChatColor.RED + "必要整地レベルが足りません");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
				}
			}

			else if(itemstackcurrent.getType().equals(Material.GOLD_ORE)){
				if(playerdata.activenum == ActiveSkill.EXPLOSION.getNum()){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else if(playerdata.level >= config.getExplosionlevel() && playerdata.activenum != ActiveSkill.EXPLOSION.getNum()){
					playerdata.activenum = ActiveSkill.EXPLOSION.getNum();
					player.sendMessage(ChatColor.GREEN + "アクティブスキル:エクスプロージョン が選択されました");
					playerdata.activemineflagnum = 1;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}else{
					player.sendMessage(ChatColor.RED + "必要整地レベルが足りません");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
				}
			}

			else if(itemstackcurrent.getType().equals(Material.REDSTONE_ORE)){
				if(playerdata.activenum == ActiveSkill.THUNDERSTORM.getNum()){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else if(playerdata.level >= config.getThunderStormlevel() && playerdata.activenum != ActiveSkill.THUNDERSTORM.getNum()){
					playerdata.activenum = ActiveSkill.THUNDERSTORM.getNum();
					player.sendMessage(ChatColor.GREEN + "アクティブスキル:サンダーストーム が選択されました");
					playerdata.activemineflagnum = 1;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}else{
					player.sendMessage(ChatColor.RED + "必要整地レベルが足りません");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
				}
			}

			else if(itemstackcurrent.getType().equals(Material.LAPIS_ORE)){
				if(playerdata.activenum == ActiveSkill.BLIZZARD.getNum()){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else if(playerdata.level >= config.getBlizzardlevel() && playerdata.activenum != ActiveSkill.BLIZZARD.getNum()){
					playerdata.activenum = ActiveSkill.BLIZZARD.getNum();
					player.sendMessage(ChatColor.GREEN + "アクティブスキル:ブリザード が選択されました");
					playerdata.activemineflagnum = 1;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}else{
					player.sendMessage(ChatColor.RED + "必要整地レベルが足りません");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
				}
			}

			else if(itemstackcurrent.getType().equals(Material.EMERALD_ORE)){
				if(playerdata.activenum == ActiveSkill.METEO.getNum()){
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					player.sendMessage(ChatColor.YELLOW + "既に選択されています");
				}else if(playerdata.level >= config.getMeteolevel() && playerdata.activenum != ActiveSkill.METEO.getNum()){
					playerdata.activenum = ActiveSkill.METEO.getNum();
					player.sendMessage(ChatColor.GREEN + "アクティブスキル:メテオ が選択されました");
					playerdata.activemineflagnum = 1;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}else{
					player.sendMessage(ChatColor.RED + "必要整地レベルが足りません");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
				}
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


			//経験値を消費してプレイヤーの頭を召喚
			else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_Villager")){
				//経験値変更用のクラスを設定
				//経験値が足りなかったら処理を終了
				if(!expman.hasExp(10000)){
					player.sendMessage(ChatColor.RED + "必要な経験値が足りません");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
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
				player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "http://mc.seichi.click:8123");
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
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
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
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
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

			/*
			else if(itemstackcurrent.getType().equals(Material.DIAMOND_ORE)){
				if(playerdata.activenum == ActiveSkill.GRAVITY.getNum()){

				}else if(playerdata.level >= config.getGravitylevel() && playerdata.activenum != ActiveSkill.GRAVITY.getNum()){
					playerdata.activenum = ActiveSkill.GRAVITY.getNum();
					player.sendMessage(ChatColor.GREEN + "アクティブスキル:グラビティ");
					playerdata.activemineflagnum = 1;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
				}else{
					player.sendMessage(ChatColor.GREEN + "必要整地レベルが足りません。");
					player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
				}
			}
			*/

		}else if(topinventory.getTitle().equals(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
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

			else if(itemstackcurrent.getType().equals(Material.DIRT)){
				if(playerdata.minestack.dirt >= 64){
					playerdata.minestack.dirt -= 64;
					ItemStack itemstack = new ItemStack(Material.DIRT,64);
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,itemstack);
					}else{
						Util.dropItem(player,itemstack);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				}
			}

			else if(itemstackcurrent.getType().equals(Material.GRAVEL)){
				if(playerdata.minestack.gravel >= 64){
					playerdata.minestack.gravel -= 64;
					ItemStack itemstack = new ItemStack(Material.GRAVEL,64);
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,itemstack);
					}else{
						Util.dropItem(player,itemstack);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				}
			}

			else if(itemstackcurrent.getType().equals(Material.COBBLESTONE)){
				if(playerdata.minestack.cobblestone >= 64){
					playerdata.minestack.cobblestone -= 64;
					ItemStack itemstack = new ItemStack(Material.COBBLESTONE,64);
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,itemstack);
					}else{
						Util.dropItem(player,itemstack);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				}
			}

			else if(itemstackcurrent.getType().equals(Material.STONE)){
				if(playerdata.minestack.stone >= 64){
					playerdata.minestack.stone -= 64;
					ItemStack itemstack = new ItemStack(Material.STONE,64);
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,itemstack);
					}else{
						Util.dropItem(player,itemstack);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				}
			}
			else if(itemstackcurrent.getType().equals(Material.SAND)){
				if(playerdata.minestack.sand >= 64){
					playerdata.minestack.sand -= 64;
					ItemStack itemstack = new ItemStack(Material.SAND,64);
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,itemstack);
					}else{
						Util.dropItem(player,itemstack);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				}
			}
			else if(itemstackcurrent.getType().equals(Material.SANDSTONE)){
				if(playerdata.minestack.sandstone >= 64){
					playerdata.minestack.sandstone -= 64;
					ItemStack itemstack = new ItemStack(Material.SANDSTONE,64);
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,itemstack);
					}else{
						Util.dropItem(player,itemstack);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				}
			}
			else if(itemstackcurrent.getType().equals(Material.NETHERRACK)){
				if(playerdata.minestack.netherrack >= 64){
					playerdata.minestack.netherrack -= 64;
					ItemStack itemstack = new ItemStack(Material.NETHERRACK,64);
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,itemstack);
					}else{
						Util.dropItem(player,itemstack);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				}
			}
			else if(itemstackcurrent.getType().equals(Material.ENDER_STONE)){
				if(playerdata.minestack.ender_stone >= 64){
					playerdata.minestack.ender_stone -= 64;
					ItemStack itemstack = new ItemStack(Material.ENDER_STONE,64);
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,itemstack);
					}else{
						Util.dropItem(player,itemstack);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				}else{
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
				}
			}
		}
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


/*バグ確認のため未実装
	//インベントリに4次元ポケットを入れられないようにする。
	@EventHandler
	public void onPlayerClickPortalInventoryEvent(InventoryClickEvent event){
		ItemStack itemstackcursor = event.getCursor();
		ItemStack itemstackcurrent = event.getCurrentItem();
		Inventory inventory = event.getClickedInventory();

		if(inventory == null){
			return;
		}
		if(!inventory.getType().equals(InventoryType.PLAYER)){
			if(itemstackcursor.getType().equals(Material.ENDER_PORTAL_FRAME) || itemstackcurrent.getType().equals(Material.ENDER_PORTAL_FRAME)){
				event.setCancelled(true);
			}
		}

	}

	//ドロップできないようにする。
	@EventHandler
	public void onPlayerDropPortalInventoryEvent(PlayerDropItemEvent event){
		Item item = event.getItemDrop();
		ItemStack itemstack = item.getItemStack();
		if(itemstack.getType().equals(Material.ENDER_PORTAL_FRAME)){
			event.setCancelled(true);
		}
	}
*/

}