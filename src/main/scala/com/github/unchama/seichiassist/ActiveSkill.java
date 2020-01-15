package com.github.unchama.seichiassist;

import com.github.unchama.seichiassist.data.XYZTuple;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;

public enum ActiveSkill {
    ARROW(1, "", "", "", "エビフライ・ドライブ", "ホーリー・ショット", "ツァーリ・ボンバ", "アーク・ブラスト", "ファンタズム・レイ", "スーパー・ノヴァ", ""),
    MULTI(2, "", "", "", "トム・ボウイ", "サンダー・ストーム", "スターライト・ブレイカー", "アース・ディバイド", "ヘブン・ゲイボルグ", "ディシジョン", ""),
    BREAK(3, "デュアル・ブレイク", "トリアル・ブレイク", "エクスプロージョン", "ミラージュ・フレア", "ドッ・カーン", "ギガンティック・ボム", "ブリリアント・デトネーション", "レムリア・インパクト", "エターナル・ヴァイス", ""),
    WATERCONDENSE(4, "", "", "", "", "", "", "ホワイト・ブレス", "アブソリュート・ゼロ", "ダイアモンド・ダスト", ""),
    LAVACONDENSE(5, "", "", "", "", "", "", "ラヴァ・コンデンセーション", "モエラキ・ボールダーズ", "エルト・フェットル", ""),
    ARMOR(6, "", "", "", "", "", "", "", "", "", "アサルト・アーマー"),
    FLUIDCONDENSE(7, "", "", "", "", "", "", "", "", "", "ヴェンダー・ブリザード"),
    ;
    public final int typenum;
    private String lv1name;
    private String lv2name;
    private String lv3name;
    private String lv4name;
    private String lv5name;
    private String lv6name;
    private String lv7name;
    private String lv8name;
    private String lv9name;
    private String lv10name;


    ActiveSkill(int typenum, String lv1name, String lv2name, String lv3name, String lv4name, String lv5name, String lv6name, String lv7name, String lv8name, String lv9name, String lv10name) {
        this.typenum = typenum;
        this.lv1name = lv1name;
        this.lv2name = lv2name;
        this.lv3name = lv3name;
        this.lv4name = lv4name;
        this.lv5name = lv5name;
        this.lv6name = lv6name;
        this.lv7name = lv7name;
        this.lv8name = lv8name;
        this.lv9name = lv9name;
        this.lv10name = lv10name;

    }

    //与えられたスキル種類とレベルに応じて名前を返す
    public static String getActiveSkillName(int typenum, int skilllevel) {
        // 列挙定数を取得
        ActiveSkill[] activeskill = ActiveSkill.values();
        String str;
        if (typenum == 0) {
            str = "未設定";
            return str;
        }
        switch (skilllevel) {
            case 0:
                str = "未設定";
                break;
            case 1:
                str = activeskill[typenum - 1].getLv1Name();
                break;
            case 2:
                str = activeskill[typenum - 1].getLv2Name();
                break;
            case 3:
                str = activeskill[typenum - 1].getLv3Name();
                break;
            case 4:
                str = activeskill[typenum - 1].getLv4Name();
                break;
            case 5:
                str = activeskill[typenum - 1].getLv5Name();
                break;
            case 6:
                str = activeskill[typenum - 1].getLv6Name();
                break;
            case 7:
                str = activeskill[typenum - 1].getLv7Name();
                break;
            case 8:
                str = activeskill[typenum - 1].getLv8Name();
                break;
            case 9:
                str = activeskill[typenum - 1].getLv9Name();
                break;
            case 10:
                str = activeskill[typenum - 1].getLv10Name();
                break;
            default:
                str = "エラー";
                break;
        }
        return str;
    }

    public static int getActiveSkillUseExp(int typenum, int skilllevel) {
        int exp = 0;
        if (typenum == ActiveSkill.ARROW.gettypenum()) {
            switch (skilllevel) {
                case 4:
                    exp = 18;
                    break;
                case 5:
                    exp = 35;
                    break;
                case 6:
                    exp = 80;
                    break;
                case 7:
                    exp = 110;
                    break;
                case 8:
                    exp = 220;
                    break;
                case 9:
                    exp = 380;
                    break;
                default:
                    break;
            }
        } else if (typenum == ActiveSkill.MULTI.gettypenum()) {
            switch (skilllevel) {
                case 4:
                    exp = 28;
                    break;
                case 5:
                    exp = 65;
                    break;
                case 6:
                    exp = 90;
                    break;
                case 7:
                    exp = 185;
                    break;
                case 8:
                    exp = 330;
                    break;
                case 9:
                    exp = 480;
                    break;
                default:
                    break;
            }
        } else if (typenum == ActiveSkill.BREAK.gettypenum()) {
            switch (skilllevel) {
                case 1:
                    exp = 1;
                    break;
                case 2:
                    exp = 3;
                    break;
                case 3:
                    exp = 12;
                    break;
                case 4:
                    exp = 30;
                    break;
                case 5:
                    exp = 70;
                    break;
                case 6:
                    exp = 100;
                    break;
                case 7:
                    exp = 200;
                    break;
                case 8:
                    exp = 350;
                    break;
                case 9:
                    exp = 500;
                    break;
                default:
                    break;
            }
        } else if (typenum == ActiveSkill.WATERCONDENSE.gettypenum()) {
            switch (skilllevel) {
                case 7:
                    exp = 30;
                    break;
                case 8:
                    exp = 80;
                    break;
                case 9:
                    exp = 160;
                    break;
                default:
                    break;
            }
        } else if (typenum == ActiveSkill.LAVACONDENSE.gettypenum()) {
            switch (skilllevel) {
                case 7:
                    exp = 20;
                    break;
                case 8:
                    exp = 60;
                    break;
                case 9:
                    exp = 150;
                    break;
                default:
                    break;
            }
        } else if (typenum == ActiveSkill.ARMOR.gettypenum()) {
            exp = 600;
        } else if (typenum == ActiveSkill.FLUIDCONDENSE.gettypenum()) {
            exp = 170;
        }
        return exp;
    }

    @Deprecated public int gettypenum() {
        return this.typenum;
    }

    private String getLv1Name() {
        return this.lv1name;
    }

    private String getLv2Name() {
        return this.lv2name;
    }

    private String getLv3Name() {
        return this.lv3name;
    }

    private String getLv4Name() {
        return this.lv4name;
    }

    private String getLv5Name() {
        return this.lv5name;
    }

    private String getLv6Name() {
        return this.lv6name;
    }

    private String getLv7Name() {
        return this.lv7name;
    }

    private String getLv8Name() {
        return this.lv8name;
    }

    private String getLv9Name() {
        return this.lv9name;
    }

    private String getLv10Name() {
        return this.lv10name;
    }

    public int getRepeatTimes(int skilllevel) {
        int repeattimes = 1;
        switch (this) {
            case MULTI:
                switch (skilllevel) {
                    case 4:
                        repeattimes = 3;
                        break;
                    case 5:
                        repeattimes = 7;
                        break;
                    case 6:
                        repeattimes = 3;
                        break;
                    case 7:
                        repeattimes = 5;
                        break;
                    case 8:
                        repeattimes = 3;
                        break;
                    case 9:
                        repeattimes = 7;
                        break;
                }
                break;
        }
        return repeattimes;
    }

    public XYZTuple getBreakLength(int skilllevel) {
        XYZTuple breaklength = new XYZTuple(0, 0, 0);
        switch (this) {
            case ARROW:
                switch (skilllevel) {
                    case 4:
                        breaklength = new XYZTuple(3, 3, 3);
                        break;
                    case 5:
                        breaklength = new XYZTuple(5, 3, 5);
                        break;
                    case 6:
                        breaklength = new XYZTuple(7, 5, 7);
                        break;
                    case 7:
                        breaklength = new XYZTuple(9, 7, 9);
                        break;
                    case 8:
                        breaklength = new XYZTuple(11, 9, 11);
                        break;
                    case 9:
                        breaklength = new XYZTuple(13, 11, 13);
                        break;
                }
                break;
            case MULTI:
                switch (skilllevel) {
                    case 4:
                        breaklength = new XYZTuple(3, 3, 3);
                        break;
                    case 5:
                        breaklength = new XYZTuple(3, 3, 3);
                        break;
                    case 6:
                        breaklength = new XYZTuple(5, 5, 5);
                        break;
                    case 7:
                        breaklength = new XYZTuple(5, 5, 5);
                        break;
                    case 8:
                        breaklength = new XYZTuple(7, 7, 7);
                        break;
                    case 9:
                        breaklength = new XYZTuple(7, 7, 7);
                        break;
                }
                break;
            case BREAK:
                switch (skilllevel) {
                    case 1:
                        breaklength = new XYZTuple(1, 2, 1);
                        break;
                    case 2:
                        breaklength = new XYZTuple(3, 2, 1);
                        break;
                    case 3:
                        breaklength = new XYZTuple(3, 3, 3);
                        break;
                    case 4:
                        breaklength = new XYZTuple(5, 3, 5);
                        break;
                    case 5:
                        breaklength = new XYZTuple(7, 5, 7);
                        break;
                    case 6:
                        breaklength = new XYZTuple(9, 7, 9);
                        break;
                    case 7:
                        breaklength = new XYZTuple(11, 9, 11);
                        break;
                    case 8:
                        breaklength = new XYZTuple(13, 11, 13);
                        break;
                    case 9:
                        breaklength = new XYZTuple(15, 13, 15);
                        break;
                }
                break;
            case WATERCONDENSE:
                switch (skilllevel) {
                    case 7:
                        breaklength = new XYZTuple(7, 7, 7);
                        break;
                    case 8:
                        breaklength = new XYZTuple(11, 11, 11);
                        break;
                    case 9:
                        breaklength = new XYZTuple(15, 15, 15);
                        break;
                }
                break;
            case LAVACONDENSE:
                switch (skilllevel) {
                    case 7:
                        breaklength = new XYZTuple(7, 7, 7);
                        break;
                    case 8:
                        breaklength = new XYZTuple(9, 9, 9);
                        break;
                    case 9:
                        breaklength = new XYZTuple(11, 11, 11);
                        break;
                }
                break;
            case ARMOR:
                breaklength = new XYZTuple(11, 11, 11);
                break;
            case FLUIDCONDENSE:
                breaklength = new XYZTuple(11, 11, 11);
        }
        return breaklength;
    }

    public long getCoolDown(int skillnum) {
        double cooldowntime = 0;
        switch (this) {
            case ARROW:
                switch (skillnum) {
                    case 4:
                        cooldowntime = 0.2;
                        break;
                    case 5:
                        cooldowntime = 1.3;
                        break;
                    case 6:
                        cooldowntime = 1.6;
                        break;
                    case 7:
                        cooldowntime = 2.7;
                        break;
                    case 8:
                        cooldowntime = 3.8;
                        break;
                    case 9:
                        cooldowntime = 5.5;
                        break;
                }
                break;
            case MULTI:
                switch (skillnum) {
                    case 4:
                        cooldowntime = 0.6;
                        break;
                    case 5:
                        cooldowntime = 1.4;
                        break;
                    case 6:
                        cooldowntime = 2.4;
                        break;
                    case 7:
                        cooldowntime = 3.4;
                        break;
                    case 8:
                        cooldowntime = 4.8;
                        break;
                    case 9:
                        cooldowntime = 6.8;
                        break;
                }
                break;
            case BREAK:
                switch (skillnum) {
                    case 1:
                        cooldowntime = 0;
                        break;
                    case 2:
                        cooldowntime = 0;
                        break;
                    case 3:
                        cooldowntime = 0;
                        break;
                    case 4:
                        cooldowntime = 0.7;
                        break;
                    case 5:
                        cooldowntime = 1.5;
                        break;
                    case 6:
                        cooldowntime = 2.5;
                        break;
                    case 7:
                        cooldowntime = 3.5;
                        break;
                    case 8:
                        cooldowntime = 5.0;
                        break;
                    case 9:
                        cooldowntime = 7.0;
                        break;
                }
                break;
        }
        return (long) (cooldowntime * 20);
    }

    public Material getMaterial(int skilllevel) {
        Material material = Material.AIR;
        switch (this) {
            case ARROW:
                material = Material.TIPPED_ARROW;
                break;
            case MULTI:
                switch (skilllevel) {
                    case 4:
                        material = Material.SADDLE;
                        break;
                    case 5:
                        material = Material.MINECART;
                        break;
                    case 6:
                        material = Material.STORAGE_MINECART;
                        break;
                    case 7:
                        material = Material.POWERED_MINECART;
                        break;
                    case 8:
                        material = Material.EXPLOSIVE_MINECART;
                        break;
                    case 9:
                        material = Material.HOPPER_MINECART;
                        break;
                }
                break;
            case BREAK:
                switch (skilllevel) {
                    case 1:
                        material = Material.GRASS;
                        break;
                    case 2:
                        material = Material.STONE;
                        break;
                    case 3:
                        material = Material.COAL_ORE;
                        break;
                    case 4:
                        material = Material.IRON_ORE;
                        break;
                    case 5:
                        material = Material.GOLD_ORE;
                        break;
                    case 6:
                        material = Material.REDSTONE_ORE;
                        break;
                    case 7:
                        material = Material.LAPIS_ORE;
                        break;
                    case 8:
                        material = Material.EMERALD_ORE;
                        break;
                    case 9:
                        material = Material.DIAMOND_ORE;
                        break;
                }
                break;
            case WATERCONDENSE:
                switch (skilllevel) {
                    case 7:
                        material = Material.SNOW_BLOCK;
                        break;
                    case 8:
                        material = Material.ICE;
                        break;
                    case 9:
                        material = Material.PACKED_ICE;
                        break;
                }
                break;
            case LAVACONDENSE:
                switch (skilllevel) {
                    case 7:
                        material = Material.NETHERRACK;
                        break;
                    case 8:
                        material = Material.NETHER_BRICK;
                        break;
                    case 9:
                        material = Material.MAGMA;
                        break;
                }
                break;
            case ARMOR:
                material = Material.DIAMOND_CHESTPLATE;
                break;
            case FLUIDCONDENSE:
                material = Material.NETHER_STAR;
        }
        return material;
    }

    public String getName(int skilllevel) {
        String str;
        if (typenum == 0) {
            str = "未設定";
            return str;
        }
        switch (skilllevel) {
            case 0:
                str = "未設定";
                break;
            case 1:
                str = getLv1Name();
                break;
            case 2:
                str = getLv2Name();
                break;
            case 3:
                str = getLv3Name();
                break;
            case 4:
                str = getLv4Name();
                break;
            case 5:
                str = getLv5Name();
                break;
            case 6:
                str = getLv6Name();
                break;
            case 7:
                str = getLv7Name();
                break;
            case 8:
                str = getLv8Name();
                break;
            case 9:
                str = getLv9Name();
                break;
            case 10:
                str = getLv10Name();
                break;
            default:
                str = "エラー";
                break;
        }
        return str;
    }


    public PotionType getPotionType(int skilllevel) {
        PotionType potiontype = PotionType.WATER;
        switch (this) {
            case ARROW:
                switch (skilllevel) {
                    case 4:
                        potiontype = PotionType.REGEN;
                        break;
                    case 5:
                        potiontype = PotionType.FIRE_RESISTANCE;
                        break;
                    case 6:
                        potiontype = PotionType.INSTANT_HEAL;
                        break;
                    case 7:
                        potiontype = PotionType.NIGHT_VISION;
                        break;
                    case 8:
                        potiontype = PotionType.SPEED;
                        break;
                    case 9:
                        potiontype = PotionType.INSTANT_DAMAGE;
                        break;
                }
                break;
        }
        return potiontype;
    }
}
