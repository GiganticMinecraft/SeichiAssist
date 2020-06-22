use seichiassist;

alter table playerdata
    drop column if exists effect_blizzard,
    drop column if exists effect_explosion,
    drop column if exists effect_meteo,
    drop column if exists activenum,
    drop column if exists condenskill;
