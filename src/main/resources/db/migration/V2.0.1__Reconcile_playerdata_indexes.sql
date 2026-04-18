USE seichiassist;

-- 既存 Flyway migration / baseline に含まれているが、
-- 現行クエリ実態では未使用な index を明示的に削除する。
--
-- 対象:
-- - playerdata.index_playerdata_on_lastquit
-- - playerdata.index_playerdata_playtick
-- - player_break_preference.index_player_break_preference_on_uuid

ALTER TABLE playerdata
    DROP INDEX IF EXISTS `index_playerdata_on_lastquit`,
    DROP INDEX IF EXISTS `index_playerdata_playtick`;

ALTER TABLE player_break_preference
    DROP INDEX IF EXISTS `index_player_break_preference_on_uuid`;
