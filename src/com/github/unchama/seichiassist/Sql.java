package com.github.unchama.seichiassist;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import com.github.unchama.seichiassist.util.Util;

//MySQL操作関数
public class Sql{
	private SeichiAssist plugin;
	private final String url, db, id, pw;
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	public static String exc;

	//コンストラクタ
	Sql(SeichiAssist plugin ,String url, String db, String id, String pw){
		this.plugin = plugin;
		this.url = url;
		this.db = db;
		this.id = id;
		this.pw = pw;
	}

	/**
	 * 接続関数
	 *
	 * @param url 接続先url
	 * @param id ユーザーID
	 * @param pw ユーザーPW
	 * @param db データベースネーム
	 * @param table テーブルネーム
	 * @return
	 */
	public boolean connect(){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			e.printStackTrace();
			plugin.getLogger().info("Mysqlドライバーのインスタンス生成に失敗しました");
			return false;
		}
		//sql鯖への接続とdb作成
		if(!connectMySQL()){
			plugin.getLogger().info("SQL接続に失敗しました");
			return false;
		}
		if(!createDB()){
			plugin.getLogger().info("データベース作成に失敗しました");
			return false;
		}
		if(!connectDB()){
			plugin.getLogger().info("データベース接続に失敗しました");
			return false;
		}
		if(!createPlayerDataTable(SeichiAssist.PLAYERDATA_TABLENAME)){
			plugin.getLogger().info("playerdataテーブル作成に失敗しました");
			return false;
		}

		if(!createGachaDataTable(SeichiAssist.GACHADATA_TABLENAME)){
			plugin.getLogger().info("gachadataテーブル作成に失敗しました");
			return false;
		}

		return true;
	}

	private boolean connectMySQL(){
		try {
			if(stmt != null && !stmt.isClosed()){
				stmt.close();
				con.close();
			}
			con = (Connection) DriverManager.getConnection(url, id, pw);
			stmt = con.createStatement();
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    	return false;
		}
		return true;
	}

	/**
	 * コネクション切断処理
	 *
	 * @return 成否
	 */
	public boolean disconnect(){
	    if (con != null){
	    	try{
	    		stmt.close();
				con.close();
	    	}catch (SQLException e){
	    		e.printStackTrace();
	    		return false;
	    	}
	    }
	    return true;
	}

	//コマンド出力関数
	//@param command コマンド内容
	//@return 成否
	//@throws SQLException
	private boolean putCommand(String command){
		try {
			stmt.executeUpdate(command);
			return true;
		} catch (SQLException e) {
			//接続エラーの場合は、再度接続後、コマンド実行
			java.lang.System.out.println("接続に失敗しました");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * データベース作成
	 * 失敗時には変数excにエラーメッセージを格納
	 *
	 * @param table テーブル名
	 * @return 成否
	 */
	public boolean createDB(){
		if(db==null){
			return false;
		}
		String command;
		command = "CREATE DATABASE IF NOT EXISTS " + db
				+ " character set utf8 collate utf8_general_ci";
		return putCommand(command);
	}

	private boolean connectDB() {
		String command;
		command = "use " + db;
		return putCommand(command);
	}

	/**
	 * テーブル作成
	 * 失敗時には変数excにエラーメッセージを格納
	 *
	 * @param table テーブル名
	 * @return 成否
	 */
	public boolean createPlayerDataTable(String table){
		if(table==null){
			return false;
		}
		//テーブルが存在しないときテーブルを新規作成
		String command =
				"CREATE TABLE IF NOT EXISTS " + table +
				"(name varchar(30) unique," +
				"uuid varchar(128) unique)";
		if(!putCommand(command)){
			return false;
		}
		//必要なcolumnを随時追加
		command =
				"alter table " + table +
				" add column if not exists effectflag boolean default true" +
				",add column if not exists minestackflag boolean default true" +
				",add column if not exists messageflag boolean default false" +
				",add column if not exists activemineflagnum int default 0" +
				",add column if not exists activenum int default 1" +
				",add column if not exists gachapoint int default 0" +
				",add column if not exists gachaflag boolean default true" +
				",add column if not exists level int default 1" +
				",add column if not exists numofsorryforbug int default 0" +
				",add column if not exists inventory blob default null" +
				",add column if not exists rgnum int default 0" +
				",add column if not exists totalbreaknum int default 0" +
				",add column if not exists lastquit datetime default null" +
				",add column if not exists stack_dirt int default 0" +
				",add column if not exists stack_gravel int default 0" +
				",add column if not exists stack_cobblestone int default 0" +
				",add column if not exists stack_stone int default 0" +
				",add column if not exists stack_sand int default 0" +
				",add column if not exists stack_sandstone int default 0" +
				",add column if not exists stack_netherrack int default 0" +
				",add column if not exists stack_ender_stone int default 0" +
				",add column if not exists stack_grass int default 0" +
				",add column if not exists stack_quartz int default 0" +
				",add column if not exists stack_quartz_ore int default 0" +
				",add column if not exists stack_soul_sand int default 0" +
				",add column if not exists stack_magma int default 0" +
				",add column if not exists playtick int default 0" +
				",add column if not exists killlogflag boolean default false" +
				",add column if not exists pvpflag boolean default false" +
				",add index if not exists name_index(name)" +
				"";
		return putCommand(command);
	}

	public boolean createGachaDataTable(String table){
		if(table==null){
			return false;
		}
		//テーブルが存在しないときテーブルを新規作成
		String command =
				"CREATE TABLE IF NOT EXISTS " + table +
				"(id int auto_increment unique,"
				+ "amount int(11))";
		if(!putCommand(command)){
			return false;
		}
		//必要なcolumnを随時追加
		command =
				"alter table " + table +
				" add column if not exists probability double default 0.0" +
				",add column if not exists itemstack blob default null" +
				"";
		return putCommand(command);
	}

	public PlayerData loadPlayerData(Player p) {
		String name = Util.getName(p);
		UUID uuid = p.getUniqueId();
		String struuid = uuid.toString().toLowerCase();
		String command = "";
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
 		int count = -1;
 		//uuidがsqlデータ内に存在するか検索
 		//command:
 		//select count(*) from playerdata where uuid = 'struuid'
 		command = "select count(*) as count from " + table
 				+ " where uuid = '" + struuid + "'";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				   count = rs.getInt("count");
				  }
			rs.close();
		} catch (SQLException e) {
			exc = e.getMessage();
			return null;
		}

 		if(count == 0){
 			//uuidが存在しない時の処理

 			//新しくuuidとnameを設定し行を作成
 			//insert into playerdata (name,uuid) VALUES('unchima','UNCHAMA')
 			command = "insert into " + table
 	 				+ " (name,uuid) values('" + name
 	 				+ "','" + struuid + "')";
 			if(!putCommand(command)){
 				return null;
 			}
 			//PlayerDataを新規作成
 			return new PlayerData(p);

 		}else if(count == 1){
 			//uuidが存在するときの処理
 			if(SeichiAssist.DEBUG){
 				p.sendMessage("sqlにデータが保存されています。");
 			}

 			/*
 			//playernameをアップデート→廃止、ログアウト時にプレイヤーネームを更新するようにした
 			//update playerdata set name = 'uma' WHERE uuid like 'UNCHAMA'
 			command = "update " + table
 					+ " set name = '" + name
 					+ "' where uuid like '" + struuid + "'";
 			try{
 				stmt.executeUpdate(command);
 			} catch (SQLException e) {
 				exc = e.getMessage();
 				return null;
 			}
 			if(SeichiAssist.DEBUG){
 				p.sendMessage("sqlのプレイヤーネームを更新しました。");
 			}
 			*/

 			//PlayerDataを新規作成
 			PlayerData playerdata = new PlayerData(p);

 			//sqlデータから得られた値で更新

 			command = "select * from " + table
 					+ " where uuid like '" + struuid + "'";
 			try{
 				rs = stmt.executeQuery(command);
 				while (rs.next()) {
 					//各種数値
 	 				playerdata.effectflag = rs.getBoolean("effectflag");
 	 				playerdata.minestackflag = rs.getBoolean("minestackflag");
 	 				playerdata.messageflag = rs.getBoolean("messageflag");
 	 				playerdata.activemineflagnum = rs.getInt("activemineflagnum");
 	 				playerdata.activenum = rs.getInt("activenum");
 	 				playerdata.gachapoint = rs.getInt("gachapoint");
 	 				playerdata.gachaflag = rs.getBoolean("gachaflag");
 	 				playerdata.level = rs.getInt("level");
 	 				playerdata.numofsorryforbug = rs.getInt("numofsorryforbug");
 	 				playerdata.rgnum = rs.getInt("rgnum");
 	 				playerdata.inventory = BukkitSerialization.fromBase64(rs.getString("inventory").toString());
 	 				playerdata.dispkilllogflag = rs.getBoolean("killlogflag");
 	 				playerdata.pvpflag = rs.getBoolean("pvpflag");

 	 				//MineStack機能の数値
 	 				playerdata.minestack.dirt = rs.getInt("stack_dirt");
 	 				playerdata.minestack.gravel = rs.getInt("stack_gravel");
 	 				playerdata.minestack.cobblestone = rs.getInt("stack_cobblestone");
 	 				playerdata.minestack.stone = rs.getInt("stack_stone");
 	 				playerdata.minestack.sand = rs.getInt("stack_sand");
 	 				playerdata.minestack.sandstone = rs.getInt("stack_sandstone");
 	 				playerdata.minestack.netherrack = rs.getInt("stack_netherrack");
 	 				playerdata.minestack.ender_stone = rs.getInt("stack_ender_stone");
 	 				playerdata.minestack.grass = rs.getInt("stack_grass");
 	 				playerdata.minestack.quartz = rs.getInt("stack_quartz");
 	 				playerdata.minestack.quartz_ore = rs.getInt("stack_quartz_ore");
 	 				playerdata.minestack.soul_sand = rs.getInt("stack_soul_sand");
 	 				playerdata.minestack.magma = rs.getInt("stack_magma");
 				  }
 				rs.close();
 			} catch (SQLException | IOException e) {
 				exc = e.getMessage();
 				return null;
 			}
 			if(SeichiAssist.DEBUG){
 				p.sendMessage("sqlデータで更新しました。");
 			}
 			//更新したplayerdataを返す
 			return playerdata;
 		}else{
 			//mysqlに該当するplayerdataが2個以上ある時エラーを吐く
 			Bukkit.getLogger().info(Util.getName(p) + "のplayerdataがmysqlに2個以上ある為、正常にロード出来ませんでした");
 			p.sendMessage("独自機能のロードに失敗しました。管理人に報告して下さい");
 			return null;
 		}
	}
	public boolean savePlayerData(PlayerData playerdata) {
		//引数のplayerdataをsqlにデータを送信

		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		String command = "";

		command = "update " + table
				+ " set"

				//名前更新処理
				+ " name = '" + playerdata.name + "'"

				//各種数値更新処理
				+ ",effectflag = " + Boolean.toString(playerdata.effectflag)
				+ ",minestackflag = " + Boolean.toString(playerdata.minestackflag)
				+ ",messageflag = " + Boolean.toString(playerdata.messageflag)
				+ ",activemineflagnum = " + Integer.toString(playerdata.activemineflagnum)
				+ ",activenum = " + Integer.toString(playerdata.activenum)
				+ ",gachapoint = " + Integer.toString(playerdata.gachapoint)
				+ ",gachaflag = " + Boolean.toString(playerdata.gachaflag)
				+ ",level = " + Integer.toString(playerdata.level)
				+ ",numofsorryforbug = " + Integer.toString(playerdata.numofsorryforbug)
				+ ",rgnum = " + Integer.toString(playerdata.rgnum)
				+ ",totalbreaknum = " + Integer.toString(playerdata.totalbreaknum)
				+ ",inventory = '" + BukkitSerialization.toBase64(playerdata.inventory) + "'"
				+ ",playtick = " + Integer.toString(playerdata.playtick)
				+ ",lastquit = cast( now() as datetime )"
				+ ",killlogflag = " + Boolean.toString(playerdata.dispkilllogflag)
				+ ",pvpflag = " + Boolean.toString(playerdata.pvpflag)

				//MineStack機能の数値更新処理
				+ ",stack_dirt = " + Integer.toString(playerdata.minestack.dirt)
				+ ",stack_gravel = " + Integer.toString(playerdata.minestack.gravel)
				+ ",stack_cobblestone = " + Integer.toString(playerdata.minestack.cobblestone)
				+ ",stack_stone = " + Integer.toString(playerdata.minestack.stone)
				+ ",stack_sand = " + Integer.toString(playerdata.minestack.sand)
				+ ",stack_sandstone = " + Integer.toString(playerdata.minestack.sandstone)
				+ ",stack_netherrack = " + Integer.toString(playerdata.minestack.netherrack)
				+ ",stack_ender_stone = " + Integer.toString(playerdata.minestack.ender_stone)
				+ ",stack_grass = " + Integer.toString(playerdata.minestack.grass)
				+ ",stack_quartz = " + Integer.toString(playerdata.minestack.quartz)
				+ ",stack_quartz_ore = " + Integer.toString(playerdata.minestack.quartz_ore)
				+ ",stack_soul_sand = " + Integer.toString(playerdata.minestack.soul_sand)
				+ ",stack_magma = " + Integer.toString(playerdata.minestack.magma)

				+ " where uuid like '" + struuid + "'";

		return putCommand(command);
	}


	//ガチャデータロード
	public boolean loadGachaData(){
		String table = SeichiAssist.GACHADATA_TABLENAME;
		List<GachaData> gachadatalist = new ArrayList<GachaData>();
		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select * from " + table;
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				GachaData gachadata = new GachaData();
				Inventory inventory = BukkitSerialization.fromBase64(rs.getString("itemstack").toString());
				gachadata.itemstack = (inventory.getItem(0));
				gachadata.amount = rs.getInt("amount");
				gachadata.probability = rs.getDouble("probability");
				gachadatalist.add(gachadata);
				  }
			rs.close();
		} catch (SQLException | IOException e) {
			exc = e.getMessage();
			return false;
		}
 		SeichiAssist.gachadatalist.clear();
 		SeichiAssist.gachadatalist.addAll(gachadatalist);
 		return true;

	}

	//ガチャデータセーブ
	public boolean saveGachaData(){
		String table = SeichiAssist.GACHADATA_TABLENAME;

		//まずmysqlのガチャテーブルを初期化(中身全削除)
		String command = "truncate table " + table;
		if(!putCommand(command)){
			return false;
		}

		//次に現在のgachadatalistでmysqlを更新
		for(GachaData gachadata : SeichiAssist.gachadatalist){
			//Inventory作ってガチャのitemstackに突っ込む
			Inventory inventory = SeichiAssist.plugin.getServer().createInventory(null, 9*1);
			inventory.setItem(0,gachadata.itemstack);

			command = "insert into " + table + " (probability,amount,itemstack)"
					+ " values"
					+ "(" + Double.toString(gachadata.probability)
					+ "," + Integer.toString(gachadata.amount)
					+ ",'" + BukkitSerialization.toBase64(inventory) + "'"
					+ ")";
			if(!putCommand(command)){
				return false;
			}
		}
		return true;
	}

	//ランキング表示用に総破壊ブロック数のカラムだけ全員分引っ張る
	public boolean setRanking() {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		List<Integer> ranklist = SeichiAssist.ranklist;
		ranklist.clear();

		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select totalbreaknum from " + table
				+ " where 1 order by " + table + ".totalbreaknum desc";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				ranklist.add(rs.getInt(1));
				  }
			rs.close();
		} catch (SQLException e) {
			exc = e.getMessage();
			return false;
		}
 		return true;
	}

	//プレイヤーレベル全リセット
	public boolean resetAllPlayerLevel(){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "update " + table
				+ " set level = 1";
		return putCommand(command);
	}

	//全員に詫びガチャの配布
	public boolean addAllPlayerBug(int amount){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "update " + table
				+ " set numofsorryforbug = numofsorryforbug + " + amount;
		return putCommand(command);
	}

	//指定されたプレイヤーにガチャ券を送信する
	public boolean addPlayerBug(UUID uuid,int num) {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = uuid.toString();
		String command = "update " + table
				+ " set numofsorryforbug = numofsorryforbug + " + num
				+ " where uuid like '" + struuid + "'";
		return putCommand(command);
	}

	//指定プレイヤーの四次元ポケットの中身取得
	public Inventory selectInventory(UUID uuid){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = uuid.toString();
		Inventory inventory = null;
		String command = "select inventory from " + table
					+ " where uuid like '" + struuid + "'";
			try{
				rs = stmt.executeQuery(command);
				while (rs.next()) {
	 				inventory = BukkitSerialization.fromBase64(rs.getString("inventory").toString());
				  }
				rs.close();
			} catch (SQLException | IOException e) {
				exc = e.getMessage();
				return null;
			}
		return inventory;
	}

}