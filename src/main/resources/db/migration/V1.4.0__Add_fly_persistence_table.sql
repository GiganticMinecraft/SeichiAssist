use seichiassist;

create table fly_status_cache
(
    player_uuid           varchar(36) not null,

    /**
     * 残Fly時間を記録するカラム。
     *  - 正の値はプレーヤーが飛行する残り時間
     *  - 0はプレーヤーが飛行していないこと
     *  - -1は時間無制限で飛行していたこと
     * をそれぞれ示す。
     *
     * 制約によりこのカラムは他の範囲の値は取らない。
     */
    remaining_fly_minutes int         not null check (remaining_fly_minutes >= -1),

    constraint primary key (player_uuid)
)
