use seichiassist;

-- sub_homeテーブルのlocationカラムの型を変更する
ALTER TABLE sub_home alter column location_x DOUBLE
ALTER TABLE sub_home alter column location_y DOUBLE
ALTER TABLE sub_home alter column location_z DOUBLE