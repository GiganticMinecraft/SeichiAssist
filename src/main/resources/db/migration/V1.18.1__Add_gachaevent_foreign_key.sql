USE seichiassist;

ALTER TABLE gachadata ADD FOREIGN KEY (event_id) REFERENCES gacha_events(id) ON DELETE CASCADE;
