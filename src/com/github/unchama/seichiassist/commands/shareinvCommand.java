package com.github.unchama.seichiassist.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.SerializeItemList;
import com.github.unchama.seichiassist.util.Util;

import net.md_5.bungee.api.ChatColor;

public class shareinvCommand implements TabExecutor {
	public shareinvCommand(SeichiAssist plugin) {
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			// プレイヤーからの送信でない時処理終了
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください");
			return true;
		}
		shareInv((Player) sender);
		return true;
	}

	public void shareInv(Player player) {
		PlayerData playerdata = SeichiAssist.playermap.get(player.getUniqueId());
		Sql sql = SeichiAssist.sql;

		ItemStack air = new ItemStack(Material.AIR);
		// 収納中なら取り出す
		if (playerdata.shareinv) {
			String serial = sql.loadShareInv(player, playerdata);
			if (serial == "") {
				player.sendMessage(ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "収納アイテムが存在しません。");
			} else if (serial != null) {
				PlayerInventory pi = player.getInventory();
				List<ItemStack> items = SerializeItemList.fromBase64(serial);

				// 取得完了見込みにより現所持アイテムをドロップ＆クリア
				ItemStack offhand = pi.getItemInOffHand();
				if (offhand != null && !offhand.getType().equals(Material.AIR)) {
					Util.dropItem(player, offhand);
				}
				pi.setItemInOffHand(air);

				ItemStack[] armor = pi.getArmorContents();
				for (int cnt = 0; cnt < armor.length; cnt++) {
					if (armor[cnt] != null && !armor[cnt].getType().equals(Material.AIR)) {
						Util.dropItem(player, armor[cnt]);
					}
					armor[cnt] = air;
				}
				pi.setArmorContents(armor);

				ItemStack[] contents = pi.getStorageContents();
				for (int cnt = 0; cnt < contents.length; cnt++) {
					if (contents[cnt] != null && !contents[cnt].getType().equals(Material.AIR)) {
						Util.dropItem(player, contents[cnt]);
					}
					contents[cnt] = air;
				}
				pi.setStorageContents(contents);

				// 収納アイテムを分割
				offhand = items.get(0);
				armor = items.subList(1, 5).toArray(new ItemStack[0]);
				contents = items.subList(5, items.size()).toArray(new ItemStack[0]);

				// アイテムを取り出し
				pi.setItemInOffHand(offhand);
				pi.setArmorContents(armor);
				pi.setStorageContents(contents);
				// SQLデータをクリア
				sql.clearShareInv(player, playerdata);
				playerdata.shareinv = false;
				player.sendMessage(ChatColor.GREEN + "アイテムを取得しました。手持ちにあったアイテムはドロップしました。");
				Bukkit.getLogger().info(Util.getName(player) + "がアイテム取り出しを実施(SQL送信成功)");
			}
		}
		// 収納処理
		else {
			PlayerInventory pi = player.getInventory();
			List<ItemStack> items = new ArrayList<ItemStack>();

			// アイテム一覧をリストに取り出す
			ItemStack offhand = pi.getItemInOffHand();
			items.add(offhand);
			ItemStack[] armor = pi.getArmorContents();
			items.addAll(Arrays.asList(armor));
			ItemStack[] contents = pi.getStorageContents();
			items.addAll(Arrays.asList(contents));

			// アイテム一覧をシリアル化する
			String serial = SerializeItemList.toBase64(items);
			if (serial == "") {
				// 収納失敗
				player.sendMessage(ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "収納アイテムの変換に失敗しました。");
			} else {
				if (sql.saveShareInv(player, playerdata, serial)) {
					// 収納成功により現所持アイテムを全て削除
					pi.setItemInOffHand(air);
					for (int cnt = 0; cnt < armor.length; cnt++) {
						armor[cnt] = air;
					}
					pi.setArmorContents(armor);
					for (int cnt = 0; cnt < contents.length; cnt++) {
						contents[cnt] = air;
					}
					pi.setStorageContents(contents);

					// インベントリ共有ボタンをトグル
					playerdata.shareinv = true;
					player.sendMessage(ChatColor.GREEN + "アイテムを収納しました。10秒以上あとに、手持ちを空にして取り出してください。");
					Bukkit.getLogger().info(Util.getName(player) + "がアイテム収納を実施(SQL送信成功)");
					// 木の棒を取得
					player.chat("/stick");
				}
			}
		}
	}
}
