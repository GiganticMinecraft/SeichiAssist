-- 本来重複しないはずのuuidにunique制約をかけていなかった &
-- それによって何故か全く同じデータをもつデータがplayerdataテーブルに存在しているのでそれを取り除く

USE seichiassist;

CREATE TEMPORARY TABLE playerdata_tmp AS SELECT DISTINCT * FROM playerdata;
TRUNCATE TABLE playerdata;

INSERT INTO playerdata SELECT * FROM playerdata_tmp;
DROP TABLE playerdata_tmp;

-- ゆるせないのでplayerdataテーブルのuuidカラムをprimary keyとして設定する

ALTER TABLE playerdata ADD PRIMARY KEY(uuid)
