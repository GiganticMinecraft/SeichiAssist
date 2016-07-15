package com.github.unchama.multiseichieffect;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
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
	private double effect_p_num;
	private int last_mineblock;
	private int now_mineblock;
	private int minute_mineblock;
	private int sum_mineblock;
	private int last_sum_mineblock;
	private double effect_mineblock;
	private int effect_sum;
	private int last_effect_sum;
	private int now_sum;
	private int out_sum;
	private int effect_mysum;
	private int last_effect_mysum;
	private int now_dulation;


	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	TestRunnable(Player _player) {
		// TODO 自動生成されたコンストラクター・スタブ

		//インスタンスにプレイヤー情報設定
		player = _player;

		//関数初期化
		p_num = 0;
		effect_p_num = 0.0;
		last_mineblock = (int)player.getStatistic(Statistic.MINE_BLOCK, Material.STONE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.NETHERRACK)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.DIRT)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.GRAVEL)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.LOG)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.LOG_2)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.GRASS)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.COAL_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.IRON_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.GOLD_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.LAPIS_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.EMERALD_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.REDSTONE_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.SAND)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.SANDSTONE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.QUARTZ_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.END_BRICKS)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.ENDER_STONE);
		now_mineblock = 0;
		minute_mineblock = 0;
		sum_mineblock = 0;
		last_sum_mineblock = 0;
		effect_mineblock = 0.0;
		effect_sum = 0;
		last_effect_sum = -1;
		now_sum = -1;
		out_sum = 0;
		effect_mysum = 0;
		last_effect_mysum = 0;
		now_dulation = -1;

	}

	@Override
	public void run() {
		// スケジュールで実行する処理の内容をここに書きます。

		//タスクキル判定用ArrayListに自分の名前が無かったらこのスゲジュールを削除する
		if(!MultiSeichiEffect.tasks.contains(player)){
			cancel();
		}

		//effect_mineblockを計算
		now_mineblock = (int)player.getStatistic(Statistic.MINE_BLOCK, Material.STONE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.NETHERRACK)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.DIRT)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.GRAVEL)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.LOG)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.LOG_2)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.GRASS)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.COAL_ORE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.IRON_ORE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.GOLD_ORE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.LAPIS_ORE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.EMERALD_ORE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.REDSTONE_ORE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.SAND)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.SANDSTONE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.QUARTZ_ORE)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.END_BRICKS)
					  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.ENDER_STONE);
		minute_mineblock = now_mineblock - last_mineblock;
		
		sum_mineblock += minute_mineblock;

		/*
		if(sum_mineblock > 1000){
			sum_mineblock -= 1000;
			
			ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
			SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
			skull.setDurability((short) 3);
			skullMeta.setDisplayName("ガチャ券");
			skullMeta.setOwner("unchama");
			skull.setItemMeta(skullMeta);
			if(!player.getInventory().contains(skull) && player.getInventory().firstEmpty()== -1){
				player.getWorld().dropItemNaturally(player.getLocation(), skull);
				player.sendMessage("あなたのガチャ券地べたに置いたわよ忘れるんじゃないよ");
			}else{
				player.getInventory().addItem(skull);
				player.sendMessage("ガチャ券プレゼントフォーユー");
			}
		}else if(last_sum_mineblock != sum_mineblock){
			player.sendMessage("あと"+ (1000 - sum_mineblock) + "ブロック整地するとガチャ券獲得ダヨ");
		}else{
			player.sendMessage("あくしろはたらけ");
		}
		*/
		
		effect_mineblock = (double)minute_mineblock / 100.0;

		//effect_p_numを計算
		p_num = Bukkit.getOnlinePlayers().size();
		effect_p_num = (double)p_num / 2.0;
		
		//外部コマンド・プラグイン等による採掘速度上昇の検出
		if(player.hasPotionEffect(PotionEffectType.FAST_DIGGING)){
			//既に採掘効果上昇がかかっている場合
			//player.sendMessage("うえ-1");
			PotionEffect[] potioneffect = player.getActivePotionEffects().toArray(new PotionEffect[0]);
			for( PotionEffect pe : potioneffect){
				//採掘速度上昇のエフェクトのデータ抽出
				//player.sendMessage("うえ0");
				if(pe.getType().equals(PotionEffectType.FAST_DIGGING)){
					//上昇値
					now_sum = pe.getAmplifier();
					//持続時間
					now_dulation = pe.getDuration();
					//player.sendMessage("now_sumとnow_dulationの値取得官僚" + now_sum + "-" +now_dulation);
					break;
				}
			}
		}else{
			//採掘効果がない場合
			//player.sendMessage("した-1");
			now_sum = -1;
			out_sum = 0;
			now_dulation = -1;
		}
		
		//自作プラグイン内の上昇値
		effect_mysum = (int)(effect_p_num + effect_mineblock - 1);
		
		//自作プラグインと外部プラグインの上昇値合算
		if(now_sum == -1 || (int)(now_sum - last_effect_mysum) == (int)out_sum){
			//自作プラグインの上昇値のみが反映される場合叉は外部プラグインの持続時間が残っている場合
			//player.sendMessage("うえ");
			effect_sum = out_sum + effect_mysum;
		}else{
			//外部プラグインの上昇値を検出した場合
			//player.sendMessage("した");
			effect_sum = effect_mysum + now_sum;
			out_sum = now_sum;
		}
		
		//上昇値の例外判定
		if(effect_sum < 0){
			effect_sum = 0;
		}
		
		//外部プラグインの上昇値が存在するときの場合分け
		if(now_dulation == -1){
			//外部プラグインの上昇値が存在しないとき
			//player.sendMessage("うえ２");
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 1200, effect_sum, false, false), true);
		}else{
			//外部プラグインの上昇値が存在するとき
			//player.sendMessage("した２");
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, now_dulation, effect_sum, false, false), true);
		}

		//effect_sumの値が変わってたらお知らせする
		if(last_effect_sum != effect_sum){
			player.sendMessage("採掘速度上昇レベルが" + (effect_sum + 1) + "になりました。");
			player.sendMessage("内訳:接続人数(" + p_num + "人)→上昇値(" + effect_p_num + ")");
			player.sendMessage("    1分間のブロック破壊数(" + minute_mineblock + "個)→上昇値(" + effect_mineblock + ")");
			if(out_sum != 0){
				player.sendMessage("    外部からの上昇量(" + out_sum + ")→上昇値(" + out_sum + ")");
			}
		}

		//lastの更新(最後にやろう)
		last_mineblock = now_mineblock;
		last_effect_sum = effect_sum;
		last_effect_mysum = effect_mysum;
		last_sum_mineblock = sum_mineblock;
	}

}
