package com.github.unchama.seichiassist.data.descrptions;

import com.github.unchama.seichiassist.LevelThresholds;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RankData;
import com.github.unchama.seichiassist.text.Templates;
import com.github.unchama.seichiassist.text.Warnings;
import com.github.unchama.seichiassist.util.TypeConverter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bukkit.ChatColor.*;

/**
 * Created by karayuu on 2019/05/05
 */
public final class PlayerInformationDescriptions {
    private PlayerInformationDescriptions() {
    }

    /**
     * Player統計のLoreを返します.
     *
     * @param playerData {@link Player} の {@link PlayerData} ({@code null} は許容されません)
     */
    @NotNull
    public static List<String> playerInfoLore(@NotNull PlayerData playerData) {
        List<String> lore = new ArrayList<>();

        //TODO: 値とともに説明文を持つようにしたい...playerDataを引数にいちいち与えるのはめんどくさい
        lore.add(seichiLevelDescription(playerData));
        lore.add(remainLevelDescription(playerData));
        lore.addAll(Warnings.noRewardsOutsideSeichiWorld(playerData.getPlayer()));
        lore.addAll(passiveSkillDescription(playerData));
        lore.add(totalBreakAmountDescription(playerData));
        lore.add(rankingDescription(playerData));
        lore.add(rankingDiffDescription(playerData));
        lore.add(totalLoginTimeDescrpition(playerData));
        lore.add(totalLoginDaysDescrption(playerData));
        lore.add(totalChainLoginDaysDescription(playerData));
        lore.add(totalChainVoteDaysDescription(playerData));
        lore.addAll(Templates.playerInfoDescrpition);
        lore.addAll(expBarDescription(playerData));

        return lore;
    }

    /**
     * 木の棒メニュー等で用いられる整地レベルの説明文
     * スターレベルを保持していたら,スターレベルも同時に表示します.
     */
    @NotNull
    private static String seichiLevelDescription(@NotNull PlayerData playerData) {
        final int starLevel = playerData.getTotalStarLevel();
        final int level = playerData.getLevel();

        if (starLevel <= 0) {
            return AQUA + "整地レベル:" + level;
        } else {
            return AQUA + "整地レベル:" + level + "☆" + starLevel;
        }
    }

    /**
     * 次のレベルまでの残り必要整地量の説明文
     * レベルが {@link LevelThresholds#levelExpThresholds} で指定された最大レベルを超えている場合, {@code null} を返します.
     */
    @Nullable
    private static String remainLevelDescription(@NotNull PlayerData playerData) {
        if (playerData.getLevel() < LevelThresholds.INSTANCE.getLevelExpThresholds().size()) {
            return AQUA + "次のレベルまで:" +
                (LevelThresholds.INSTANCE.getLevelExpThresholds().get(playerData.getLevel()) - playerData.getTotalbreaknum());
            //TODO:この計算は,ここにあるべきではない.
        } else {
            return null;
        }
    }

    /**
     * パッシブスキルの説明文
     */
    @NotNull
    private static List<String> passiveSkillDescription(@NotNull PlayerData playerData) {
        return Arrays.asList(
            DARK_GRAY + "パッシブスキル効果：",
            DARK_GRAY + "1ブロック整地ごとに",
            DARK_GRAY + "" + PlayerData.passiveSkillProbability + "%の確率で",
            DARK_GRAY + "" + playerData.getPassiveExp() + "のマナを獲得"
        );
    }

    /**
     * 総整地量の説明文
     */
    @NotNull
    private static String totalBreakAmountDescription(@NotNull PlayerData playerData) {
        return AQUA + "総整地量：" + playerData.getTotalbreaknum();
    }

    /**
     * ランキングの順位の説明文
     */
    @NotNull
    private static String rankingDescription(@NotNull PlayerData playerData) {
        return GOLD + "ランキング：" + playerData.calcPlayerRank() + "位" +
            GRAY + "(" + SeichiAssist.Companion.getRanklist().size() + "人中)";
    }

    /**
     * 一つ前のランキングのプレイヤーとの整地量の差を表す説明文を返します.
     * ただし,1位のときは {@code null} を返します.
     *
     * @return 説明文
     */
    @Nullable
    private static String rankingDiffDescription(@NotNull PlayerData playerData) {
        if (playerData.calcPlayerRank() > 1) {
            final int playerRanking = playerData.calcPlayerRank();
            final RankData rankData = SeichiAssist.Companion.getRanklist().get(playerRanking - 2);
            return AQUA + "" + (playerRanking - 1) + "位(" + rankData.name + ")との差：" +
                (rankData.totalbreaknum - playerData.getTotalbreaknum());
            //TODO: この計算はここにあるべきではない.
        } else {
            return null;
        }
    }

    /**
     * 総ログイン時間の説明文
     */
    @NotNull
    private static String totalLoginTimeDescrpition(@NotNull PlayerData playerData) {
        return GRAY + "総ログイン時間：" +
            TypeConverter.toTimeString(TypeConverter.toSecond(playerData.getPlaytick()));
    }

    /**
     * 通算ログイン日数の説明文
     */
    @NotNull
    private static String totalLoginDaysDescrption(@NotNull PlayerData playerData) {
        return GRAY +"通算ログイン日数：" + playerData.getLoginStatus().getTotalLoginDay() + "日";
    }

    /**
     * 連続ログイン日数の説明文
     */
    @NotNull
    private static String totalChainLoginDaysDescription(@NotNull PlayerData playerData) {
        return GRAY + "連続ログイン日数：" + playerData.getLoginStatus().getChainLoginDay() + "日";
    }

    /**
     * 連続投票日数の説明文. ただし, {@link PlayerData#getChainVote()} が 0の場合は {@code null} を返します.
     */
    @Nullable
    private static String totalChainVoteDaysDescription(@NotNull PlayerData playerData) {
        if (playerData.getChainVote() > 0) {
            return "連続投票日数：" + playerData.getChainVote() + "日";
        } else {
            return null;
        }
    }

    /**
     * Expバーの説明文.
     */
    @NotNull
    private static List<String> expBarDescription(@NotNull PlayerData playerData) {
        if (playerData.getExpbar().isVisible()) {
            return Arrays.asList(
                GREEN + "整地量バーを表示",
                DARK_RED + "" + UNDERLINE + "クリックで非表示"
            );
        } else {
            return Arrays.asList(
                RED + "整地量バーを非表示",
                DARK_GREEN + "" + UNDERLINE + "クリックで表示"
            );
        }
    }
}
