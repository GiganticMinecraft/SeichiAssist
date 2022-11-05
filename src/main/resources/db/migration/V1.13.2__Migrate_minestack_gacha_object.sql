USE seichiassist;

CREATE TABLE mine_stack_gacha_objects(
	id INT,
	mine_stack_object_name VARCHAR(20),
	FOREIGN KEY fk_id(id) REFERENCES gachadata(id)
);

INSERT INTO
	mine_stack_gacha_objects(id, mine_stack_object_name)
SELECT
	id,
	CONCAT('gachadata0_', id - 1)
FROM
	gachadata
WHERE
	event_name IS NULL;

DROP TABLE msgachadata;
