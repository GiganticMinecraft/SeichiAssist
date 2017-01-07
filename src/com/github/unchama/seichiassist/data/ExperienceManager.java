package com.github.unchama.seichiassist.data;

import org.bukkit.entity.Player;

// 下記計算式に基づく（日本wikiと計算式が異なる）
// http://minecraft.gamepedia.com/Experience#Leveling_up
// 軽く確認したところ多分英語wikiがあってる、日本wikiの計算式はBeta1.8までの計算式
// 計算式を扱うため、誤差は保証出来ない
public class ExperienceManager {
	private Player player;

	private static enum CF {
		// Total Experience = [Level]^2 + 6[Level] (at levels 0-16)
		// Experience Required = 2[Current Level] + 7 (at levels 0-16)
		C1(1.0, 6.0, 0, 2, 7),
		// Total Experience = 2.5[Level]^2 - 40.5[Level] + 360 (at levels 17-31)
		// Experience Required = 5[Current Level] - 38 (at levels 17-31)
		C2(2.5, -40.5, 360, 5, -38),
		// Total Experience = 4.5[Level]^2 - 162.5[Level] + 2220 (at level 32+)
		// Experience Required = 9[Current Level] - 158 (at level 32+)
		C3(4.5, -162.5, 2220, 9, -158);
		public double ta, tb;
		public int tc, rb, rc;

		private CF(double ta, double tb, int tc, int rb, int rc) {
			this.ta = ta;
			this.tb = tb;
			this.tc = tc;
			this.rb = rb;
			this.rc = rc;
		}

		public static CF lv(int level) {
			if (level < 17) {
				return C1;
			} else if (32 <= level) {
				return C3;
			} else {
				return C2;
			}
		}

		public static CF exp(int total) {
			if (total < 352) {
				return C1;
			} else if (1507 <= total) {
				return C3;
			} else {
				return C2;
			}
		}
	}

	public ExperienceManager(Player player) {
		this.player = player;
	}

	public int getTotalExperience() {
		// 現在のレベル
		int level = player.getLevel();
		// レベル別係数格納
		CF cf = CF.lv(level);

		// 合計経験値
		int total = (int) Math.ceil(cf.ta * Math.pow(level, 2) + cf.tb * level + cf.tc);
		// 次のレベルまでの必要経験値
		int required = cf.rb * level + cf.rc;
		// 経験パーセンテージ
		double exp = Double.parseDouble(Float.toString(player.getExp()));
		// 現在の総経験値を計算
		return total + (int) Math.ceil(exp * required);
	}

	public void setTotalExperience(int total) {
		// レベル別係数格納
		CF cf = CF.exp(total);

		// 解の公式から正結果を切り捨て整数で取得…現在のレベル
		int level = (int) Math.floor((-cf.tb + Math.sqrt(Math.pow(cf.tb, 2) - (4 * cf.ta * (cf.tc - total)))) / (2 * cf.ta));
		// 現在のレベルまでに必要な経験値を算出
		int base = (int) (cf.ta * Math.pow(level, 2) + cf.tb * level + cf.tc);
		// 現在レベルで獲得した経験値を算出
		int current = total - base;
		// レベルアップに必要な経験値を算出
		int required = cf.rb * level + cf.rc;
		// 現在の経験パーセンテージ計算
		float exper = (float) current / required;
		// レベルと経験パーセンテージを設定
		player.setLevel(level);
		player.setExp(exper);
	}
}
