package com.github.unchama.multiseichieffect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class TestRunnable extends BukkitRunnable{

	//このクラス自身を表すインスタンス
	//public static TestRunnable instance;

	//private SeichiPlugin plugin;
	//public TestRunnable(SeichiPlugin plugin){
	//	this.plugin = plugin;
	//}

	//値の宣言
	private Player player;
	private int p_num;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	TestRunnable(Player _player) {
		// TODO 自動生成されたコンストラクター・スタブ

		//インスタンスにプレイヤー情報設定
		player = _player;

		//ログイン人数取得用関数初期化
		p_num = 0;
	}

	@Override
	public void run() {
		// スケジュールで実行する処理の内容をここに書きます。

		//タスクキル判定用ArrayListに自分の名前が無かったらこのスゲジュールを削除する
		if(!MultiSeichiEffect.tasks.contains(player)){
			cancel();
		}

		//ログイン人数を取得しp_numを決定
		p_num = ( Bukkit.getOnlinePlayers().size() - 1 )  * 1;

		//プレイヤーにポーションエフェクトを付加
		player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 110, p_num), true);
	}

	//@EventHandler
	//public void onplayerQuitEvent(PlayerQuitEvent event){
	//	this.cancel();
	//}
}
