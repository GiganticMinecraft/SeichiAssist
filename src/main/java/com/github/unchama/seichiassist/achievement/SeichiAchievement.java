package com.github.unchama.seichiassist.achievement;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * 実績の列挙.
 *
 * @author unicroak
 */
public enum SeichiAchievement {

    // 整地ランキング
    NO_1001(1001, player -> getPlayerData(player).calcPlayerRank(player) == 1),
    NO_1002(1002, player -> getRank(player) < 6), // maybe wrong, not `<` but `>`
    NO_1003(1003, player -> getRank(player) < 28),
    NO_1004(1004, player -> getRank(player) < 51),
    NO_1005(1005, player -> getRank(player) < 751),
    NO_1006(1006, player -> getRank(player) < 1001),
    NO_1007(1007, player -> getRank(player) < 2501),
    NO_1008(1008, player -> getRank(player) < 5001),
    NO_1009(1009, player -> getRank(player) < 10001),
    NO_1010(1010, player -> getRank(player) < 101),
    NO_1011(1011, player -> getRank(player) < 251),
    NO_1012(1012, player -> getRank(player) < 501),

    // 整地量
    NO_3001(3001, player -> getBrokenBlockAmount(player) > 2147483646L),
    NO_3002(3002, player -> getBrokenBlockAmount(player) > 1000000000L),
    NO_3003(3003, player -> getBrokenBlockAmount(player) > 500000000L),
    NO_3004(3004, player -> getBrokenBlockAmount(player) > 100000000L),
    NO_3005(3005, player -> getBrokenBlockAmount(player) > 50000000L),
    NO_3006(3006, player -> getBrokenBlockAmount(player) > 10000000L),
    NO_3007(3007, player -> getBrokenBlockAmount(player) > 5000000L),
    NO_3008(3008, player -> getBrokenBlockAmount(player) > 1000000L),
    NO_3009(3009, player -> getBrokenBlockAmount(player) > 500000L),
    NO_3010(3010, player -> getBrokenBlockAmount(player) > 100000L),
    NO_3011(3011, player -> getBrokenBlockAmount(player) > 2000000000L),
    NO_3012(3012, player -> getBrokenBlockAmount(player) > 3000000000L),
    NO_3013(3013, player -> getBrokenBlockAmount(player) > 4000000000L),
    NO_3014(3014, player -> getBrokenBlockAmount(player) > 5000000000L),
    NO_3015(3015, player -> getBrokenBlockAmount(player) > 6000000000L),
    NO_3016(3016, player -> getBrokenBlockAmount(player) > 7000000000L),
    NO_3017(3017, player -> getBrokenBlockAmount(player) > 8000000000L),
    NO_3018(3018, player -> getBrokenBlockAmount(player) > 9000000000L),
    NO_3019(3019, player -> getBrokenBlockAmount(player) > 10000000000L),

    // 参加時間
    NO_4001(4001, player -> getSpentTicks(player) > 144000000L),
    NO_4002(4002, player -> getSpentTicks(player) > 72000000L),
    NO_4003(4003, player -> getSpentTicks(player) > 36000000L),
    NO_4004(4004, player -> getSpentTicks(player) > 18000000L),
    NO_4005(4005, player -> getSpentTicks(player) > 7200000L),
    NO_4006(4006, player -> getSpentTicks(player) > 3600000L),
    NO_4007(4007, player -> getSpentTicks(player) > 1728000L),
    NO_4008(4008, player -> getSpentTicks(player) > 720000L),
    NO_4009(4009, player -> getSpentTicks(player) > 360000L),
    NO_4010(4010, player -> getSpentTicks(player) > 72000L),
    NO_4011(4011, player -> getSpentTicks(player) > 216000000L),
    NO_4012(4012, player -> getSpentTicks(player) > 288000000L),
    NO_4013(4013, player -> getSpentTicks(player) > 360000000L),
    NO_4014(4014, player -> getSpentTicks(player) > 432000000L),
    NO_4015(4015, player -> getSpentTicks(player) > 504000000L),
    NO_4016(4016, player -> getSpentTicks(player) > 576000000L),
    NO_4017(4017, player -> getSpentTicks(player) > 648000000L),
    NO_4018(4018, player -> getSpentTicks(player) > 720000000L),
    NO_4019(4019, player -> getSpentTicks(player) > 864000000L),
    NO_4020(4020, player -> getSpentTicks(player) > 1008000000L),
    NO_4021(4021, player -> getSpentTicks(player) > 1152000000L),
    NO_4022(4022, player -> getSpentTicks(player) > 1296000000L),
    NO_4023(4023, player -> getSpentTicks(player) > 1440000000L),

    // 連続ログイン
    NO_5001(5001, player -> getDaysChaining(player) >= 100),
    NO_5002(5002, player -> getDaysChaining(player) >= 50),
    NO_5003(5003, player -> getDaysChaining(player) >= 30),
    NO_5004(5004, player -> getDaysChaining(player) >= 20),
    NO_5005(5005, player -> getDaysChaining(player) >= 10),
    NO_5006(5006, player -> getDaysChaining(player) >= 5),
    NO_5007(5007, player -> getDaysChaining(player) >= 3),
    NO_5008(5008, player -> getDaysChaining(player) >= 2),
    NO_5101(5101, player -> getTotalPlayedDays(player) >= 365),
    NO_5102(5102, player -> getTotalPlayedDays(player) >= 300),
    NO_5103(5103, player -> getTotalPlayedDays(player) >= 200),
    NO_5104(5104, player -> getTotalPlayedDays(player) >= 100),
    NO_5105(5105, player -> getTotalPlayedDays(player) >= 75),
    NO_5106(5106, player -> getTotalPlayedDays(player) >= 50),
    NO_5107(5107, player -> getTotalPlayedDays(player) >= 30),
    NO_5108(5108, player -> getTotalPlayedDays(player) >= 20),
    NO_5109(5109, player -> getTotalPlayedDays(player) >= 10),
    NO_5110(5110, player -> getTotalPlayedDays(player) >= 5),
    NO_5111(5111, player -> getTotalPlayedDays(player) >= 2),
    NO_5112(5112, player -> getTotalPlayedDays(player) >= 400),
    NO_5113(5113, player -> getTotalPlayedDays(player) >= 500),
    NO_5114(5114, player -> getTotalPlayedDays(player) >= 600),
    NO_5115(5115, player -> getTotalPlayedDays(player) >= 700),
    NO_5116(5116, player -> getTotalPlayedDays(player) >= 730),
    NO_5117(5117, player -> getTotalPlayedDays(player) >= 800),
    NO_5118(5118, player -> getTotalPlayedDays(player) >= 900),
    NO_5119(5119, player -> getTotalPlayedDays(player) >= 1000),
    NO_5120(5120, player -> getTotalPlayedDays(player) >= 1095),

    // 投票数
    NO_6001(6001, player -> getVotingCounts(player) >= 365),
    NO_6002(6002, player -> getVotingCounts(player) >= 200),
    NO_6003(6003, player -> getVotingCounts(player) >= 100),
    NO_6004(6004, player -> getVotingCounts(player) >= 50),
    NO_6005(6005, player -> getVotingCounts(player) >= 25),
    NO_6006(6006, player -> getVotingCounts(player) >= 10),
    NO_6007(6007, player -> getVotingCounts(player) >= 5),
    NO_6008(6008, player -> getVotingCounts(player) >= 1),

    // 隠し
    NO_8001(8001, player -> {
        for (int slot = 0; slot < 9 * 4; slot++) {
            ItemStack head = player.getInventory().getItem(slot);
            if (head == null
                    || head.getType() != Material.SKULL_ITEM) {
                return false;
            }

            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (!meta.hasOwner()
                    || !Objects.equals(meta.getOwningPlayer().getUniqueId(), UUID.fromString("b66cc3f6-a045-42ad-b4b8-320f20caf140"))) {
                return false;
            }
        }
        return true;
    }),
    NO_8002(8002, player -> getBrokenBlockAmount(player) % 1000000 == 777777),

    // 特殊
    NO_9001(9001, player -> inDayOf(1, 1)),
    NO_9002(9002, player -> inDayOf(12, 25)),
    NO_9003(9003, player -> inDayOf(12, 31)),
    NO_9004(9004, player -> Calendar.getInstance().get(Calendar.MONTH) + 1 == 1),
    NO_9005(9005, player -> Calendar.getInstance().get(Calendar.MONTH) + 1 == 2),
    NO_9006(9006, player -> inDayOf(2, 3)),
    NO_9007(9007, player -> inDayOf(2, 11)),
    NO_9008(9008, player -> inDayOf(2, 14)),
    NO_9009(9009, player -> Calendar.getInstance().get(Calendar.MONTH) + 1 == 3),
    NO_9010(9010, player -> inDayOf(3, 3)),
    NO_9011(9011, player -> inDayOf(3, 14)),
    NO_9012(9012, player -> inDayOf(3, 20)),
    NO_9013(9013, player -> Calendar.getInstance().get(Calendar.MONTH) + 1 == 4),
    NO_9014(9014, player -> inDayOf(4, 1)),
    NO_9015(9015, player -> inDayOf(4, 15)),
    NO_9016(9016, player -> inDayOf(4, 22)),
    NO_9017(9017, player -> Calendar.getInstance().get(Calendar.MONTH) + 1 == 5),
    NO_9018(9018, player -> inDayOf(5, 5)),
    NO_9019(9019, player -> inDayOf(5, 5)), // missing?
    NO_9020(9020, player -> isWeekday(Month.MAY, 2, DayOfWeek.SUNDAY)),
    NO_9021(9021, player -> Calendar.getInstance().get(Calendar.MONTH) + 1 == 6),
    NO_9022(9022, player -> inDayOf(6, 12)),
    NO_9023(9023, player -> inDayOf(6, 17)),
    NO_9024(9024, player -> inDayOf(6, 29)),
    NO_9025(9025, player -> Calendar.getInstance().get(Calendar.MONTH) + 1 == 7),
    NO_9026(9026, player -> inDayOf(7, 7)),
    NO_9027(9027, player -> inDayOf(7, 17)),
    NO_9028(9028, player -> inDayOf(7, 29)),
    NO_9029(9029, player -> Calendar.getInstance().get(Calendar.MONTH) + 1 == 8),
    NO_9030(9030, player -> inDayOf(8, 7)),
    NO_9031(9031, player -> inDayOf(8, 16)),
    NO_9032(9032, player -> inDayOf(8, 29)),
    NO_9033(9033, player -> Calendar.getInstance().get(Calendar.MONTH) + 1 == 9),
    NO_9034(9034, player -> inDayOf(9, 2)),
    NO_9035(9035, player -> inDayOf(9, 12)),
    NO_9036(9036, player -> inDayOf(9, 29)),
    ;

    private final int id;
    private final Predicate<Player> condition;

    SeichiAchievement(int id, Predicate<Player> condition) {
        this.id = id;
        this.condition = condition;
    }

    public void achieve(Player player) {
        PlayerData playerData = getPlayerData(player);
        if (playerData.getTitleFlags().get(id)) return;

        if (!condition.test(player)) {
            // TODO: this shouldn't be here
            if (9000 < id && id < 10000) player.sendMessage("実績No" + id + "は条件を満たしていません。");
        } else {
            playerData.getTitleFlags().set(id);
            player.sendMessage("実績No" + id + "解除！おめでとうございます！");
        }
    }

    public static void tryAchieve(Player player, int id) {
        PlayerData playerData = getPlayerData(player);
        Optional<SeichiAchievement> optionalAchievement = Arrays.stream(SeichiAchievement.values())
                .filter(achievement -> achievement.id == id)
                .findFirst();

        // 予約配布システム
        if (7000 < id && id < 8000) {
            playerData.getTitleFlags().set(id);
            player.sendMessage("【実績システム】運営チームよりNo" + id + "の二つ名がプレゼントされました。");
        } else {
            optionalAchievement.ifPresent(seichiAchievement -> seichiAchievement.achieve(player));
        }
    }

    /**
     * 指定された月日かどうかを返します。
     *
     * @param month 指定する月。例: バレンタインなら {@code 2}
     * @param day   指定する日。例: バレンタインなら {@code 11}
     * @return 指定された月日かどうか
     */
    private static boolean inDayOf(int month, int day) {
        final LocalDate date = LocalDate.now();
        return date.getMonth().getValue() == month && date.getDayOfMonth() == day;
    }

    private static int getRank(Player player) {
        return getPlayerData(player).calcPlayerRank(player);
    }

    private static long getBrokenBlockAmount(Player player) {
        return getPlayerData(player).getTotalbreaknum();
    }

    private static long getSpentTicks(Player player) {
        return getPlayerData(player).getPlaytick();
    }

    private static int getDaysChaining(Player player) {
        return getPlayerData(player).getChainJoin();
    }

    private static int getTotalPlayedDays(Player player) {
        return getPlayerData(player).getTotalJoin();
    }

    private static int getVotingCounts(Player player) {
        return getPlayerData(player).getP_vote_forT();
    }

    private static PlayerData getPlayerData(Player player) {
        return SeichiAssist.playermap.get(player.getUniqueId());
    }

    /**
     今日が{@code month}月の{@code weeks}週の{@code weekday}曜日かを判定する。
     @param month 月
     @param weeks 月の中で第何週目か。1-5までが受け付けられる
     @param weekday 何曜日か。
     @return 今日が{@code month}月の{@code weeks}週の{@code weekday}曜日かを判定するならtrue、そうでないならfalse
     */
    private static boolean isWeekday(final Month month, final int weeks, final DayOfWeek weekday) {
        if (weeks < 1 || weeks > 5) {
            throw new IllegalArgumentException("weeks requires in 1..5");
        }
        final LocalDate now = LocalDate.now();
        // そもそも月が違うならfalse
        if (now.getMonth() != month) return false;
        // 今日と指定されている曜日が違うならfalse
        if (now.getDayOfWeek() != weekday) return false;
        // 第一週目ならずらさなくていい
        if (weeks == 1) {
            return true;
        }
        // 月は同じ、曜日も一緒、第一週目ではない
        // ここで今月の第n週目のm曜日を求めて現在と同一性を比較する、それでおしまい。
        return now.equals(
                now.with(TemporalAdjusters.firstDayOfMonth())
                        .with(TemporalAdjusters.dayOfWeekInMonth(weeks, weekday))
        );
    }

}
