use seichiassist;

-- UUID = char[36]
-- Player.name = vchar[16]

alter table donatedata alter column playername varchar(16);
alter table donatedata alter column uuid char(36);

alter table playerdata alter column name varchar(16);
alter table playerdata alter column uuid char(36);

alter table mine_stack alter column player_uuid char(36);

alter table sub_home alter column player_uuid char(36);

alter table grid_template alter column designer_uuid char(36);

alter table unlocked_active_skill_effect alter column player_uuid char(36);

alter table unlocked_active_skill_premium_effect alter column player_uuid char(36);
