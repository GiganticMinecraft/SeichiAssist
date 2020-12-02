use seichiassist;

create table booked_achievement_status_change
(
    id              int auto_increment unique primary key,
    player_uuid     varchar(36) not null,
    achievement_id  int not null,
    operation          varchar(10) not null,
    completed_at    datetime
);
