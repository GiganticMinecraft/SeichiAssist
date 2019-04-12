package com.github.unchama.seichiassist;

public enum Worlds {
    WORLD_SPAWN("world_spawn", "スポーンワールド", false),
    WORLD("world", "メインワールド", false),
    WORLD_SW("world_SW", "第一整地ワールド", true),
    WORLD_SW_2("world_SW_2", "第二整地ワールド", true),
    WORLD_SW_3("world_SW_3", "第三整地ワールド", true),
    WORLD_SW_NETHER("world_SW_nether", "整地ネザー", true),
    WORLD_SW_END("world_SW_the_end", "整地エンド", true),
    ;

    private final String alphabetName;
    private final String japaneseName;
    private final boolean isSeichi;
    Worlds(String alphabetName, String japaneseName, boolean isSeichi) {
        this.alphabetName = alphabetName;
        this.japaneseName = japaneseName;
        this.isSeichi = isSeichi;
    }

    public boolean isSeichi() {
        return isSeichi;
    }

    public String getJapaneseName() {
        return japaneseName;
    }

    public String getAlphabetName() {
        return alphabetName;
    }
}
