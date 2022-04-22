use seichiassist;

-- pitchを保存するカラムを追加する
-- pitch = 0.0で真っ直ぐを向くことになる
ALTER TABLE sub_home ADD COLUMN pitch FLOAT DEFAULT 0.0;

-- yawを保存するカラムを追加する
-- yaw = -90.0で南を向くことになる
ALTER TABLE sub_home ADD COLUMN yaw FLOAT DEFAULT -90.0;