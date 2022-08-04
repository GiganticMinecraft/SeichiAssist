USE seichiassist;

CREATE TABLE vote_fairy(
	uuid CHAR(36) PRIMARY KEY,
	apple_open_state INT,
	is_fairy_using BOOLEAN,
	fairy_recovery_mana_value INT,
	fairy_end_time DATETIME,
	given_apple_amount BIGINT,
	is_play_fairy_speech_sound BOOLEAN
)
INSERT INTO
	vote_fairy(
		uuid,
		apple_open_state,
		is_fairy_using,
		fairy_recovery_mana_value,
		fairy_end_time,
		given_apple_amount,
		is_play_fairy_speech_sound
	)
SELECT
	uuid,
	toggleGiveApple,
	canVotingFairyUse,
	VotingFairyRecoveryValue,
	newVotingFairyTime,
	p_apple,
	is_fairy_speech_play_sound
FROM
	playerdata;

ALTER TABLE
	playerdata DROP toggleGiveApple,
	DROP canVotingFairyUse,
	DROP VotingFairyRecoveryValue,
	DROP newVotingFairyTime,
	DROP p_apple,
	DROP is_fairy_speech_play_sound