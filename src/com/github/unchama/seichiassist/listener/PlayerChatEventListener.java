package com.github.unchama.seichiassist.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

/**
 * Created by karayuu on 2018/06/17
 */

public class PlayerChatEventListener implements Listener {

	@EventHandler(priority=EventPriority.LOW)
    public void setSubHomeName(AsyncPlayerChatEvent event) {
    	Player player = event.getPlayer();
    	PlayerData data = SeichiAssist.playermap.get(player.getUniqueId());

        if (!data.isSubHomeNameChange) {
            return;
        }

        int n = data.setHomeNameNum;
        if(event.getMessage().contains(",")){
        	player.sendMessage(ChatColor.RED + "名前に[,]を使用することはできません");
        }else{
            data.subhome_name[n] = event.getMessage();

            player.sendMessage(ChatColor.GREEN + "サブホームポイント" + (n+1) + "の名前を");
            player.sendMessage(ChatColor.GREEN + event.getMessage() + "に更新しました");
        }
        data.isSubHomeNameChange = false;
        event.setCancelled(true);
    }

	/*
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerData data = SeichiAssist.playermap.get(player.getUniqueId());

        if (!data.isSearching) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage();

        if (message.equals("end")) {
            player.sendMessage(ChatColor.AQUA + "MineStackアイテム検索を終了しました。");
            return;
        }

        //前方一致で検索
        List<MineStackObj> searchList = SeichiAssist.minestacklist.stream().filter(
                obj -> obj.getJapaneseName().startsWith(message)
        ).collect(Collectors.toList());

        Map<Integer, MineStackObj> map = new HashMap<>();

        searchList.forEach( obj ->
                map.put(SeichiAssist.minestacklist.indexOf(obj), obj)
        );
        data.indexMap = map;

        Inventory searchInventory = Bukkit.createInventory(null, 9*6, ChatColor.RED + "" +
                ChatColor.UNDERLINE + message + ChatColor.DARK_PURPLE + "の検索結果");

        int slot = 0;
        for (Map.Entry<Integer, MineStackObj> entry : map.entrySet()) {
            int index = entry.getKey();
            MineStackObj obj = entry.getValue();
            if (obj.getItemStack() == null) {
                MenuInventoryData.setMineStackButton(searchInventory, data.minestack.getNum(index), new ItemStack(obj.getMaterial(), 1, (short)obj.getDurability()), SeichiAssist.config.getMineStacklevel(obj.getLevel()), slot, obj.getJapaneseName());
            } else {
                MenuInventoryData.setMineStackButton(searchInventory, data.minestack.getNum(index), obj.getItemStack(), SeichiAssist.config.getMineStacklevel(obj.getLevel()), slot, obj.getJapaneseName());
            }
            slot++;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, (float) 1, (float) 1);
        player.openInventory(searchInventory);

        data.isSearching = false;
    } */

}
