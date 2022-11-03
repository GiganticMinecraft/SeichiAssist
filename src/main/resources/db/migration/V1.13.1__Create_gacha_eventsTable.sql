USE seichiassist;

CREATE TABLE gacha_events(
    id INT AUTO_INCREMENT,
    event_name VARCHAR(50),
    event_start_time DATETIME,
    event_end_time DATETIME,
    PRIMARY KEY(id, event_name)
);
