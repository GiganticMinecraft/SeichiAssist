package com.github.unchama.seichiassist.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;

/**
 * 初見確認とプレイヤーデータのロードを行うタスク
 * 1回のみ処理されることを想定している
 * @author unchama coolpoco
 *
 */
public class CheckAlreadyExistPlayerDataTaskRunnable extends BukkitRunnable{

	private SeichiAssist plugin = SeichiAssist.plugin;
	private Config config = SeichiAssist.config;

	private Sql sql = SeichiAssist.sql;
	private Connection con = sql.con;
	private final String table = SeichiAssist.PLAYERDATA_TABLENAME;
	private String db = config.getDB();

	private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;

	private PlayerData playerData;
	private String name;
	private final UUID uuid;
	private final String struuid;
	private String command = "";
	private Statement stmt = null;
	private ResultSet rs = null;

	public CheckAlreadyExistPlayerDataTaskRunnable(PlayerData playerData) {
		this.playerData = playerData;
		name = playerData.name;
		uuid = playerData.uuid;
		struuid = uuid.toString().toLowerCase();
	}

	@Override
	public void run() {
		// TODO 自動生成されたメソッド・スタブ

		//対象プレイヤーがオフラインなら処理終了
		if(SeichiAssist.plugin.getServer().getPlayer(uuid) == null){
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + name + "はオフラインの為取得処理を中断");
			return;
		}
		//sqlコネクションチェック
		sql.checkConnection();
		//同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		int count = -1;
		//uuidがsqlデータ内に存在するか検索
		//command:
		//select count(*) from playerdata where uuid = 'struuid'
		command = "select count(*) as count from " + db + "." + table
				+ " where uuid = '" + struuid + "'";
		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				   count = rs.getInt("count");
				  }
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			e.printStackTrace();
			return;
		}

		if(count == 0){
			//uuidが存在しない時の処理
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + name + "は完全初見です。プレイヤーデータを作成します");
			//新しくuuidとnameを設定し行を作成
			//insert into playerdata (name,uuid) VALUES('unchima','UNCHAMA')
			command = "insert into " + db + "." + table
					+ " (name,uuid,loginflag) values('" + name
					+ "','" + struuid+ "','1')";
			try{
				if(stmt.executeUpdate(command) <= 0){
					return;
				}
			} catch (SQLException e) {
				java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
				e.printStackTrace();
				return;
			}
			//PlayerDataをplayermapへ格納
			playermap.put(uuid, playerData);

			//ログイン時init処理
			new PlayerDataUpdateOnJoinRunnable(playerData).runTaskTimer(plugin, 0, 20);

		}else if(count == 1){
			//uuidが存在するときの処理
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + name + "のプレイヤーデータ読み込み開始");
			new LoadPlayerDataTaskRunnable(playerData).runTaskTimerAsynchronously(plugin, 0, 20);
			new PlayerDataUpdateOnJoinRunnable(playerData).runTaskTimer(plugin, 0, 20);

		}else{
			//mysqlに該当するplayerdataが2個以上ある時エラーを吐く
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + name + "のplayerdata読込時に原因不明のエラー発生");
		}

	}

}
