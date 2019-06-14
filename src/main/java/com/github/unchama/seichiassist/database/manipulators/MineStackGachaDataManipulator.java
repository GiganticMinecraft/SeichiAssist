package com.github.unchama.seichiassist.database.manipulators;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.MineStackGachaData;
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
                Inventory savedInventory = BukkitSerialization.fromBase64(lrs.getString("itemstack"));
                ItemStack itemStack = savedInventory.getItem(0);

                MineStackGachaData gachaData = new MineStackGachaData(
                        lrs.getString("obj_name"), itemStack, lrs.getDouble("probability"), lrs.getInt("level")
                );

                gachadatalist.add(gachaData);
            }
        } catch (SQLException | IOException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return false;
        }
        SeichiAssist.Companion.getMsgachadatalist().clear();
        SeichiAssist.Companion.getMsgachadatalist().addAll(gachadatalist);
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
        for(MineStackGachaData gachadata : SeichiAssist.Companion.getMsgachadatalist()){
            //Inventory作ってガチャのitemstackに突っ込む
            Inventory inventory = Bukkit.getServer().createInventory(null, 9*1);
            inventory.setItem(0, gachadata.getItemStack());

            command = "insert into " + getTableReference() + " (probability,level,obj_name,itemstack)"
                    + " values"
                    + "(" + gachadata.getProbability()
                    + "," + gachadata.getLevel()
                    + ",'" + gachadata.getObjName() + "'"
                    + ",'" + BukkitSerialization.toBase64(inventory) + "'"
                    + ")";

            if (gateway.executeUpdate(command) == Fail) {
                return false;
            }
        }
        return true;
    }
}
