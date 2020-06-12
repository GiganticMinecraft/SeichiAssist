use seichiassist;

-- len(UUID) = 36
alter table playerdata modify column player_uuid varchar(36) not null;
