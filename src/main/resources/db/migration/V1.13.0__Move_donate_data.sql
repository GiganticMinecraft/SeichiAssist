USE seichiassist;

-- donatedataテーブルの中に2016年に挿入されたplayeruuidがNULLという意味を持たないデータが存在しており、
-- donate_usage_historyのuuidに対してかける制約(NOT NULL)が成り立たなくなるのでunknown_player_donationへ移動する

CREATE TABLE IF NOT EXISTS unknown_player_donation(
    id INT AUTO_INCREMENT NOT NULL,
    uuid CHAR(36),
    effect_name VARCHAR(20) NOT NULL,
    use_points INT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY(id)
);

INSERT INTO unknown_player_donation(
    uuid,
    effect_name,
    use_points,
    timestamp
)
SELECT
    playeruuid,
    effectname,
    usepoint,
    date
FROM
    donatedata
WHERE
    usepoint > 0 AND playeruuid IS NULL;

DELETE FROM donatedata WHERE playeruuid IS NULL;

-- それ以外のデータを移動する

CREATE TABLE IF NOT EXISTS donate_usage_history(
    id INT AUTO_INCREMENT NOT NULL,
    uuid CHAR(36) NOT NULL,
    effect_name VARCHAR(20) NOT NULL,
    use_points INT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY(id, uuid)
);


INSERT INTO donate_usage_history(
    uuid,
    effect_name,
    use_points,
    timestamp
)
SELECT
    playeruuid,
    effectname,
    usepoint,
    date
FROM
    donatedata
WHERE
    usepoint > 0;

CREATE TABLE IF NOT EXISTS donate_purchase_history(
    id INT AUTO_INCREMENT NOT NULL,
    uuid CHAR(36) NOT NULL,
    get_points INT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY(id, uuid)
);

INSERT INTO donate_purchase_history(
    uuid,
    get_points,
    timestamp
)
SELECT
    playeruuid,
    getpoint,
    date
FROM
    donatedata
WHERE
    getpoint > 0;
