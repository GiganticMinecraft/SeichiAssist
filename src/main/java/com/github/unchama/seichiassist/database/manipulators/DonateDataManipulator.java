package com.github.unchama.seichiassist.database.manipulators;

import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.database.DatabaseConstants;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.util.ActionStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DonateDataManipulator {
    private final DatabaseGateway gateway;

    public DonateDataManipulator(DatabaseGateway gateway) {
        this.gateway = gateway;
    }

    private String getTableReference() {
        return gateway.databaseName + "." + DatabaseConstants.DONATEDATA_TABLENAME;
    }

    public ActionStatus addPremiumEffectBuy(PlayerData playerdata,
                                            ActiveSkillPremiumEffect effect) {
        String command = "insert into " + getTableReference()
                + " (playername,playeruuid,effectnum,effectname,usepoint,date) "
                + "value("
                + "'" + playerdata.name + "',"
                + "'" + playerdata.uuid.toString() + "',"
                + effect.getNum() + ","
                + "'" + effect.getsqlName() + "',"
                + effect.getUsePoint() + ","
                + "cast( now() as datetime )"
                + ")";

        return gateway.executeUpdate(command);
    }

    public ActionStatus addDonate(String name, int point) {
        String command = "insert into " + getTableReference()
                + " (playername,getpoint,date) "
                + "value("
                + "'" + name + "',"
                + point + ","
                + "cast( now() as datetime )"
                + ")";
        return gateway.executeUpdate(command);
    }

    public boolean loadDonateData(PlayerData playerdata, Inventory inventory) {
        ItemStack itemstack;
        ItemMeta itemmeta;
        Material material;
        List<String> lore;
        int count = 0;
        ActiveSkillPremiumEffect[] effect = ActiveSkillPremiumEffect.values();

        String command = "select * from " + getTableReference() + " where playername = '" + playerdata.name + "'";
        try (ResultSet lrs = gateway.executeQuery(command)){
            while (lrs.next()) {
                //ポイント購入の処理
                if(lrs.getInt("getpoint")>0){
                    itemstack = new ItemStack(Material.DIAMOND);
                    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND);
                    itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付");
                    lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "" + "金額：" + lrs.getInt("getpoint")*100,
                            ChatColor.RESET + "" +  ChatColor.GREEN + "" + "プレミアムエフェクトポイント：+" + lrs.getInt("getpoint"),
                            ChatColor.RESET + "" +  ChatColor.GREEN + "" + "日時：" + lrs.getString("date")
                    );
                    itemmeta.setLore(lore);
                    itemstack.setItemMeta(itemmeta);
                    inventory.setItem(count,itemstack);
                }else if(lrs.getInt("usepoint")>0){
                    int num = lrs.getInt("effectnum")-1;
                    material = effect[num].getMaterial();
                    itemstack = new ItemStack(material);
                    itemmeta = Bukkit.getItemFactory().getItemMeta(material);
                    itemmeta.setDisplayName(ChatColor.RESET + "" +  ChatColor.YELLOW + "購入エフェクト：" + effect[num].getName());
                    lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GOLD + "" + "プレミアムエフェクトポイント： -" + lrs.getInt("usepoint"),
                            ChatColor.RESET + "" +  ChatColor.GOLD + "" + "日時：" + lrs.getString("date")
                    );
                    itemmeta.setLore(lore);
                    itemstack.setItemMeta(itemmeta);
                    inventory.setItem(count,itemstack);
                }
                count ++;
            }
        } catch (SQLException e) {
            java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
