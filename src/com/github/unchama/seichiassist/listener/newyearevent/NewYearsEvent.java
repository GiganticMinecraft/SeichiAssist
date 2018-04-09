package com.github.unchama.seichiassist.listener.newyearevent;

import com.github.unchama.seichiassist.*;
import com.github.unchama.seichiassist.data.*;
import com.github.unchama.seichiassist.util.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.*;

import java.text.*;
import java.util.*;
/**
 * Created by karayuu on 2017/11/28
 * Developer of Gigantic☆Seichi Server
 * Support at dev-basic or dev-extreme channel of Discord
 */
/**
 * 正月イベント関連クラス
 */
public class NewYearsEvent implements Listener {
    private static SeichiAssist plugin = SeichiAssist.plugin;
    private static Config config = SeichiAssist.config;
    private static Map<UUID, PlayerData> playerMap = SeichiAssist.playermap;

    /**
     * 正月イベント準備メソッド(コンストラクタ)
     * @param plugin SeichiAssistインスタンス
     */
    public NewYearsEvent(SeichiAssist plugin) {
        try {
            //イベント開催期間の判断
            Date nowDay = new Date();
            Calendar now = Calendar.getInstance();
            now.setTime(nowDay);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date givingSobaDay = dateFormat.parse(config.getGivingNewYearSobaDay());
            Calendar givingSobaCal = Calendar.getInstance();
            givingSobaCal.setTime(givingSobaDay);
            if (now.get(Calendar.MONTH) == givingSobaCal.get(Calendar.MONTH) && now.get(Calendar.DATE) == givingSobaCal.get(Calendar.DATE)) {
                //「年越し蕎麦」配布処理はこのクラス内に
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
            }
            Date dropBagDay_Start = dateFormat.parse(config.getDropNewYearBagStartDay());
            Date dropBagDay_End = dateFormat.parse(config.getDropNewYearBagEndDay());
            if (isHeld(nowDay, dropBagDay_Start, dropBagDay_End)) {
                //「お年玉袋」配布処理は他クラス
                plugin.getServer().getPluginManager().registerEvents(new NewYearBagListener(), plugin);
            }
            Date useAppleDay_Start = dateFormat.parse(config.getNewYearAppleStartDay());
            Date useAppleDay_End = dateFormat.parse(config.getNewYearAppleEndDay());
            if (isHeld(nowDay, useAppleDay_Start, useAppleDay_End)) {
                //「正月リンゴ」配布処理は他クラス
                plugin.getServer().getPluginManager().registerEvents(new NewYearItemListener(), plugin);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Bukkit.getLogger().warning(ChatColor.RED + "[正月イベント]Configの日付設定は年4桁-月2桁-日2桁で設定してください。");
        }
    }

    /**
     * 年越し蕎麦配布処理
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void giveNewYearSobaToPlayer(PlayerJoinEvent event) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, () ->
                giveNewYearSobaToPlayer(event.getPlayer(), config.getNewYearSobaYear()), 200L);
    }

    private void giveNewYearSobaToPlayer(Player player, String year) {
        PlayerData playerData = playerMap.get(player.getUniqueId());
        if (playerData.hasNewYearSobaGive) {
            return;
        }

        if (Util.isPlayerInventryFill(player)) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "インベントリが一杯のため,アイテムが入手できませんでした。");
            player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "インベントリに空きを作ってから再度サーバに参加してください。");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
        } else {
            String command = "give " + player.getName() + " skull 1 3 {display:{Name:\"年越し蕎麦(" + year + "年)\",Lore:[\"\", \"" + ChatColor.YELLOW + "大晦日記念アイテムだよ！\"]},SkullOwner:{Id:\"f15ab073-412e-4fe2-8668-1be12066e2ac\"," +
                    "Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY4MzRiNWIyNTQyNmRlNjM1MzhlYzgyY2E4ZmJlY2ZjYmIzZTY4MmQ4MDYzNjQzZDJlNjdhNzYyMWJkIn19fQ==\"}]}}}";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
            playerData.hasNewYearSobaGive = true;
        }
    }

    /**
     * 現在の日付が開催日であるかどうか返します。(開始日0:00～終了日23:59まで)
     * @param nowDay 現在日
     * @param startDay 開始日
     * @param endDay 終了日
     * @return startDayからendDayの範囲内にあるか(startDay, endDayを含む)
     */
    public static boolean isHeld(Date nowDay, Date startDay, Date endDay) {
        /*
        //a.compareTo(b) -> aとb同じ(0) aが後(>0) aが前(<0)
        //nowDayが後の時>0
        int start_diff = startDay.compareTo(nowDay);
        //nowDayが前の時<0
        int end_diff = nowDay.compareTo(endDay);

        //nowDayがstartDay,endDayの範囲内にある(startDay,endDayを含む)ときtrueを返す
        return start_diff >= 0 && end_diff <= 0;
        */
        return !(nowDay.before(startDay) || nowDay.after(endDay));
    }
}
