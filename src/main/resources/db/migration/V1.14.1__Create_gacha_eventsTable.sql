USE seichiassist;

CREATE TABLE IF NOT EXISTS gacha_events(
    id INT AUTO_INCREMENT,
    event_name VARCHAR(30),
    event_start_time DATETIME,
    event_end_time DATETIME,
    PRIMARY KEY(id, event_name)
);
