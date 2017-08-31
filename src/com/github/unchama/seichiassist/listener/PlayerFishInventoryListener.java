package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerFishInventoryListener implements Listener {
    HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
    List<GachaData> gachadatalist = SeichiAssist.gachadatalist;
    SeichiAssist plugin = SeichiAssist.plugin;
    private Config config = SeichiAssist.config;
    private Sql sql = SeichiAssist.plugin.sql;

    @EventHandler
    public void onPlayerClickFishInv(InventoryClickEvent event) {
        //外枠のクリック処理なら終了
        if (event.getClickedInventory() == null) {
            return;
        }

        ItemStack itemstackcurrent = event.getCurrentItem();
        InventoryView view = event.getView();
        HumanEntity he = view.getPlayer();
        //インベントリを開けたのがプレイヤーではない時終了
        if (!he.getType().equals(EntityType.PLAYER)) {
            return;
        }

        Inventory topinventory = view.getTopInventory();
        //インベントリが存在しない時終了
        if (topinventory == null) {
            return;
        }

        //インベントリタイプがホッパーでない時終了
        if (!topinventory.getType().equals(InventoryType.HOPPER)) {
            return;
        }

        Player player = (Player)he;
        UUID uuid = player.getUniqueId();
        PlayerData playerdata = playermap.get(uuid);

        //インベントリ名が以下の時処理
        if (topinventory.getTitle().equals(ChatColor.BLUE + "釣りメインメニュー")) {
            event.setCancelled(true);

            //プレイヤーインベントリのクリックの場合終了
            if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                return;
            }

            /*
            この先に処理を書く
             */
        }
    }
}
