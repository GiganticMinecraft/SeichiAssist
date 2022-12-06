USE seichiassist;

CREATE TABLE vote(
 uuid CHAR(36) NOT NULL PRIMARY KEY,
 vote_number INT NOT NULL ,
 chain_vote_number INT NOT NULL ,
 effect_point INT NOT NULL ,
 given_effect_point INT NOT NULL ,
 last_vote DATETIME NOT NULL
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