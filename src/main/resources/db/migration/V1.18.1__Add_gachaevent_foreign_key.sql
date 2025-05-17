USE seichiassist;

DELETE FROM gachadata WHERE event_id NOT IN (SELECT id FROM gacha_events) AND event_id IS NOT NULL;

ALTER TABLE gachadata ADD FOREIGN KEY (event_id) REFERENCES gacha_events(id) ON DELETE CASCADE;
