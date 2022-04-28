use seichiassist;

-- sub_homeテーブルのlocationカラムの型を変更する
ALTER TABLE sub_home MODIFY location_x DOUBLE;
ALTER TABLE sub_home MODIFY location_y DOUBLE;
ALTER TABLE sub_home MODIFY location_z DOUBLE;