package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.Util;




public class PlayerData {
	//プレイヤー名
	public String name;
	//UUID
	public UUID uuid;
	//エフェクトのフラグ
	public boolean effectflag;
	//内訳メッセージを出すフラグ
	public boolean messageflag;
	//1分間のデータを保存するincrease:１分間の採掘量
	public MineBlock minuteblock;
	//３０分間のデータを保存する．
	public MineBlock halfhourblock;
	//ガチャの基準となるポイント
	public int gachapoint;
	//最後のガチャポイントデータ
	public int lastgachapoint;
	//ガチャ受け取りフラグ
	public boolean gachaflag;
	//今回の採掘速度上昇レベルを格納
	public int minespeedlv;
	//前回の採掘速度上昇レベルを格納
	public int lastminespeedlv;
	//持ってるポーションエフェクト全てを格納する．
	public List<EffectData> effectdatalist;
	//現在のプレイヤーレベル
	public int level;
	//詫び券をあげる数
	public int numofsorryforbug;
	//採掘用アクティブスキルのフラグ 0:なし 1:上破壊 2:下破壊
	public int activemineflagnum;
	//拡張インベントリ
	public Inventory inventory;
	//アクティブスキル番号を格納
	public int activenum;
	//スキルクールダウン用フラグ
	public boolean skillcanbreakflag;
	//ワールドガード保護自動設定用
	public int rgnum;
	//ランキング算出用トータル破壊ブロック
	public int totalbreaknum;
	//スキル発動中だけtrueになるフラグ
	public boolean skillflag;
	//MineStack
	public MineStack minestack;
	//MineStackFlag
	public boolean minestackflag;
	//プレイ時間
	public int playtick;
	//キルログ表示トグル
	public boolean dispkilllogflag;
	//PvPトグル
	public boolean pvpflag;



	public PlayerData(Player player){
		//初期値を設定
		name = Util.getName(player);
		uuid = player.getUniqueId();
		effectflag = true;
		messageflag = false;
		minuteblock = new MineBlock();
		halfhourblock = new MineBlock();
		gachapoint = 0;
		lastgachapoint = 0;
		gachaflag = true;
		minespeedlv = 0;
		lastminespeedlv = 0;
		effectdatalist = new ArrayList<EffectData>();
		level = 1;
		numofsorryforbug = 0;
		activemineflagnum = 0;
		inventory = SeichiAssist.plugin.getServer().createInventory(null, 9*1 ,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "4次元ポケット");
		activenum = 1;
		skillcanbreakflag = true;
		rgnum = 0;
		totalbreaknum = Util.calcMineBlock(player);
		skillflag = false;
		minestack = new MineStack();
		minestackflag = true;
		playtick = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK);
		dispkilllogflag = false;
		pvpflag = false;
	}

	//プレイヤーデータを最新の状態に更新
	public void updata(Player player,int mines) {
		//破壊量データ(before)を設定
		minuteblock.before = mines;
		halfhourblock.before = mines;
		levelupdata(player,mines);
	}
	//詫び券の配布
	public void giveSorryForBug(Player player){
		ItemStack skull = Util.getskull(Util.getName(player));
		int count = 0;
		while(numofsorryforbug >= 1){
			numofsorryforbug -= 1;
			if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
				Util.addItem(player,skull);
			}else{
				Util.dropItem(player,skull);
			}
			count++;
		}
		//詫びガチャ関数初期化
		numofsorryforbug = 0;

		if(count > 0){
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			player.sendMessage(ChatColor.GREEN + "運営チームから"+count+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "を受け取りました");
		}
	}

	//詫びガチャの通知
	public void NotifySorryForBug(Player player){
		if(numofsorryforbug > 0){
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			player.sendMessage(ChatColor.GREEN + "運営チームから"+numofsorryforbug+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "が届いています！\n木の棒メニューから受け取ってください。");
		}
	}

	//エフェクトデータのdurationを60秒引く
	public void calcEffectData() {
		//tmplistを作成
		List<EffectData> tmplist = new ArrayList<EffectData>();
		//effectdatalistのdurationをすべて60秒（1200tick）引いてtmplistに格納
		for(EffectData ed : effectdatalist){
			ed.duration -= 1200;
			tmplist.add(ed);
		}
		//tmplistのdurationが3秒以下（60tick）のものはeffectdatalistから削除
		for(EffectData ed : tmplist){
			if(ed.duration <= 60){
				effectdatalist.remove(ed);
			}
		}
	}

	//プレイヤーネームを更新
	public void renewName(Player new_player) {
		//現在のplayername を取得
				String now_name = Util.getName(new_player);
				//UUIDは同じだがplayernameが異なっているとき
				if(!now_name.equals(name)){
					name = now_name;
				}
	}


	//オフラインかどうか
	public boolean isOffline() {
		return SeichiAssist.plugin.getServer().getPlayer(name) == null;
	}


	//レベルを更新
	public void levelupdata(Player p,int mines) {
		calcPlayerLevel(p,mines);
		setDisplayName(p);
	}


	//プレイヤーのレベルを指定された値に設定
	public void setLevel(int _level) {
		level = _level;
	}


	//表示される名前に整地レベルを追加
	public void setDisplayName(Player p) {
		String displayname = Util.getName(p);
		if(p.isOp()){
			//管理人の場合
			displayname = ChatColor.RED + "<管理人>" + name;
		}
		displayname =  "[ Lv" + level + " ]" + displayname + ChatColor.WHITE;

		p.setDisplayName(displayname);
		p.setPlayerListName(displayname);
	}


	//プレイヤーレベルを計算し、更新する。
	private void calcPlayerLevel(Player p,int mines){
		//現在のランクの次を取得
		int i = level + 1;
		//ランクが上がらなくなるまで処理
		while(SeichiAssist.levellist.get(i).intValue() <= mines && i <= SeichiAssist.levellist.size()){

			//レベルアップ時のメッセージ
			p.sendMessage(ChatColor.GOLD+"ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv("+(i-1)+")→Lv("+i+")】");
			//レベルアップ時の花火の打ち上げ
			Location loc = p.getLocation();
			Util.launchFireWorks(loc);
			String lvmessage = SeichiAssist.config.getLvMessage(i);
			if(!(lvmessage.isEmpty())){
				p.sendMessage(ChatColor.AQUA+lvmessage);
			}

			i++;
		}
		level = i-1;
	}

	//パッシブスキルの獲得量表示
	public int dispPassiveExp() {
		if(level < 8){
			return 0;
		}else if (level < 18){
			return SeichiAssist.config.getDropExplevel(1);
		}else if (level < 28){
			return SeichiAssist.config.getDropExplevel(2);
		}else if (level < 38){
			return SeichiAssist.config.getDropExplevel(3);
		}else if (level < 48){
			return SeichiAssist.config.getDropExplevel(4);
		}else if (level < 58){
			return SeichiAssist.config.getDropExplevel(5);
		}else if (level < 68){
			return SeichiAssist.config.getDropExplevel(6);
		}else if (level < 78){
			return SeichiAssist.config.getDropExplevel(7);
		}else if (level < 88){
			return SeichiAssist.config.getDropExplevel(8);
		}else if (level < 98){
			return SeichiAssist.config.getDropExplevel(9);
		}else{
			return SeichiAssist.config.getDropExplevel(10);
		}
	}
	//四次元ポケットのサイズを取得
	public int getPocketSize() {
		if (level < 26){
			return 9*3;
		}else if (level < 36){
			return 9*3;
		}else if (level < 46){
			return 9*3;
		}else if (level < 56){
			return 9*4;
		}else if (level < 66){
			return 9*5;
		}else{
			return 9*6;
		}
	}
}
