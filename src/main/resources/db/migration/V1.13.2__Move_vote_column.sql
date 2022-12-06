USE seichiassist;

CREATE TABLE vote(
 uuid CHAR(36) PRIMARY KEY,
 vote_number INT,
 chain_vote_number INT,
 effect_point INT,
 given_effect_point INT,
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
 CONVERT(lastvote, DATE)
FROM
 playerdata;

ALTER TABLE
 playerdata DROP p_vote,
 DROP chainvote,
 DROP effectpoint,
 DROP p_givenvote,
 DROP lastvote;