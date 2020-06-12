use seichiassist;

create table if not exists
    unlocked_seichi_skill(
        player_uuid varchar(128) not null,
        skill_name varchar(64)  not null,
        constraint primary key (player_uuid, skill_name)
    );

alter table donatedata
    drop column effectnum;

alter table playerdata
    drop column activemineflagnum,
    drop column activeskilltype,
    drop column activeskillnum,
    drop column assaultskilltype,
    drop column assaultskillnum,
    drop column assaultflag,
    drop column arrowskill,
    drop column multiskill,
    drop column breakskill,
    drop column fluidcondenskill,
    drop column watercondenskill,
    drop column lavacondenskill,
    drop column effectnum,

    # 0がスキルオフに対応する
    add column if not exists serialized_usage_mode int not null default 0,

    # 本来foreign key制約をそれぞれunlocked_active_skill_effectとunlocked_seichi_skillまで掛けられるのだが、
    # 場合によっては保存処理中に一時的に制約が破られる可能性が排除できなかったため制約を掛けていない。
    # 必要ならば今後のマイグレーションで制約を掛けること。
    add column if not exists selected_effect varchar(64) null default null,
    add column if not exists selected_active_skill varchar(64) null default null,
    add column if not exists selected_assault_skill varchar(64) null default null;
