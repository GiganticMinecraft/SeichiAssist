package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import net.coreprotect.model.Config;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class PlayerJoinListener implements Listener {
	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	private Sql sql = SeichiAssist.sql;

	// プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event) {
		// プレイヤー取得
		Player player = event.getPlayer();

		// プレイヤーデータ作成
		// 新しく作成したPlayerDataを引数とする
		sql.loadPlayerData(new PlayerData(player));

		// 初見さんへの処理

		if(!player.hasPlayedBefore()){
			//初見さんであることを全体告知
			Util.sendEveryMessage(ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+player.getName()+"さんはこのサーバに初めてログインしました！");
			Util.sendEveryMessage(ChatColor.WHITE + "Wikiはもう読みましたか？→" + ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "http://seichi.click");
			Util.sendEverySound(Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			//初見プレイヤーに木の棒、エリトラ、ピッケルを配布
			player.getInventory().addItem(new ItemStack(Material.STICK));
			player.getInventory().addItem(new ItemStack(Material.ELYTRA));
			player.getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE));
			player.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE));
			player.sendMessage("初期装備を配布しました。Eキーで確認してネ");
			//メビウスおひとつどうぞ
			MebiusListener.give(player);
			//初見さんにLv1メッセージを送信
			player.sendMessage(SeichiAssist.config.getLvMessage(1));
		}

	}

	// プレイヤーがワールドを移動したとき
	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		// 整地ワールドから他のワールドに移動したとき
		if (SeichiAssist.ignoreWorldlist.contains(event.getFrom().getName())) {
			Player p = event.getPlayer();
			PlayerData pd = playermap.get(p.getUniqueId());

			// coreprotectを切る
			// inspectマップにtrueで登録されている場合
			if (net.coreprotect.model.Config.inspecting.get(p.getName()) != null
					&& net.coreprotect.model.Config.inspecting.get(p.getName())) {
				// falseに変更する
				p.sendMessage("§3CoreProtect §f- Inspector now disabled.");
				Config.inspecting.put(p.getName(), Boolean.valueOf(false));
			}

			// アサルトスキルを切る
			// 現在アサルトスキルorアサルトアーマーを選択中
			if (pd.activeskilldata.assaultnum >= 4 && pd.activeskilldata.assaulttype >= 4) {
				// アクティブスキルがONになっている
				if (pd.activeskilldata.mineflagnum != 0) {
					// メッセージを表示
					p.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(pd.activeskilldata.assaulttype, pd.activeskilldata.assaultnum) + "：OFF");
					// 内部状態をアサルトOFFに変更
					pd.activeskilldata.updataAssaultSkill(p, pd.activeskilldata.assaulttype, pd.activeskilldata.assaultnum, 0);
					// トグル音を鳴らす
					p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				}
			}
		}
	}
}
