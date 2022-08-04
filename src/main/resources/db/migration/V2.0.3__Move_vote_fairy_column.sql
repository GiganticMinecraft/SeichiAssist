USE seichiassist;

CREATE TABLE vote_fairy(
	uuid CHAR(36) PRIMARY KEY,
	apple_open_state INT DEFAULT 1,
	fairy_summon_cost INT DEFAULT 1,
	is_fairy_using BOOLEAN DEFAULT false,
	fairy_recovery_mana_value INT DEFAULT 0,
	fairy_end_time DATETIME DEFAULT NULL,
	given_apple_amount BIGINT DEFAULT 0,
	is_play_fairy_speech_sound BOOLEAN DEFAULT true
)
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

ALTER TABLE
	playerdata DROP toggleGiveApple,
	DROP toggleVotingFairy,
	DROP canVotingFairyUse,
	DROP VotingFairyRecoveryValue,
	DROP newVotingFairyTime,
	DROP p_apple,
	DROP is_fairy_speech_play_sound