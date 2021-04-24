use seichiassist
-- "break" | "build" | "berserk" | "none"
ALTER TABLE playerdata ADD COLUMN IF NOT EXISTS extended_barstyle not null varchar(32);

UPDATE playerdata SET extended_expvisible = "break" WHERE expvisible = TRUE;
UPDATE playerdata SET extended_expvisible = "none" WHERE expvisible = FALSE;

ALTER TABLE playerdata DROP COLUMN IF EXISTS expvisible;