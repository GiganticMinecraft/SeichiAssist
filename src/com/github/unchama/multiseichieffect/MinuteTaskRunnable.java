package com.github.unchama.multiseichieffect;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MinuteTaskRunnable extends BukkitRunnable{

	//このクラス自身を表すインスタンス
	//public static TestRunnable instance;

	//値の宣言
	private Player player;
	private Config config;
	private Effect effect;
	private Gacha gacha;

	//おあそびついか
	//private int lastlevel;


	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	MinuteTaskRunnable(Player _player,Config _config) {
		player = _player;
		config = _config;
		effect = new Effect(player,config);
		gacha = new Gacha(player,config);

		//おあそびついか
		//lastlevel = 0;
	}


	@Override
	public void run() {
		// スケジュールで実行する処理の内容をここに書きます。

		//タスクキル判定用ArrayListに自分の名前が無かったらこのスゲジュールを削除する
		if(!MultiSeichiEffect.playermap.containsKey(player)){
			cancel();
		}
		//総破壊数を計算
		effect.mineblock.setNow();
		effect.mineblock.setIncrease();

		//総破壊数によるeffectを計算
		effect.setMineblock();
		//ログイン人数によるeffectを計算
		effect.setPnum();

		//外部コマンド・プラグイン等による採掘速度上昇の検出
		effect.findOutEffect();
		//自作プラグイン内の上昇値計算
		effect.setMySum();
		//自作プラグインと外部プラグインの上昇値合算
		effect.setSum();

		//ポーション効果付与
		effect.addPotion();

		//ガチャ用採掘量データセット
		//gacha.setPoint(effect.mineblock.getIncrease());
		//ガチャ券付与
		//gacha.presentticket();

		//プレイヤーにメッセージ送信
		effect.sendEffectMessage();

		//lastの更新(最後にやろう)
		gacha.setLastPoint();
		effect.mineblock.setLast();
		effect.setLastSum();
		effect.setLastMySum();

		//おあそびついか
		//lastlevel = LJoinQuit.calcRankBorder2(player,lastlevel);
	}

}


