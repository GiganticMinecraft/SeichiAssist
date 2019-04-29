package com.github.unchama.seichiassist;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

import javax.annotation.Nonnull;

public enum Worlds {
    WORLD_SPAWN("world_spawn", "スポーンワールド", false, false),
    WORLD("world", "メインワールド", false, true),
    WORLD_2("world_2", "メインワールド", false, true),
    WORLD_SW("world_SW", "第一整地ワールド", true, true),
    WORLD_SW_2("world_SW_2", "第二整地ワールド", true, true),
    WORLD_SW_3("world_SW_3", "第三整地ワールド", true, true),
    WORLD_SW_NETHER("world_SW_nether", "整地ネザー", true, true),
    WORLD_SW_END("world_SW_the_end", "整地エンド", true, true),
    WORLD_SW_ZERO("world_SW_zero", "整地ゼロワールド", true, false),
    WORLD_TT("world_TT", "地上TTワールド", false, true),
    WORLD_TT_NETHER("world_nether_TT", "ネザーTTワールド", false, true),
    WORLD_TT_END("world_the_end_TT", "エンドTTワールド", false, true);

    private final String alphabetName;
    private final String japaneseName;
    private final boolean isSeichi;
    private final boolean isInvocableSkill;

    Worlds(String alphabetName, String japaneseName, boolean isSeichi, boolean isInvocableSkill) {
        this.alphabetName = alphabetName;
        this.japaneseName = japaneseName;
        this.isSeichi = isSeichi;
        this.isInvocableSkill = isInvocableSkill;
    }

    /**
     * 整地ワールドの英語名の {@link ImmutableList}
     */
    public static ImmutableList<String> seichiWorldNames =
        Lists.immutable.of(Worlds.values())
                       .select(Worlds::isSeichi)
                       .collect(Worlds::getAlphabetName);

    /**
     * スキル発動可能ワールドの英語名の {@link ImmutableList}
     */
    public static ImmutableList<String> invocableSkillWorldNames =
        Lists.immutable.of(Worlds.values())
                       .select(Worlds::isInvocableSkill)
                       .collect(Worlds::getAlphabetName);

    /**
     * 整地ワールドかどうかを返します.
     *
     * @return {@code true} : 整地ワールド / {@code false} : 整地ワールドではない
     */
    public boolean isSeichi() {
        return isSeichi;
    }

    /**
     * ワールドの日本語名を返します.
     *
     * @return ワールドの日本語名
     */
    @Nonnull
    public String getJapaneseName() {
        return japaneseName;
    }

    /**
     * ワールドの英語名を返します.
     *
     * @return ワールドの英語名
     */
    @Nonnull
    public String getAlphabetName() {
        return alphabetName;
    }

    /**
     * スキル発動可能ワールドかどうかを返します.
     *
     * @return {@code true} : スキル発動可能ワールド / {@code false} : スキル発動不可ワールド
     */
    public boolean isInvocableSkill() {
        return isInvocableSkill;
    }
}
