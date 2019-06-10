package com.github.unchama.seichiassist.database.manipulators;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaCandidate;
import com.github.unchama.seichiassist.database.DatabaseConstants;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.github.unchama.util.ActionStatus.Fail;

public class GachaDataManipulator {
    private final DatabaseGateway gateway;

    public GachaDataManipulator(DatabaseGateway gateway) {
        this.gateway = gateway;
    }

    private String getTableReference() {
        return gateway.databaseName + "." + DatabaseConstants.GACHADATA_TABLENAME;
    }

    //ガチャデータロード
    public boolean loadGachaData(){
        List<GachaCandidate> gachadatalist = new ArrayList<>();

        String command = "select * from " + getTableReference();
        try (ResultSet lrs = gateway.executeQuery(command)) {
            while (lrs.next()) {
                Inventory restoredInventory = BukkitSerialization.fromBase64(lrs.getString("itemstack"));
                ItemStack restoredItemStack = restoredInventory.getItem(0);

                GachaCandidate gachadata = new GachaCandidate(
                        restoredItemStack, lrs.getDouble("probability")
                );

                gachadatalist.add(gachadata);
            }
        } catch (SQLException | IOException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return false;
        }
        SeichiAssist.gachadatalist.clear();
        SeichiAssist.gachadatalist.addAll(gachadatalist);
        return true;

    }

    //ガチャデータセーブ
    public boolean saveGachaData(){

        //まずmysqlのガチャテーブルを初期化(中身全削除)
        String command = "truncate table " + getTableReference();
        if(gateway.executeUpdate(command) == Fail){
            return false;
        }

        //次に現在のgachadatalistでmysqlを更新
        for(GachaCandidate gachadata : SeichiAssist.gachadatalist){
            //Inventory作ってガチャのitemstackに突っ込む
            Inventory inventory = Bukkit.getServer().createInventory(null, 9*1);
            inventory.setItem(0, gachadata.getItemStack());

            command = "insert into " + getTableReference() + " (probability, itemstack)"
                    + " values"
                    + "(" + gachadata.getProbability() + "," +
                    "'" + BukkitSerialization.toBase64(inventory) + "')";
            if(gateway.executeUpdate(command) == Fail){
                return false;
            }
        }
        return true;
    }


}
