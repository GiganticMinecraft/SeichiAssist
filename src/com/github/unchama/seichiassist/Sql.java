package com.github.unchama.seichiassist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

//MySQL操作関数
public class Sql{
	private SeichiAssist plugin;
	private final String url, db, table, id, pw;
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	public static String exc;

	//コンストラクタ
	Sql(SeichiAssist plugin ,String url, String db, String table, String id, String pw){
		this.plugin = plugin;
		this.url = url;
		this.db = db;
		this.id = id;
		this.pw = pw;
		this.table = table;
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
		if(!createTable()){
			plugin.getLogger().info("テーブル作成に失敗しました");
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
	public boolean createTable(){
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
				",add column if not exists messageflag boolean default false" +
				",add column if not exists gachapoint int default 0" +
				",add column if not exists lastgachapoint int default 0" +
				",add column if not exists level int default 1" +
				",add column if not exists numofsorryforbug int default 0" +
				",add column if not exists minutebefore int default 0" +
				",add column if not exists minuteafter int default 0" +
				",add column if not exists minuteincrease int default 0" +
				",add column if not exists halfbefore int default 0" +
				",add column if not exists halfafter int default 0" +
				",add column if not exists halfincrease int default 0" +
				",add column if not exists minespeedlv int default 0" +
				",add column if not exists level int default 0" +
				",add column if not exists activemineflag boolean default false" +
				"";
		return putCommand(command);
	}

	//選んだｷｰの値を取得できる（boolean)
	public boolean selectboolean(String name,String key){
		String command;
		boolean flag = false;
		//command:
		//SELECT key from playerdata where name = 'uma'
		command = "select " + key
				+ " as flag from " + table
				+ " where name = '" + name
				+ "'";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				   flag = rs.getBoolean(key);
				  }
		} catch (SQLException e) {
			exc = e.getMessage();
			return false;
		}
 		if(SeichiAssist.DEBUG){
 			plugin.getLogger().info("key"+"の値:" + flag);
 		}
		return flag;
	}

	//選んだｷｰの値を取得できる（int)
	public int selectint(String name,String key){
		String command;
		int num = 0;
		//command:
		//SELECT key from playerdata where name = 'uma'
		command = "select " + key
				+ " as num from " + table
				+ " where name = '" + name
				+ "'";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				   num = rs.getInt(key);
				  }
			if(SeichiAssist.DEBUG){
				plugin.getLogger().info("key:" + key + "の値"+num+"を取得しました");
			}
		} catch (SQLException e) {
			exc = e.getMessage();
			return 0;
		}
 		if(SeichiAssist.DEBUG){
 			plugin.getLogger().info("key"+"の値:" + num);
 		}
		return num;
	}
	//選んだｷｰの値を取得できる（string)
		public String selectstring(String name,String key){
			String command;
			String str = null;
			//command:
			//SELECT key from playerdata where name = 'uma'
			command = "select " + key
					+ " as string from " + table
					+ " where name = '" + name
					+ "'";
	 		try{
				rs = stmt.executeQuery(command);
				while (rs.next()) {
					   str = rs.getString(key);
					  }
			} catch (SQLException e) {
				exc = e.getMessage();
				return null;
			}
	 		if(SeichiAssist.DEBUG){
	 			plugin.getLogger().info("key"+"の値:" + str);
	 		}
			return str;
		}
	/**
	 * データの挿入・更新(playername)
	 * 失敗時には変数excにエラーメッセージを格納
	 *
	 * @param table テーブル名
	 * @param key カラム名
	 * @param uuid キャラのuuid
	 * @return 成否
	 */
	public boolean insertname(String name,UUID uuid){
		String command = "";
 		String struuid = uuid.toString();
 		int count = -1;

 		//command:
 		//select count(*) from playerdata where uuid = 'struuid'
 		command = "select count(*) as count from " + table
 				+ " where uuid = '" + struuid + "'";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				   count = rs.getInt("count");
				  }
		} catch (SQLException e) {
			exc = e.getMessage();
			return false;
		}
 		if(SeichiAssist.DEBUG){
 			plugin.getLogger().info("countの値:" + count);
 		}

 		if(count == 0){
 			//insert into playerdata (name,uuid) VALUES('unchima','UNCHAMA')
 			command = "insert into " + table
 	 				+ " (name,uuid) values('" + name
 	 				+ "','" + struuid + "')";
 		}else if(count == 1){
 			//update playerdata set name = 'uma' WHERE uuid like 'UNCHAMA'
 			command = "update " + table
 					+ "set name = '" + name
 					+ "' where uuid like '" + struuid + "'";
 		}else{
 			return false;
 		}
 		return putCommand(command);
	}

	/**
	 * データの挿入・更新(string)
	 * 失敗時には変数excにエラーメッセージを格納
	 *
	 * @param table テーブル名
	 * @param key カラム名
	 * @param s 挿入する文字列
	 * @param uuid キャラのuuid
	 * @return 成否
	 */
	public boolean insert(String key, String s, String name){
		String command = "";

		//command:
		//insert into @table(@key, uuid) values('@s', '@struuid')
		// on duplicate key update @key='@s'
		command = "insert into " +  table +
				"(name," + key + ") values('" +
				name + "', '" + s + "')" +
				" on duplicate key update " + key + "='" + s + "'";

		return putCommand(command);
	}
	/**
	 * データの挿入・更新(int)
	 * 失敗時には変数excにエラーメッセージを格納
	 *
	 * @param table テーブル名
	 * @param key カラム名
	 * @param s 挿入する文字列
	 * @param uuid キャラのuuid
	 * @return 成否
	 */
	public boolean insert(String key, int num, String name){
		String command = "";
		//command:
		//insert into @table(@key, uuid) values('@s', '@struuid')
		// on duplicate key update @key='@s'
		command = "insert into " +  table +
				"(name," + key + ") values('" +
				name + "', " + num + ")" +
				" on duplicate key update " + key + "= " + num;

		return putCommand(command);
	}

	/**
	 * データの挿入・更新(boolean)
	 * 失敗時には変数excにエラーメッセージを格納
	 *
	 * @param table テーブル名
	 * @param key カラム名
	 * @param s 挿入する文字列
	 * @param uuid キャラのuuid
	 * @return 成否
	 */
	public boolean insert(String key, boolean flag, String name){
		String command = "";

		//command:
		//insert into @table(@key, uuid) values('@s', '@struuid')
		// on duplicate key update @key='@s'
		command = "insert into " +  table +
				"(name," + key + ") values('" +
				name + "', " + flag + ")" +
				" on duplicate key update " + key + "= " + flag;

		return putCommand(command);
	}


	/*プレイヤーのuuidを検索し、なかったら行を追加する
	 * SELECT COUNT(*) FROM テーブル名 WHERE UUIDのカラム名  = "UUID代入"
	 * 上記のsql文でcountの返り血が0ならば、新規行作成insert
	 * uuidとプレイヤーネームを照合し、プレイヤーネームが違ってたら書き換えて(update)あげないといけない
	 * UPDATE テーブル名 SET カラム名playername=(新しいplayername) WHERE カラム名uuid=(uuid)
	 * playername で検索をかけて中の値を取得または更新
	 * select flag from テーブル名 WHERE playername = "プレイヤーの名前";
	 * update テーブル名 set flag = (newflagの値) WHERE Playername = "プレイヤーの名前";
	 * alter table test add column if not exists start int DEFAULT 1
	 */
	/**
	 * コマンド出力関数
	 * @param command コマンド内容
	 * @return 成否
	 * @throws SQLException
	 */
	private boolean putCommand(String command){
		try {
			stmt.executeUpdate(command);
			return true;
		} catch (SQLException e) {
			//接続エラーの場合は、再度接続後、コマンド実行
			java.lang.System.out.println("接続に失敗しました。");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
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
	//全ての表を1行ずつ取得する。
	public ResultSet getTable() {
		String command;
		//command:
		//SELECT * from playerdata
		command = "select * from " + table;
 		try{
			rs = stmt.executeQuery(command);

		} catch (SQLException e) {
			exc = e.getMessage();
			return null;
		}
 		return rs;
	}
	//与えられたｷｰを降順に最初の３つのみ取得する。
	//SELECT * FROM `playerdata` order by gachapoint desc limit 3
	public ResultSet getRanking(String key, int num){
		String command;
		//command:
		//SELECT * FROM `playerdata` order by gachapoint desc limit 3
		command = "select * from " + table
				+ " order by " + key
				+ " desc limit " + num;
 		try{
			rs = stmt.executeQuery(command);

		} catch (SQLException e) {
			exc = e.getMessage();
			return null;
		}
 		return rs;
	}
	//指定されたプレイヤー名が存在するか検索する。
	public boolean isExists(String name){
		String command = "";
 		int count = -1;

 		//command:
 		//select count(*) from playerdata where uuid = 'struuid'
 		command = "select count(*) as count from " + table
 				+ " where name = '" + name + "'";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				   count = rs.getInt("count");
				  }

		} catch (SQLException e) {
			exc = e.getMessage();
			return false;
		}
 		if(count == 1){
			return true;
		}else if (count == 0){
			return false;
		}else if(SeichiAssist.DEBUG){
			Util.sendEveryMessage("countの値が2以上または、取得に失敗しています。");
		}
		return false;
	}


}