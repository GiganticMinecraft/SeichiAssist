package com.github.unchama.seichiassist.database.manipulators;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.database.DatabaseConstants;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.github.unchama.util.ActionStatus.Fail;

public class MineStackGachaDataManipulator {
    private final DatabaseGateway gateway;

    public MineStackGachaDataManipulator(DatabaseGateway gateway) {
        this.gateway = gateway;
    }

    private String getTableReference() {
        return gateway.databaseName + "." + DatabaseConstants.MINESTACK_GACHADATA_TABLENAME;
    }

    //MineStack用ガチャデータロード
    public boolean loadMineStackGachaData(){
        List<MineStackGachaData> gachadatalist = new ArrayList<>();

        String command = "select * from " + getTableReference();
        try (ResultSet lrs = gateway.executeQuery(command)){
            while (lrs.next()) {
                MineStackGachaData gachadata = new MineStackGachaData();
                Inventory inventory = BukkitSerialization.fromBase64(lrs.getString("itemstack"));
                gachadata.itemStack = (inventory.getItem(0));
                gachadata.amount = lrs.getInt("amount");
                gachadata.level = lrs.getInt("level");
                gachadata.objName = lrs.getString("obj_name");
                gachadata.probability = lrs.getDouble("probability");
                gachadatalist.add(gachadata);
            }
        } catch (SQLException | IOException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return false;
        }
        SeichiAssist.msgachadatalist.clear();
        SeichiAssist.msgachadatalist.addAll(gachadatalist);
        return true;
    }

    //MineStack用ガチャデータセーブ
    public boolean saveMineStackGachaData(){


        //まずmysqlのガチャテーブルを初期化(中身全削除)
        String command = "truncate table " + getTableReference();
        if(gateway.executeUpdate(command) == Fail){
            return false;
        }

        //次に現在のgachadatalistでmysqlを更新
        for(MineStackGachaData gachadata : SeichiAssist.msgachadatalist){
            //Inventory作ってガチャのitemstackに突っ込む
            Inventory inventory = Bukkit.getServer().createInventory(null, 9*1);
            inventory.setItem(0,gachadata.itemStack);

            command = "insert into " + getTableReference() + " (probability,amount,level,obj_name,itemstack)"
                    + " values"
                    + "(" + gachadata.probability
                    + "," + gachadata.amount
                    + "," + gachadata.level
                    + ",'" + gachadata.objName + "'"
                    + ",'" + BukkitSerialization.toBase64(inventory) + "'"
                    + ")";

            if (gateway.executeUpdate(command) == Fail) {
                return false;
            }
        }
        return true;
    }
}
