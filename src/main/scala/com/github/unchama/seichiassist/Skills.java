package com.github.unchama.seichiassist;

// TODO: すべてのスキルについて埋める
public enum Skills {
    // SPECIAL
    NONE("[この表示を目にした場合はバグです]", 0, 0, 0, 99999, 99999, null, Kind.SINGLE),
    ASSAULT(/* TODO */),
    VENDER_BRIZARD(/* TODO */),
    // SINGLE
    DUAL_BREAK("デュアル・ブレイク", 1, 2, 0.0, 1, 10, NONE, Kind.SINGLE),
    TRIALE_BREAK("トリアル・ブレイク", 3, 2, 0.0, 3, 20, DUAL_BREAK, Kind.SINGLE),
    EXPLOSION("エクスプロージョン", 3, 3, 3, 12, 30, TRIALE_BREAK, Kind.SINGLE),
    MIRRORJUE_FLARE("ミラージュ・フレア", 5, 5, 3, 0.7, 30, 40, EXPLOSION, Kind.SINGLE),
    BO_MB("ドッ・カーン", 7, 7, 5, 1.5, 70, 50, MIRRORJUE_FLARE, Kind.SINGLE),
    GIGANTIC_BOMB("ギガンティック・ボム", 9, 9, 7, 2.5, 100, 60, BO_MB, Kind.SINGLE),
    BRILLIANT_DETONATION("ブリリアント・デトネーション", 11, 11, 9, 3.5, 200, 70, GIGANTIC_BOMB, Kind.SINGLE),
    REMLIA_IMPACT("レムリア・インパクト", 13, 13, 11, 5.0, 350, 80, BRILLIANT_DETONATION, Kind.SINGLE),
    ETERNAL_VAIS("エターナル・ヴァイス", 15, 15, 13, 7.0, 500, 90, REMLIA_IMPACT, Kind.SINGLE),
    // MULTI
    TOM_BOY("トム・ボウイ", 3, 3, 3, 3, 0.6, 28, 40, EXPLOSION, Kind.MULTI),
    THUNDER_STORM("サンダーストーム", 3, 3, 3, 7, 1.4, 65, 50, TOM_BOY, Kind.MULTI),
    STARLIGHT_BREAKER("スターライト・ブレイカー", 5, 5, 5, 3, 2.4, 90, 60, THUNDER_STORM, Kind.MULTI),
    EARTH_DIVIDE("アース・ディバイド", 5, 5, 5, 5, 3.4, 185, 70, STARLIGHT_BREAKER, Kind.MULTI),
    HEAVEN_GAYBOLG("ヘヴン・ゲイボルグ", 7, 7, 7, 3, 4.8, 330, 80, EARTH_DIVIDE, Kind.MULTI),
    DECISION("ディシジョン", 7, 7, 7, 7, 6.8, 480, 90, HEAVEN_GAYBOLG, Kind.FAR),

    // FAR
    EBIFURAI_DORAIBU("エビフライ・ドライブ", 3, 3, 3, 0.2, 18, 40, EXPLOSION, Kind.FAR),
    HORI_SHOT("ホーリー・ショット", 5, 5, 3, 1.3, 35, 50, EBIFURAI_DORAIBU, Kind.FAR),
    TUA_BOMBA("ツァーリ・ボンバ", 7, 7, 5, 1.6, 80, 60, HORI_SHOT, Kind.FAR),
    ARC_BURST("アーク・ブラスト", 9, 9, 7, 2.7, 110, 70, TUA_BOMBA, Kind.FAR),
    FANTAZM_RAY("ファンタズム・レイ", 11, 11, 9, 3.8, 220, 80, ARC_BURST, Kind.FAR),
    SUPER_NOVA("スーパー・ノヴァ", 13, 13, 11, 5.5, 380, 90, FANTAZM_RAY, Kind.FAR),
    ;

    Skills() {

    }

    /**
     * <code>rangeW</code> * <code>rangeH</code> * <code>rangeD</code>で示される。
     *
     * @param name   名前。
     * @param rangeW 適用される (プレイヤーから見たときの) 幅。
     * @param rangeH 適用される (プレイヤーから見たときの) 高さ。
     * @param rangeD 適用される (プレイヤーから見たときの) 奥行き。
     */
    Skills(String name, int rangeW, int rangeH, int rangeD, double coolDown, int spendMana, int requiredActiveSkillPoint, Skills beforeSkill, Skills.Kind kind) {

    }

    Skills(String name, int rangeW, int rangeH, int rangeD, int repeat, double coolDown, int spendMana, int requiredActiveSkillPoint, Skills beforeSkill, Skills.Kind kind) {

    }

    Skills(String name, int rangeW, int rangeH, double coolDown, int spendMana, int requiredActiveSkillPoint, Skills beforeSkill, Skills.Kind kind) {

    }

    public enum Kind {
        SINGLE,
        MULTI,
        /**
         * 遠距離型。例: エビフライ
         */
        FAR,
    }
}