USE seichiassist;

-- 型を変えるので一度内容をリセットする
UPDATE playerdata SET newVotingFairyTime = NULL;
ALTER TABLE playerdata MODIFY newVotingFairyTime DATETIME;
