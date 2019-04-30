use seichiassist;

create table if not exists mine_stack(
    player_uuid varchar(128) not null,
    object_name varchar(128) not null,

    amount bigint unsigned not null default 0,
    
    constraint primary key (player_uuid, object_name)
);

create table if not exists sub_home(
    player_uuid   varchar(128) not null,
    server_id     int unsigned not null,
    id            int unsigned not null,

    name text default null,

    location_x           int not null,
    location_y           int not null,
    location_z           int not null,
    world_name   varchar(64) not null,

    constraint primary key (player_uuid, server_id, id)
) engine=InnoDB default charset=utf8;

create table if not exists grid_template(
    id            int unsigned not null,
    designer_uuid varchar(128) not null,

    ahead_length  int not null default 0,
    behind_length int not null default 0,
    right_length  int not null default 0,
    left_length   int not null default 0,

    constraint primary key (id, designer_uuid)
);

# このテーブルにおいて、レコードの存在はスキルエフェクトの取得に意味論的に対応している
create table if not exists unlocked_active_skill_effect(
    player_uuid varchar(128) not null,
    effect_name varchar(64)  not null,

    constraint primary key (player_uuid, effect_name)
);

# このテーブルにおいて、レコードの存在はスキルエフェクトの取得に意味論的に対応している
create table if not exists unlocked_active_skill_premium_effect(
    player_uuid varchar(128) not null,
    effect_name varchar(64)  not null,

    constraint primary key (player_uuid, effect_name)
);
