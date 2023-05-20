USE seichiassist;

CREATE TABLE IF NOT EXISTS mine_stack_gacha_objects(
	id INT,
	mine_stack_object_name VARCHAR(20),
	FOREIGN KEY fk_id(id) REFERENCES gachadata(id)
);

INSERT INTO
	mine_stack_gacha_objects(id, mine_stack_object_name)
SELECT
	id,
	CONCAT('gachadata0_', id - 2)
FROM
	gachadata
WHERE
	event_id IS NULL;

DROP TABLE IF EXISTS msgachadata;
