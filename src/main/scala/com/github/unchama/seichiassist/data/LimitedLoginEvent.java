package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import scala.collection.mutable.HashMap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class LimitedLoginEvent {
    private static final Config config = SeichiAssist.seichiAssistConfig();
    private final HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();
    private String lastcheckdate;

    public void setLastCheckDate(final String s) {
        lastcheckdate = s;
    }

    //ここで処理対象のユーザーと、そのtitleNoを拾って処理を行う。
    public void TryGetItem(final Player p) {
        final UUID uuid = p.getUniqueId();
        final PlayerData playerdata = playermap.getOrElse(uuid, () -> null);
        final ItemStack skull = Util.getskull(p.getName());
        final Calendar cal = Calendar.getInstance();
        final DateFormat sdf = SimpleDateFormat.getDateInstance(); // new SimpleDateFormat("yyyy/MM/dd");

        if (lastcheckdate == null) return;

        if (lastcheckdate.isEmpty()) {
            return;
        }

        try {
            final Date TodayDate = sdf.parse(sdf.format(cal.getTime()));
            final Date LastDate = sdf.parse(lastcheckdate);
            final Date LLEStart = sdf.parse(config.getLimitedLoginEventStart());
            final Date LLEEnd = sdf.parse(config.getLimitedLoginEventEnd());

            final long TodayLong = TodayDate.getTime();
            final long LastLong = LastDate.getTime();
            final long LLEStartLong = LLEStart.getTime();
            final long LLEEndLong = LLEEnd.getTime();

            int loginDays = playerdata.LimitedLoginCount();
            int configDays;
            int internalItemId;
            int amount;

            //開催期間内かどうか
            final long today2start = ((TodayLong - LLEStartLong) / (1000 * 60 * 60 * 24));
            final long today2end = ((TodayLong - LLEEndLong) / (1000 * 60 * 60 * 24));
            final long last2start = ((LastLong - LLEStartLong) / (1000 * 60 * 60 * 24));
            final long last2end = ((LastLong - LLEEndLong) / (1000 * 60 * 60 * 24));
            if ((today2start >= 0) && (today2end <= 0)) {
                //最終ログインが開催期間内だったか
                if (!((last2start >= 0) && (last2end <= 0))) {
                    //開催期間内初のログイン時、開催終了後初のログイン時にここを処理
                    //期間限定の累計ログイン数のデータをリセットする。
                    loginDays = 0;
                }

                loginDays++;
                configDays = 0;
                do {
                    internalItemId = Integer.parseInt(config.getLimitedLoginEventItem(configDays));
                    amount = Integer.parseInt(config.getLimitedLoginEventAmount(configDays));
                    switch (internalItemId) {
                        case 1://配布対象「ガチャ券」
                            final String message;
                            if (configDays == 0) {
                                message = "限定ログボ！";
                            } else {
                                message = "限定ログボ" + loginDays + "日目記念！";
                            }
                            p.sendMessage("【" + message + "】" + amount + "個のガチャ券をプレゼント！");
                            for (int i = 1; i <= amount; i++) {
                                if (p.getInventory().contains(skull) || !Util.isPlayerInventoryFull(p)) {
                                    Util.addItem(p, skull);
                                } else {
                                    Util.dropItem(p, skull);
                                }
                            }
                            break;

                        case 2://配布対象「未定」
                            //配布処理記入場所
                            //今後の追加のためのサンプルです。
                            break;
                    }

                    configDays += loginDays;
                } while (configDays == loginDays);
            }

            playerdata.LimitedLoginCount_$eq(loginDays);

        } catch (final ParseException e) {
            e.printStackTrace();
        }
    }
}
