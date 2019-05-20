package com.github.unchama.seichiassist.data.descrptions;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RankData;
import com.github.unchama.seichiassist.text.Templates;
import com.github.unchama.seichiassist.text.Text;
import com.github.unchama.seichiassist.text.Warnings;
import com.github.unchama.seichiassist.util.TypeConverter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by karayuu on 2019/05/05
 */
public final class PlayerInformationDescriptions {
    private PlayerInformationDescriptions() {}

    /**
     * Player統計のLoreを返します.
     * @param playerData {@link Player} の {@link PlayerData} ({@code null} は許容されません)
     */
    @Nonnull
    public static List<Text> playerInfoLore(@NotNull PlayerData playerData) {
            List<Text> lore = new ArrayList<>();

            //TODO: 値とともに説明文を持つようにしたい...playerDataを引数にいちいち与えるのはめんどくさい
            lore.add(seichiLevelDescription(playerData));
            lore.add(remainLevelDescription(playerData));
            lore.addAll(Warnings.seichiWorldWarning(playerData.player));
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
    @Nonnull
    private static Text seichiLevelDescription(@NotNull PlayerData playerData) {
        if (playerData.starlevel <= 0) {
            return Text.of("整地レベル:" + playerData.level, ChatColor.AQUA);
        } else {
            return Text.of("整地レベル:" + playerData.level + "☆" + playerData.starlevel, ChatColor.AQUA);
        }
    }

    /**
     * 次のレベルまでの残り必要整地量の説明文
     * レベルが {@link SeichiAssist#levellist} で指定された最大レベルを超えている場合, {@code null} を返します.
     */
    @Nullable
    private static Text remainLevelDescription(@NotNull PlayerData playerData) {
        if (playerData.level < SeichiAssist.levellist.size()) {
            return Text.of("次のレベルまで:" +
                (SeichiAssist.levellist.get(playerData.level) - playerData.totalbreaknum), ChatColor.AQUA);
            //TODO:この計算は,ここにあるべきではない.
        } else {
            return null;
        }
    }

    /**
     * パッシブスキルの説明文
     */
    @Nonnull
    private static List<Text> passiveSkillDescription(@NotNull PlayerData playerData) {
        return Arrays.asList(
            Text.of("パッシブスキル効果：", ChatColor.DARK_GRAY),
            Text.of("1ブロック整地ごとに", ChatColor.DARK_GRAY),
            Text.of(PlayerData.passiveSkillProbability + "%の確率で", ChatColor.DARK_GRAY),
            Text.of(playerData.dispPassiveExp() + "のマナを獲得", ChatColor.DARK_GRAY)
        );
    }

    /**
     * 総整地量の説明文
     */
    @Nonnull
    private static Text totalBreakAmountDescription(@Nonnull PlayerData playerData) {
        return Text.of("総整地量：" + playerData.totalbreaknum, ChatColor.AQUA);
    }

    /**
     * ランキングの順位の説明文
     */
    @Nonnull
    private static Text rankingDescription(@Nonnull PlayerData playerData) {
        return Text.of("ランキング：" + playerData.playerRankingPosition() + "位", ChatColor.GOLD)
                   .also(Text.of("(" + SeichiAssist.ranklist.size() + "人中)", ChatColor.GRAY));
    }

    /**
     * 一つ前のランキングのプレイヤーとの整地量の差を表す説明文を返します.
     * ただし,1位のときは {@code null} を返します.
     *
     * @return 説明文
     */
    @Nullable
    private static Text rankingDiffDescription(@NotNull PlayerData playerData) {
        if (playerData.playerRankingPosition() > 1) {
            final int playerRanking = playerData.playerRankingPosition();
            final RankData rankData = SeichiAssist.ranklist.get(playerRanking - 2);
            return Text.of((playerRanking - 1) + "位(" + rankData.name + ")との差：" + (rankData.totalbreaknum - playerData.totalbreaknum), ChatColor.AQUA);
            //TODO: この計算はここにあるべきではない.
        } else {
            return null;
        }
    }

    /**
     * 総ログイン時間の説明文
     */
    @Nonnull
    private static Text totalLoginTimeDescrpition(@Nonnull PlayerData playerData) {
        return Text.of("総ログイン時間：" + TypeConverter.toTimeString(TypeConverter.toSecond(playerData.playtick)), ChatColor.GRAY);
    }

    /**
     * 通算ログイン日数の説明文
     */
    @Nonnull
    private static Text totalLoginDaysDescrption(@Nonnull PlayerData playerData) {
        return Text.of("通算ログイン日数：" + playerData.TotalJoin + "日", ChatColor.GRAY);
    }

    /**
     * 連続ログイン日数の説明文
     */
    @Nonnull
    private static Text totalChainLoginDaysDescription(@Nonnull PlayerData playerData) {
        return Text.of("連続ログイン日数：" + playerData.ChainJoin + "日", ChatColor.GRAY);
    }

    /**
     * 連続投票日数の説明文. ただし, {@link PlayerData#ChainVote} が 0の場合は {@code null} を返します.
     */
    @Nullable
    private static Text totalChainVoteDaysDescription(@Nonnull PlayerData playerData) {
        if (playerData.ChainVote > 0) {
            return Text.of("連続投票日数：" + playerData.ChainVote + "日", ChatColor.GRAY);
        } else {
            return null;
        }
    }

    /**
     * Expバーの説明文.
     */
    @Nonnull
    private static List<Text> expBarDescription(@Nonnull PlayerData playerData) {
        if (playerData.expbar.isVisible()) {
            return Arrays.asList(
                Text.of("整地量バーを表示", ChatColor.GREEN),
                Text.of("クリックで非表示", ChatColor.UNDERLINE, ChatColor.DARK_RED)
            );
        } else {
            return Arrays.asList(
                Text.of("整地量バーを非表示", ChatColor.RED),
                Text.of("クリックで表示", ChatColor.UNDERLINE, ChatColor.DARK_GREEN)
            );
        }
    }
}
