package com.github.unchama.seichiassist.database.manipulators;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RankData;
import com.github.unchama.seichiassist.database.DatabaseConstants;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.task.CheckAlreadyExistPlayerDataTask;
import com.github.unchama.seichiassist.task.CoolDownTask;
import com.github.unchama.seichiassist.task.PlayerDataSaveTask;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.util.ActionStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

import static com.github.unchama.util.ActionStatus.Fail;

public class PlayerDataManipulator {
    private final DatabaseGateway gateway;
    private final SeichiAssist plugin = SeichiAssist.instance;

    public PlayerDataManipulator(DatabaseGateway gateway) {
        this.gateway = gateway;
    }

    private String getTableReference() {
        return gateway.databaseName + "." + DatabaseConstants.PLAYERDATA_TABLENAME;
    }

    private int ifCoolDownDoneThenGet(final Player player,
                                        final PlayerData playerdata,
                                        final Supplier<Integer> supplier) {
        //連打による負荷防止の為クールダウン処理
        if(!playerdata.votecooldownflag){
            player.sendMessage(ChatColor.RED + "しばらく待ってからやり直してください");
            return 0;
        }
        new CoolDownTask(player,true,false,false).runTaskLater(plugin,1200);

        return supplier.get();
    }

    //投票特典配布時の処理(p_givenvoteの値の更新もココ)
    public int compareVotePoint(Player player, final PlayerData playerdata){
        return ifCoolDownDoneThenGet(player, playerdata, () -> {
            final String struuid = playerdata.uuid.toString();

            int p_vote = 0;
            int p_givenvote = 0;

            String command = "select p_vote,p_givenvote from " + getTableReference() + " where uuid = '" + struuid + "'";
            try (ResultSet lrs = gateway.executeQuery(command)) {
                while (lrs.next()) {
                    p_vote = lrs.getInt("p_vote");
                    p_givenvote = lrs.getInt("p_givenvote");
                }
            } catch (SQLException e) {
                java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "投票特典の受け取りに失敗しました");
                return 0;
            }
            //比較して差があればその差の値を返す(同時にp_givenvoteも更新しておく)
            if(p_vote > p_givenvote){
                command = "update " + getTableReference()
                        + " set p_givenvote = " + p_vote
                        + " where uuid like '" + struuid + "'";
                if (gateway.executeUpdate(command) == Fail) {
                    player.sendMessage(ChatColor.RED + "投票特典の受け取りに失敗しました");
                    return 0;
                }

                return p_vote - p_givenvote;
            }
            player.sendMessage(ChatColor.YELLOW + "投票特典は全て受け取り済みのようです");
            return 0;
        });
    }

    //最新のnumofsorryforbug値を返してmysqlのnumofsorrybug値を初期化する処理
    public int givePlayerBug(Player player,final PlayerData playerdata) {
        return ifCoolDownDoneThenGet(player, playerdata, () -> {
            String struuid = playerdata.uuid.toString();
            int numofsorryforbug = 0;

            String command = "select numofsorryforbug from " + getTableReference() + " where uuid = '" + struuid + "'";
            try (ResultSet lrs = gateway.executeQuery(command)) {
                while (lrs.next()) {
                    numofsorryforbug = lrs.getInt("numofsorryforbug");
                }
            } catch (SQLException e) {
                java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "ガチャ券の受け取りに失敗しました");
                return 0;
            }

            if (numofsorryforbug > 576) {
                // 576より多い場合はその値を返す(同時にnumofsorryforbugから-576)
                command = "update " + getTableReference()
                        + " set numofsorryforbug = numofsorryforbug - 576"
                        + " where uuid like '" + struuid + "'";
                if (gateway.executeUpdate(command) == Fail) {
                    player.sendMessage(ChatColor.RED + "ガチャ券の受け取りに失敗しました");
                    return 0;
                }

                return 576;
            } else if (numofsorryforbug > 0) {
                // 0より多い場合はその値を返す(同時にnumofsorryforbug初期化)
                command = "update " + getTableReference()
                        + " set numofsorryforbug = 0"
                        + " where uuid like '" + struuid + "'";
                if (gateway.executeUpdate(command) == Fail) {
                    player.sendMessage(ChatColor.RED + "ガチャ券の受け取りに失敗しました");
                    return 0;
                }

                return numofsorryforbug;
            }

            player.sendMessage(ChatColor.YELLOW + "ガチャ券は全て受け取り済みのようです");
            return 0;
        });
    }

    /**
     * 投票ポイントをインクリメントするメソッド。
     * @param playerName プレーヤー名
     * @return 処理の成否
     */
    public ActionStatus incrementVotePoint(String playerName) {
        final String command = "update " + getTableReference()
                + " set p_vote = p_vote + 1" //1加算
                + " where name like '" + playerName + "'";

        return gateway.executeUpdate(command);
    }

    /**
     * プレミアムエフェクトポイントを加算するメソッド。
     * @param playerName プレーヤーネーム
     * @param num 足す整数
     * @return 処理の成否
     */
    public ActionStatus addPremiumEffectPoint(String playerName, int num) {
        final String command = "update " + getTableReference()
                + " set premiumeffectpoint = premiumeffectpoint + " + num //引数で来たポイント数分加算
                + " where name like '" + playerName + "'";

        return gateway.executeUpdate(command);
    }


    //指定されたプレイヤーにガチャ券を送信する
    public ActionStatus addPlayerBug(String playerName, int num) {
        String command = "update " + getTableReference()
                + " set numofsorryforbug = numofsorryforbug + " + num
                + " where name like '" + playerName + "'";

        return gateway.executeUpdate(command);
    }

    public boolean addChainVote (String name){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String lastvote;
        String select = "SELECT lastvote FROM " + getTableReference() + " " +
                "WHERE name LIKE '" + name + "'";
        try (ResultSet lrs = gateway.executeQuery(select)) {
            // 初回のnextがnull→データが1件も無い場合
            if (!lrs.next()) {
                return false;
            }

            if(lrs.getString("lastvote") == null || lrs.getString("lastvote").equals("")){
                lastvote = sdf.format(cal.getTime());
            }else {
                lastvote = lrs.getString("lastvote");
            }

            lrs.close();

            String update = "UPDATE " + getTableReference() + " " +
                    " SET lastvote = '" + sdf.format(cal.getTime()) + "'" +
                    " WHERE name LIKE '" + name + "'";

            gateway.executeUpdate(update);
        }catch (SQLException e) {
            Bukkit.getLogger().warning(Util.getName(name) + " sql failed. -> lastvote");
            e.printStackTrace();
            return false;
        }
        select = "SELECT chainvote FROM " +getTableReference() + " " +
                "WHERE name LIKE '" + name + "'";
        try (ResultSet lrs = gateway.executeQuery(select)) {
            // 初回のnextがnull→データが1件も無い場合
            if (!lrs.next()) {
                return false;
            }
            int count = lrs.getInt("chainvote");
            try {
                Date TodayDate = sdf.parse(sdf.format(cal.getTime()));
                Date LastDate = sdf.parse(lastvote);
                long TodayLong = TodayDate.getTime();
                long LastLong = LastDate.getTime();

                long datediff = (TodayLong - LastLong)/(1000 * 60 * 60 * 24 );
                if(datediff <= 1 || datediff >= 0){
                    count ++ ;
                }else {
                    count = 1;
                }
                //プレイヤーがオンラインの時即時反映させる
                Player player = Bukkit.getServer().getPlayer(name);
                if (player != null) {
                    //UUIDを取得
                    UUID givenuuid = player.getUniqueId();
                    //playerdataを取得
                    PlayerData playerdata = SeichiAssist.playermap.get(givenuuid);

                    playerdata.ChainVote ++;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            lrs.close();

            String update = "UPDATE " + getTableReference() + " " +
                    " SET chainvote = " + count +
                    " WHERE name LIKE '" + name + "'";

            gateway.executeUpdate(update);
        }catch (SQLException e) {
            Bukkit.getLogger().warning(Util.getName(name) + " sql failed. -> chainvote");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean setContribute(CommandSender sender, String targetName, int p) {
        int point;

        String select = "SELECT contribute_point FROM " + getTableReference() + " " + "WHERE name LIKE '" + targetName + "'";

        // selectで確認
        try (ResultSet lrs = gateway.executeQuery(select)) {
            // 初回のnextがnull→データが1件も無い場合
            if (!lrs.next()) {
                sender.sendMessage(ChatColor.RED + "" + targetName + " はデータベースに登録されていません");
                return false;
            }
            //今までのポイントを加算して計算
            point = p + lrs.getInt("contribute_point");
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "貢献度ptの取得に失敗しました");
            Bukkit.getLogger().warning(Util.getName(targetName) + " sql failed. -> contribute_point");
            e.printStackTrace();
            return false;
        }

        String update = "UPDATE " + getTableReference() + " " +
                " SET contribute_point = " + point +
                " WHERE name LIKE '" + targetName + "'";

        if (gateway.executeUpdate(update) == Fail) {
            sender.sendMessage(ChatColor.RED + "貢献度ptの変更に失敗しました");
            Bukkit.getLogger().warning(Util.getName(targetName) + " sql failed. -> contribute_point");
            return false;
        }
        return true;
    }

    // anniversary変更
    public boolean setAnniversary(boolean anniversary, UUID uuid) {
        String command = "UPDATE " + getTableReference() + " " + "SET anniversary = " + anniversary;
        if (uuid != null) {
            command += " WHERE uuid = '" + uuid.toString() + "'";
        }
        if (gateway.executeUpdate(command) == Fail) {
            Bukkit.getLogger().warning("sql failed. -> setAnniversary");
            return false;
        }
        return true;
    }

    /**
     * 実績予約領域書き換え処理
     *
     * @param sender 発行Player
     * @param targetName 対象Playerのname
     * @param achvNo 対象実績No
     * @return 成否…true: 成功、false: 失敗
     */
    public boolean writegiveachvNo(Player sender, String targetName, String achvNo) {
        String select = "SELECT giveachvNo FROM " + getTableReference() + " " +
                "WHERE name LIKE '" + targetName + "'";
        String update = "UPDATE " + getTableReference() + " " +
                " SET giveachvNo = " + achvNo +
                " WHERE name LIKE '" + targetName + "'";

        // selectで確認
        try (ResultSet lrs = gateway.executeQuery(select)) {
            // 初回のnextがnull→データが1件も無い場合
            if (!lrs.next()) {
                sender.sendMessage(ChatColor.RED + "" + targetName + " はデータベースに登録されていません");
                return false;
            }
            // 現在予約されている値を取得
            int giveachvNo = lrs.getInt("giveachvNo");
            // 既に予約がある場合
            if (giveachvNo != 0) {
                sender.sendMessage(ChatColor.RED + "" + targetName + " には既に実績No " + giveachvNo + " が予約されています");
                return false;
            }
            lrs.close();

            // 実績を予約
            gateway.executeUpdate(update);
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "実績の予約に失敗しました");
            Bukkit.getLogger().warning(Util.getName(sender) + " sql failed. -> writegiveachvNo");
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean saveShareInv(Player player, PlayerData playerdata, String data) {
        if (!playerdata.shareinvcooldownflag) {
            player.sendMessage(ChatColor.RED + "しばらく待ってからやり直してください");
            return false;
        }
        //連打による負荷防止の為クールダウン処理
        new CoolDownTask(player, CoolDownTask.SHAREINV).runTaskLater(plugin, 200);
        String struuid = playerdata.uuid.toString();
        String command = "SELECT shareinv FROM " + getTableReference() + " " +
                "WHERE uuid = '" + struuid + "'";
        try (ResultSet lrs = gateway.executeQuery(command)) {
            lrs.next();
            String shareinv = lrs.getString("shareinv");
            lrs.close();
            if (shareinv != null && !shareinv.equals("")) {
                player.sendMessage(ChatColor.RED + "既にアイテムが収納されています");
                return false;
            }
            command = "UPDATE " + getTableReference() + " " +
                    "SET shareinv = '" + data + "' " +
                    "WHERE uuid = '" + struuid + "'";
            if (gateway.executeUpdate(command) == Fail) {
                player.sendMessage(ChatColor.RED + "アイテムの収納に失敗しました");
                Bukkit.getLogger().warning(Util.getName(player) + " sql failed. -> saveShareInv(executeUpdate failed)");
                return false;
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "共有インベントリにアクセスできません");
            Bukkit.getLogger().warning(Util.getName(player) + " sql failed. -> clearShareInv(SQLException)");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String loadShareInv(Player player, PlayerData playerdata) {
        if(!playerdata.shareinvcooldownflag){
            player.sendMessage(ChatColor.RED + "しばらく待ってからやり直してください");
            return null;
        }
        //連打による負荷防止の為クールダウン処理
        new CoolDownTask(player, CoolDownTask.SHAREINV).runTaskLater(plugin,200);
        String struuid = playerdata.uuid.toString();
        String command = "SELECT shareinv FROM " + getTableReference() + " " +
                "WHERE uuid = '" + struuid + "'";
        String shareinv = null;
        try (ResultSet lrs = gateway.executeQuery(command)) {
            lrs.next();
            shareinv = lrs.getString("shareinv");
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "共有インベントリにアクセスできません");
            Bukkit.getLogger().warning(Util.getName(player) + " sql failed. -> loadShareInv");
            e.printStackTrace();
        }
        return shareinv;
    }

    public boolean clearShareInv(Player player, PlayerData playerdata) {
        String struuid = playerdata.uuid.toString();
        String command = "UPDATE " + getTableReference() + " " +
                "SET shareinv = '' " +
                "WHERE uuid = '" + struuid + "'";
        if (gateway.executeUpdate(command) == Fail) {
            player.sendMessage(ChatColor.RED + "アイテムのクリアに失敗しました");
            Bukkit.getLogger().warning(Util.getName(player) + " sql failed. -> clearShareInv");
            return false;
        }
        return true;
    }

    //lastquitがdays日以上(または未登録)のプレイヤー名を配列で取得
    public Map<UUID, String> selectLeavers(int days){
        Map<UUID, String> leavers = new HashMap<>();
        String command = "select name, uuid from " + getTableReference()
                + " where ((lastquit <= date_sub(curdate(), interval " + days + " day))"
                + " or (lastquit is null)) and (name != '') and (uuid != '')";
        try (ResultSet lrs = gateway.executeQuery(command)) {
            while (lrs.next()) {
                try {
                    //結果のStringをUUIDに変換
                    UUID uuid = UUID.fromString(lrs.getString("uuid"));
                    if (leavers.containsKey(uuid)) {
                        java.lang.System.out.println("playerdataにUUIDが重複しています: " + lrs.getString("uuid"));
                    } else {
                        //HashMapにUUIDとnameを登録
                        leavers.put(uuid, lrs.getString("name"));
                    }
                } catch (IllegalArgumentException e) {
                    java.lang.System.out.println("不適切なUUID: " + lrs.getString("name") + ": " + lrs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return null;
        }
        return leavers;
    }

    //ランキング表示用に総破壊ブロック数のカラムだけ全員分引っ張る
    private boolean updateBlockRankingList() {
        List<RankData> ranklist = new ArrayList<>();
        SeichiAssist.allplayerbreakblockint = 0;
        String command = "select name,level,totalbreaknum from " + getTableReference()
                + " order by totalbreaknum desc";
        try (ResultSet lrs = gateway.executeQuery(command)){
            while (lrs.next()) {
                RankData rankdata = new RankData();
                rankdata.name = lrs.getString("name");
                rankdata.level = lrs.getInt("level");
                rankdata.totalbreaknum = lrs.getLong("totalbreaknum");
                ranklist.add(rankdata);
                SeichiAssist.allplayerbreakblockint += rankdata.totalbreaknum;
            }
        } catch (SQLException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return false;
        }
        SeichiAssist.ranklist.clear();
        SeichiAssist.ranklist.addAll(ranklist);
        return true;
    }

    //ランキング表示用にプレイ時間のカラムだけ全員分引っ張る
    private boolean updatePlayTickRankingList() {
        List<RankData> ranklist = new ArrayList<>();
        String command = "select name,playtick from " + getTableReference()
                + " order by playtick desc";
        try (ResultSet lrs = gateway.executeQuery(command)){
            while (lrs.next()) {
                RankData rankdata = new RankData();
                rankdata.name = lrs.getString("name");
                rankdata.playtick = lrs.getInt("playtick");
                ranklist.add(rankdata);
            }
        } catch (SQLException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return false;
        }
        SeichiAssist.ranklist_playtick.clear();
        SeichiAssist.ranklist_playtick.addAll(ranklist);
        return true;
    }

    //ランキング表示用に投票数のカラムだけ全員分引っ張る
    private boolean updateVoteRankingList() {
        List<RankData> ranklist = new ArrayList<>();
        String command = "select name,p_vote from " + getTableReference()
                + " order by p_vote desc";
        try (ResultSet lrs = gateway.executeQuery(command)){
            while (lrs.next()) {
                RankData rankdata = new RankData();
                rankdata.name = lrs.getString("name");
                rankdata.p_vote = lrs.getInt("p_vote");
                ranklist.add(rankdata);
            }
        } catch (SQLException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return false;
        }
        SeichiAssist.ranklist_p_vote.clear();
        SeichiAssist.ranklist_p_vote.addAll(ranklist);
        return true;
    }

    //ランキング表示用にプレミアムエフェクトポイントのカラムだけ全員分引っ張る
    private boolean updatePremiumEffectPointRankingList() {
        List<RankData> ranklist = new ArrayList<>();
        String command = "select name,premiumeffectpoint from " + getTableReference()
                + " order by premiumeffectpoint desc";
        try (ResultSet lrs = gateway.executeQuery(command)){
            while (lrs.next()) {
                RankData rankdata = new RankData();
                rankdata.name = lrs.getString("name");
                rankdata.premiumeffectpoint = lrs.getInt("premiumeffectpoint");
                ranklist.add(rankdata);
            }
        } catch (SQLException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return false;
        }
        SeichiAssist.ranklist_premiumeffectpoint.clear();
        SeichiAssist.ranklist_premiumeffectpoint.addAll(ranklist);
        return true;
    }

    //ランキング表示用に上げたりんご数のカラムだけ全員分引っ張る
    private boolean updateAppleNumberRankingList() {
        List<RankData> ranklist = new ArrayList<>();
        SeichiAssist.allplayergiveapplelong = 0;
        String command = "select name,p_apple from " + getTableReference() + " order by p_apple desc";
        try (ResultSet lrs = gateway.executeQuery(command)){
            while (lrs.next()) {
                RankData rankdata = new RankData();
                rankdata.name = lrs.getString("name");
                rankdata.p_apple = lrs.getInt("p_apple");
                ranklist.add(rankdata);
                SeichiAssist.allplayergiveapplelong += rankdata.p_apple;
            }
        } catch (SQLException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return false;
        }
        SeichiAssist.ranklist_p_apple.clear();
        SeichiAssist.ranklist_p_apple.addAll(ranklist);
        return true;
    }

    /**
     * 全ランキングリストの更新処理
     * @return 成否…true: 成功、false: 失敗
     * TODO この処理はDB上と通信を行う為非同期にすべき
     */
    public boolean updateAllRankingList() {
        if(!updateBlockRankingList())return false;
        if(!updatePlayTickRankingList())return false;
        if(!updateVoteRankingList())return false;
        if(!updatePremiumEffectPointRankingList())return false;
        if(!updateAppleNumberRankingList())return false;

        return true;
    }

    //プレイヤーレベル全リセット
    public ActionStatus resetAllPlayerLevel(){
        String command = "update " + getTableReference()
                + " set level = 1";
        return gateway.executeUpdate(command);
    }

    //プレイヤーのレベルと整地量をセット
    public ActionStatus resetPlayerLevelandBreaknum(UUID uuid){
        String struuid = uuid.toString();
        PlayerData playerdata = SeichiAssist.playermap.get(uuid);
        int level = playerdata.level;
        long totalbreaknum = playerdata.totalbreaknum;

        final String command = "update " + getTableReference()
                + " set"
                + " level = " + level
                + ",totalbreaknum = " + totalbreaknum
                + " where uuid like '" + struuid + "'";

        return gateway.executeUpdate(command);
    }

    //プレイヤーのレベルと整地量をセット(プレイヤーデータが無い場合)
    public ActionStatus resetPlayerLevelandBreaknum(UUID uuid, int level){
        String struuid = uuid.toString();
        int totalbreaknum = SeichiAssist.levellist.get(level-1);

        String command = "update " + getTableReference()
                + " set"
                + " level = " + level
                + ",totalbreaknum = " + totalbreaknum
                + " where uuid like '" + struuid + "'";

        return gateway.executeUpdate(command);
    }

    //全員に詫びガチャの配布
    public ActionStatus addAllPlayerBug(int amount){
        String command = "update " + getTableReference() + " set numofsorryforbug = numofsorryforbug + " + amount;
        return gateway.executeUpdate(command);
    }

    //指定プレイヤーの四次元ポケットの中身取得
    public Inventory selectInventory(UUID uuid){
        String struuid = uuid.toString();
        Inventory inventory = null;
        String command = "select inventory from " + getTableReference()
                + " where uuid like '" + struuid + "'";
        try (ResultSet lrs = gateway.executeQuery(command)){
            while (lrs.next()) {
                inventory = BukkitSerialization.fromBase64(lrs.getString("inventory"));
            }
        } catch (SQLException | IOException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return null;
        }
        return inventory;
    }

    //指定プレイヤーのlastquitを取得
    public String selectLastQuit(String name){
        String lastquit = "";
        String command = "select lastquit from " + getTableReference() + " where name = '" + name + "'";
        try (ResultSet lrs = gateway.executeQuery(command)){
            while (lrs.next()) {
                lastquit = lrs.getString("lastquit");
            }
        } catch (SQLException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return null;
        }
        return lastquit;
    }

    public void loadPlayerData(PlayerData playerdata) {
        Player player = Bukkit.getPlayer(playerdata.uuid);
        player.sendMessage(ChatColor.YELLOW + "プレイヤーデータ取得中。完了まで動かずお待ち下さい…");
        new CheckAlreadyExistPlayerDataTask(playerdata).runTaskAsynchronously(plugin);
    }

    //ondisable"以外"の時のプレイヤーデータセーブ処理(loginflag折りません)
    public void savePlayerData(PlayerData playerdata){
        new PlayerDataSaveTask(playerdata,false,false).runTaskAsynchronously(plugin);
    }

    //ondisable"以外"の時のプレイヤーデータセーブ処理(ログアウト時に使用、loginflag折ります)
    public void saveQuitPlayerData(PlayerData playerdata) {
        new PlayerDataSaveTask(playerdata,false,true).runTaskAsynchronously(plugin);
    }

}
