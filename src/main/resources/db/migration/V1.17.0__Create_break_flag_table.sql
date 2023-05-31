use SeichiAssist;

CREATE TABLE break_flags(
    uuid CHAR(36) NOT NULL PRIMARY KEY,
    flag_name VARCHAR(20) NOT NULL,
    can_break BOOL NOT NULL DEFAULT TRUE
);
