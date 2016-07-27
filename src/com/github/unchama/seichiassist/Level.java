package com.github.unchama.seichiassist;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.data.PlayerData;

public class Level{
	static private FireworkEffect.Type[] types = { FireworkEffect.Type.BALL,
		FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.BURST,
		FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR, };
	public static int calcPlayerLevel(Player player){
		//プレイヤー名を取得
		String name = Util.getName(player);
		//プレイヤーの統計値を取得
		int mines = 0;
		mines = MineBlock.calcMineBlock(player);
		//プレイヤーのデータを取得
		PlayerData playerdata = SeichiAssist.playermap.get(name);

		//現在のランクの次を取得
		int i = playerdata.level + 1;

		//ランクが上がらなくなるまで処理
		while(SeichiAssist.levellist.get(i).intValue() <= mines){
			playerdata.level = i;
			//レベルアップ時のメッセージ
			player.sendMessage(ChatColor.GOLD+"ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv("+(i-1)+")→Lv("+i+")】");
			//レベルアップ時の動作
			Location loc = player.getLocation();
			// 花火を作る
			Firework firework = loc.getWorld().spawn(loc, Firework.class);

			// 花火の設定情報オブジェクトを取り出す
			FireworkMeta meta = firework.getFireworkMeta();
			Builder effect = FireworkEffect.builder();
			Random rand = new Random();
			// 形状をランダムに決める
			effect.with(types[rand.nextInt(types.length)]);

			// 基本の色を単色～5色以内でランダムに決める
			effect.withColor(getRandomCrolors(1 + rand.nextInt(5)));

			// 余韻の色を単色～3色以内でランダムに決める
			effect.withFade(getRandomCrolors(1 + rand.nextInt(3)));

			// 爆発後に点滅するかをランダムに決める
			effect.flicker(rand.nextBoolean());

			// 爆発後に尾を引くかをランダムに決める
			effect.trail(rand.nextBoolean());

			// 打ち上げ高さを1以上4以内でランダムに決める
			meta.setPower(1 + rand.nextInt(4));

			// 花火の設定情報を花火に設定
			meta.addEffect(effect.build());
			firework.setFireworkMeta(meta);



			//パッシブスキル獲得レベルまできた時の処理
			if(SeichiAssist.passiveskillgetlevel.contains(playerdata.level)){
				playerdata.cangetpassiveskill++;
			}
			//アクティブスキル獲得レベルまできた時の処理
			if(SeichiAssist.activeskillgetlevel.contains(playerdata.level)){
				playerdata.cangetactiveskill++;
			}
			i++;
		}


		return playerdata.level;
	}

	public static void setDisplayName(int i,Player p) {
		String name =Util.getName(p);

		if(p.isOp()){
			//管理人の場合
			name = ChatColor.RED + "<管理人>" + name + ChatColor.WHITE;
		}
		name =  "[ Lv" + i + " ]" + name;
		p.setDisplayName(name);
		p.setPlayerListName(name);
	}

	public static void updata(Player player) {
		int level = Level.calcPlayerLevel(player);
		Level.setDisplayName(level, player);
	}

	public static void setLevel(String name, int level) {
		PlayerData playerdata = SeichiAssist.playermap.get(name);
		playerdata.level = level;
	}
	public static int getLevel(String name) {
		PlayerData playerdata = SeichiAssist.playermap.get(name);
		return playerdata.level;
	}

	public static void reloadLevel(String name) {
		for(Player p : SeichiAssist.plugin.getServer().getOnlinePlayers()){
			if(Util.getName(p).equals(name)){
				int level = SeichiAssist.playermap.get(name).level;
				setDisplayName(level,p);
			}
		}

	}
	private static Color[] getRandomCrolors(int length) {
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
}
