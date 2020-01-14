use seichiassist;

-- UUID = char[36]
-- Player.name = vchar[16]

alter table donatedata modify playername varchar(16);
alter table donatedata modify playeruuid char(36);

alter table playerdata modify name varchar(16);
alter table playerdata modify uuid char(36);

alter table mine_stack modify player_uuid char(36);

alter table sub_home modify player_uuid char(36);

alter table grid_template modify designer_uuid char(36);

alter table unlocked_active_skill_effect modify player_uuid char(36);

alter table unlocked_active_skill_premium_effect modify player_uuid char(36);

alter table playerdata modify gachapoint bigint default 0;
