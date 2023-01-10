USE seichiassist;

ALTER TABLE playerdata ADD is_fairy_speech_play_sound BOOLEAN DEFAULT TRUE AFTER toggleVotingFairy;
