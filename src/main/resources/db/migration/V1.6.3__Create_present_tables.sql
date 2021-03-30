use seichiassist;
-- プレゼントIDと実体の対応付け
CREATE TABLE IF NOT EXISTS present(
  present_id int PRIMARY KEY auto_increment,
  itemstack  blob NOT NULL
);

-- プレイヤーがプレゼントを受け取ったかどうかをモデリングする
CREATE TABLE IF NOT EXISTS present_state(
  present_id int      NOT NULL,
  uuid       char(36) NOT NULL,
  claimed    boolean  NOT NULL,

  PRIMARY KEY(present_id, uuid),
  FOREIGN KEY present_id_in_present_state_must_exist_in_presents_table(present_id) REFERENCES present(present_id)
);
