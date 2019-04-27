use seichiassist;

create table if not exists mine_stack(
    player_uuid varchar(128) unique not null,
    object_name varchar(128) unique not null,

    amount bigint unsigned not null default 0
);

create table if not exists sub_home(
    player_uuid   varchar(128) unique not null,
    server_id     int unsigned unique not null,
    id            int unsigned unique not null,

    name text default null,

    location_x           int not null,
    location_y           int not null,
    location_z           int not null,
    world_name   varchar(64) not null
) engine=InnoDB default charset=utf8;

create table if not exists grid_template(
    id            int unsigned unique not null,
    designer_uuid varchar(128) unique not null,

    ahead_length  int not null default 0,
    behind_length int not null default 0,
    right_length  int not null default 0,
    left_length   int not null default 0
);

# このテーブルにおいて、レコードの存在はスキルエフェクトの取得に意味論的に対応している
create table if not exists unlocked_active_skill_effect(
    player_uuid varchar(128) unique not null,
    effect_name varchar(64)  unique not null
);

# このテーブルにおいて、レコードの存在はスキルエフェクトの取得に意味論的に対応している
create table if not exists unlocked_active_skill_premium_effect(
    player_uuid varchar(128) unique not null,
    effect_name varchar(64)  unique not null
);
