USE seichiassist;

CREATE TABLE IF NOT EXISTS tutorial(
    uuid char(36) PRIMARY KEY,
    taken boolean NOT NULL default FALSE,

    FOREIGN KEY tutorial_uuid_must_exist_in_playerdata(uuid) REFERENCES playerdata(uuid)
)
