USE seichiassist;

CREATE TABLE vote_fairy(
 uuid CHAR(36) NOT NULL PRIMARY KEY,
 apple_open_state INT NOT NULL DEFAULT 1,
 fairy_summon_cost INT NOT NULL DEFAULT 1,
 is_fairy_using BOOLEAN NOT NULL DEFAULT false,
 fairy_recovery_mana_value INT NOT NULL DEFAULT 0,
 fairy_end_time DATETIME DEFAULT NULL,
 given_apple_amount BIGINT NOT NULL DEFAULT 0,
 is_play_fairy_speech_sound BOOLEAN NOT NULL DEFAULT true
);

INSERT INTO
 vote_fairy(
  uuid,
  apple_open_state,
  fairy_summon_cost,
  is_fairy_using,
  fairy_recovery_mana_value,
  fairy_end_time,
  given_apple_amount,
  is_play_fairy_speech_sound
 )
SELECT
 uuid,
 toggleGiveApple,
 toggleVotingFairy,
 canVotingFairyUse,
 VotingFairyRecoveryValue,
 newVotingFairyTime,
 p_apple,
 is_fairy_speech_play_sound
FROM
 playerdata;

ALTER TABLE playerdata
 DROP IF EXISTS toggleGiveApple,
 DROP IF EXISTS toggleVotingFairy,
 DROP IF EXISTS canVotingFairyUse,
 DROP IF EXISTS VotingFairyRecoveryValue,
 DROP IF EXISTS newVotingFairyTime,
 DROP IF EXISTS p_apple,
 DROP IF EXISTS is_fairy_speech_play_sound
