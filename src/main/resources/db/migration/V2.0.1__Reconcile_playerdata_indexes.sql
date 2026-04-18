USE seichiassist;

-- playerdata の index 構成が歴史的に drift しており、
-- live DB には migration 定義に無い重複 index / 未使用 index が残っている。
--
-- 現行クエリ実態では playerdata で index 利用されているのは name lookup と PRIMARY(uuid) だけで、
-- totalbreaknum/build_count/playtick/lastquit の単列 index はランキング全件走査には効いていない。
-- そのため、まずは重複・未使用 index を落として最小構成へ正規化する。
-- ランキング向けに試す covering index は後続 migration で追加する。
--
-- - PRIMARY KEY (uuid)
-- - KEY name_index (name)
--
-- 補足:
--   name は live data 上で重複が存在するため UNIQUE には戻さない。

ALTER TABLE playerdata
    DROP INDEX IF EXISTS `name`,
    DROP INDEX IF EXISTS `uuid`,
    DROP INDEX IF EXISTS `uuid_index`,
    DROP INDEX IF EXISTS `ranking_index`,
    DROP INDEX IF EXISTS `index_playerdata_on_lastquit`,
    DROP INDEX IF EXISTS `index_playerdata_playtick`,
    DROP INDEX IF EXISTS `pd_uid_idx`,
    DROP INDEX IF EXISTS `pd_name_idx`,
    DROP INDEX IF EXISTS `pd_lc_idx`,
    DROP INDEX IF EXISTS `pd_name_pv_idx`,
    DROP INDEX IF EXISTS `pd_bc_idx`,
    DROP INDEX IF EXISTS `name_index`,
    ADD INDEX IF NOT EXISTS `name_index` (`name`);
