use seichiassist;

# データベース上インベントリ等のアイテム変換の記録
create table item_migration_on_database
(
    version_string varchar(64) not null,

    constraint primary key (version_string)
);

# ワールドデータ上のアイテム変換の記録
create table item_migration_in_server_world_levels
(
    server_id      varchar(20) not null,
    version_string varchar(64) not null,

    constraint primary key (server_id, version_string)
);

# プレーヤーのサーバー内インベントリのアイテム変換の記録
create table player_in_server_item_migration
(
    player_uuid    varchar(36) not null,
    server_id      varchar(20) not null,
    version_string varchar(64) not null,

    constraint primary key (player_uuid, server_id, version_string)
);
