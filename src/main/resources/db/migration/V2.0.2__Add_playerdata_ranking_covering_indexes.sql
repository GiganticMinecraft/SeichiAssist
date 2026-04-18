USE seichiassist;

-- ランキング更新で使う以下の全件読み取りを、wide row 本体ではなく
-- より細い secondary index 側で処理できるようにするための covering index。
--
--   SELECT name, uuid, build_count FROM playerdata
--   SELECT name, uuid, totalbreaknum FROM playerdata
--
-- 現状のクエリは WHERE / ORDER BY を持たないため、optimizer が常にこの index を
-- 自動採用するとは限らないが、FORCE INDEX を含む後続のアプリ側調整と組み合わせて
-- playerdata 全件走査コストを下げる土台として追加する。

ALTER TABLE playerdata
    ADD INDEX IF NOT EXISTS `idx_playerdata_build_rank` (`build_count`, `uuid`, `name`),
    ADD INDEX IF NOT EXISTS `idx_playerdata_break_rank` (`totalbreaknum`, `uuid`, `name`);
