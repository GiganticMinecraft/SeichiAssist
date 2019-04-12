package com.github.unchama.seichiassist.bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class BungeeReceiver implements PluginMessageListener {
	private SeichiAssist plugin;

	public BungeeReceiver(SeichiAssist plugin) {
		this.plugin = plugin;
	}

	@Override
	public synchronized void onPluginMessageReceived(String channel, Player player, byte[] message) {
		// ストリームの準備
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		DataInputStream in = new DataInputStream(stream);
		try {
			String subchannel = in.readUTF();
			switch (subchannel) {
			case "GetLocation":
				getLocation(in.readUTF(), in.readUTF(), in.readUTF());
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getLocation(String servername, String uuid, String wanter) {
		// 受信UUIDからプレイヤーを特定
		Player p = plugin.getServer().getPlayer(UUID.fromString(uuid));
		// プレイヤーデータを取得
		PlayerData pd = SeichiAssist.playermap.get(UUID.fromString(uuid));

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			// 返却データの生成
			out.writeUTF("GetLocation");
			out.writeUTF(wanter);
			// プレイヤーの座標を返却
			out.writeUTF(p.getName() + ": 整地Lv" + Integer.toString(pd.level) + " (総整地量: " + String.format("%,d", pd.totalbreaknum) + ")");
			out.writeUTF("Server: " + servername + ", " + "World: " + p.getWorld().getName() + " (" + Integer.toString(p.getLocation().getBlockX()) + ", " + Integer.toString(p.getLocation().getBlockY()) + ", " + Integer.toString(p.getLocation().getBlockZ()) + ")");
		} catch (IOException e) {
			e.printStackTrace();
		}
		p.sendPluginMessage(plugin, "SeichiAssistBungee", b.toByteArray());
	}
}
