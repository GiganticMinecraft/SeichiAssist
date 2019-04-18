package com.github.unchama.seichiassist.database.init.ddl;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;

/**
 * playerdataテーブルの初期化クエリを計算するクラス
 */
public class PlayerDataTableQueryGenerator {
    private final String tableReferenceName;
    private final Config config;

    public PlayerDataTableQueryGenerator(String tableReferenceName, Config config) {
        this.tableReferenceName = tableReferenceName;
        this.config = config;
    }

    public String generateCreateQuery() {
        //テーブルが存在しないときテーブルを新規作成
        return "CREATE TABLE IF NOT EXISTS " + tableReferenceName +
                "(name varchar(30) unique," +
                "uuid varchar(128) unique)";
    }

    public String generateColumnCreationQuery() {
        //必要なcolumnを随時追加
        String command =
                "alter table " + tableReferenceName +
                        " add column if not exists effectflag tinyint default 0" +
                        ",add column if not exists minestackflag boolean default true" +
                        ",add column if not exists messageflag boolean default false" +
                        ",add column if not exists activemineflagnum int default 0" +
                        ",add column if not exists assaultflag boolean default false" +
                        ",add column if not exists activeskilltype int default 0" +
                        ",add column if not exists activeskillnum int default 1" +
                        ",add column if not exists assaultskilltype int default 0" +
                        ",add column if not exists assaultskillnum int default 0" +
                        ",add column if not exists arrowskill int default 0" +
                        ",add column if not exists multiskill int default 0" +
                        ",add column if not exists breakskill int default 0" +
                        ",add column if not exists fluidcondenskill int default 0" +
                        ",add column if not exists watercondenskill int default 0" +
                        ",add column if not exists lavacondenskill int default 0" +
                        ",add column if not exists effectnum int default 0" +
                        ",add column if not exists gachapoint int default 0" +
                        ",add column if not exists gachaflag boolean default true" +
                        ",add column if not exists level int default 1" +
                        ",add column if not exists numofsorryforbug int default 0" +
                        ",add column if not exists inventory blob default null" +
                        ",add column if not exists rgnum int default 0" +
                        ",add column if not exists totalbreaknum bigint default 0" +
                        ",add column if not exists lastquit datetime default null" +
                        ",add column if not exists lastcheckdate varchar(12) default null" +
                        ",add column if not exists ChainJoin int default 0" +
                        ",add column if not exists TotalJoin int default 0" +
                        ",add column if not exists LimitedLoginCount int default 0" +
                        ",add column if not exists displayTypeLv boolean default true" +
                        ",add column if not exists displayTitleNo int default 0" +
                        ",add column if not exists displayTitle1No int default 0" +
                        ",add column if not exists displayTitle2No int default 0" +
                        ",add column if not exists displayTitle3No int default 0" +
                        ",add column if not exists TitleFlags text default null" +
                        ",add column if not exists giveachvNo int default 0" +
                        ",add column if not exists achvPointMAX int default 0" +
                        ",add column if not exists achvPointUSE int default 0" +
                        ",add column if not exists achvChangenum int default 0" +
                        ",add column if not exists starlevel int default 0" +
                        ",add column if not exists starlevel_Break int default 0" +
                        ",add column if not exists starlevel_Time int default 0" +
                        ",add column if not exists starlevel_Event int default 0" ;

        //MineStack関連をすべてfor文に変更
        if(SeichiAssist.minestack_sql_enable){
            for(int i=0; i<SeichiAssist.minestacklist.size(); i++){
                command += ",add column if not exists stack_" + SeichiAssist.minestacklist.get(i).getMineStackObjName() + " int default 0";
            }
        }

        command +=
                ",add column if not exists playtick int default 0" +
                        ",add column if not exists killlogflag boolean default false" +
                        ",add column if not exists worldguardlogflag boolean default true" +//

                        ",add column if not exists multipleidbreakflag boolean default false" +

                        ",add column if not exists pvpflag boolean default false" +
                        ",add column if not exists loginflag boolean default false" +
                        ",add column if not exists p_vote int default 0" +
                        ",add column if not exists p_givenvote int default 0" +
                        ",add column if not exists effectpoint int default 0" +
                        ",add column if not exists premiumeffectpoint int default 0" +
                        ",add column if not exists mana double default 0.0" +
                        ",add column if not exists expvisible boolean default true" +
                        ",add column if not exists totalexp int default 0" +
                        ",add column if not exists expmarge tinyint unsigned default 0" +
                        ",add column if not exists shareinv blob" +
                        ",add column if not exists everysound boolean default true" +
                        ",add column if not exists everymessage boolean default true" +

                        ",add column if not exists homepoint_" + config.getServerNum() + " varchar(" + config.getSubHomeMax() * SeichiAssist.SUB_HOME_DATASIZE + ") default ''"+
                        ",add column if not exists subhome_name_" + config.getServerNum() + " blob default null" +


                        //BuildAssistのデータ
                        ",add column if not exists build_lv int default 1" +//
                        ",add column if not exists build_count double default 0" +//
                        ",add column if not exists build_count_flg TINYINT UNSIGNED default 0" +//

                        ",add column if not exists anniversary boolean default false" +//

                        //投票妖精関連
                        ",add column if not exists canVotingFairyUse boolean default false" +//
                        ",add column if not exists newVotingFairyTime varchar(" + SeichiAssist.VOTE_FAIRYTIME_DATASIZE + ") default ''" +//
                        ",add column if not exists VotingFairyRecoveryValue int default 0" +//
                        ",add column if not exists hasVotingFairyMana int default 0"+//
                        ",add column if not exists toggleGiveApple int default 1"+//
                        ",add column if not exists toggleVotingFairy int default 1"+//
                        ",add column if not exists p_apple bigint default 0"+

                        //貢献pt関連
                        ",add column if not exists contribute_point int default 0"+//
                        ",add column if not exists added_mana int default 0" +

                        ",add column if not exists lastvote varchar(40) default null" +
                        ",add column if not exists chainvote int default 0" +

                        ",add column if not exists GBstage int default 0" +
                        ",add column if not exists GBexp int default 0" +
                        ",add column if not exists GBlevel int default 0" +
                        ",add column if not exists isGBStageUp boolean default false";

        for (int i = 0; i <= config.getTemplateKeepAmount() - 1; i++) {
            command += ",add column if not exists ahead_" + i + " int default 0";
            command += ",add column if not exists behind_" + i + " int default 0";
            command += ",add column if not exists right_" + i + " int default 0";
            command += ",add column if not exists left_" + i + " int default 0";
        }

        ActiveSkillEffect[] activeSkillEffects = ActiveSkillEffect.values();
        for (final ActiveSkillEffect activeSkillEffect : activeSkillEffects) {
            command += ",add column if not exists " + activeSkillEffect.getsqlName() + " boolean default false";
        }

        ActiveSkillPremiumEffect[] premiumEffects = ActiveSkillPremiumEffect.values();
        for (final ActiveSkillPremiumEffect activeSkillPremiumEffect : premiumEffects) {
            command +=  ",add column if not exists " + activeSkillPremiumEffect.getsqlName() + " boolean default false";
        }

        //正月Event用
        command += ",add column if not exists hasNewYearSobaGive boolean default false";
        command += ",add column if not exists newYearBagAmount int default 0";

        //バレンタインイベント用
        command += ",add column if not exists hasChocoGave boolean default false";

        return command;
    }
}
