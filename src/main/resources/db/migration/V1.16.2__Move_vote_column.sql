USE seichiassist;

CREATE TABLE IF NOT EXISTS vote(
 uuid CHAR(36) NOT NULL PRIMARY KEY,
 vote_number INT NOT NULL,
 chain_vote_number INT NOT NULL,
 effect_point INT NOT NULL,
 given_effect_point INT NOT NULL,
 last_vote DATETIME
);

INSERT INTO
 vote(
  uuid,
  vote_number,
  chain_vote_number,
  effect_point,
  given_effect_point,
  last_vote
 )
SELECT
 uuid,
 p_vote,
 chainvote,
 effectpoint,
 p_givenvote,
 CASE
    WHEN lastvote REGEXP '[0-9]{4}/[0-9]{2}/[0-9]{2}' THEN CONVERT(lastvote, DATE)
 END
FROM
 playerdata;

ALTER TABLE
 playerdata DROP IF EXISTS p_vote,
 DROP IF EXISTS chainvote,
 DROP IF EXISTS effectpoint,
 DROP IF EXISTS p_givenvote,
 DROP IF EXISTS lastvote;
