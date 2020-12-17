use seichiassist;
-- プレゼントIDと実体の対応付け
CREATE TABLE presents(
  present_id int auto_increment PRIMARY KEY,
  itemstack  blob
);

-- プレイヤーがプレゼントを受け取ったかどうかをモデリングする
CREATE TABLE present_state(
  uuid       char(36) NOT NULL,
  present_id int      NOT NULL,
  claimed   boolean  NOT NULL DEFAULT FALSE,

  PRIMARY KEY(uuid, present_id),
  FOREIGN KEY present_id_constraints(present_id) REFERENCES presents(present_id)
);
