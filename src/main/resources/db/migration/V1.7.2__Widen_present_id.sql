use seichiassist;

-- このマイグレーションが走る時点ではpresentシステムが正常に動作しておらず、
-- データが入っていないため問題なし
-- foreign key制約によってalter table出来なかったため作り直している
DROP TABLE present_state;
DROP TABLE present;

-- プレゼントIDと実体の対応付け
CREATE TABLE IF NOT EXISTS present(
  present_id bigint PRIMARY KEY auto_increment,
  itemstack  blob NOT NULL
);

-- プレイヤーがプレゼントを受け取ったかどうかをモデリングする
CREATE TABLE IF NOT EXISTS present_state(
  present_id bigint   NOT NULL,
  uuid       char(36) NOT NULL,
  claimed    boolean  NOT NULL,

  PRIMARY KEY(present_id, uuid),
  FOREIGN KEY present_id_in_present_state_must_exist_in_presents_table(present_id) REFERENCES present(present_id)
);
