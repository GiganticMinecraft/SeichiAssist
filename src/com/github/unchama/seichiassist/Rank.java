package com.github.unchama.seichiassist;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.data.PlayerData;

public class Rank{
	public static int calcplayerRank(Player player){
		//プレイヤー名を取得
		String name = Util.getName(player);
		//プレイヤーの統計値を取得
		int mines = 0;
		mines = MineBlock.calcMineBlock(player);
		//プレイヤーのデータを取得
		PlayerData playerdata = SeichiAssist.playermap.get(name);

		//現在のランクの次を取得
		int i = playerdata.rank + 1;

		//ランクが上がらなくなるまで処理
		while(SeichiAssist.ranklist.get(i).intValue() <= mines){
			//レベルアップ時のメッセージ
			player.sendMessage(ChatColor.YELLOW+"ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv("+(i-1)+")→Lv("+i+")】");
			//レベルアップ時の動作

			//パッシブスキル獲得レベルまできた時の処理
			if(SeichiAssist.passiveskillgetrank.contains(playerdata.rank)){
				playerdata.cangetpassiveskill++;
			}
			//アクティブスキル獲得レベルまできた時の処理
			if(SeichiAssist.activeskillgetrank.contains(playerdata.rank)){
				playerdata.cangetactiveskill++;
			}
			i++;
		}

		playerdata.rank = i;
		return i;
	}

	public static void setDisplayName(int i,Player p) {
		String name = "[ Lv" + i + " ]" + Util.getName(p);

		if(name.equals("unchama") || name.equals("tar0ss") || name.equals("whitecat_haru")/* || name.equals("taaa150")*/){
			//管理人の場合
			name = ChatColor.RED + "<管理人>" + name + ChatColor.WHITE;
		}
		p.setDisplayName(name);
		p.setPlayerListName(name);
	}
}
