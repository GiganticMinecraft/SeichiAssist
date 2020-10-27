use seichiassist;

create table booked_achievement
(
    id             int auto_increment unique primary key,
    player_uuid    varchar(36) not null,
    achievement_id  int not null,
    is_received    boolean not null
);
