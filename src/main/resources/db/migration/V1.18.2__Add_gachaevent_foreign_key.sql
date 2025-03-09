USE seichiassist;

-- V1.18.1 で外部キー制約を追加しようとしていたが、gacha_events テーブルに存在しない event_id を持つレコードが gachadata テーブルに存在していたため、外部キー制約の追加に失敗していた。
-- しかし、マイグレーション自体は success になっていたので、このバージョンで外部キー制約を追加することで、不整合データを削除する。
DELETE FROM gachadata WHERE event_id NOT IN (SELECT id FROM gacha_events) AND event_id IS NOT NULL;

ALTER TABLE gachadata ADD FOREIGN KEY (event_id) REFERENCES gacha_events(id) ON DELETE CASCADE;
