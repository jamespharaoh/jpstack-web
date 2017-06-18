CREATE OR REPLACE VIEW queue_item_view AS

	SELECT

		queue_item.id AS queue_item_id,
		queue.id AS queue_id,
		queue_item.created_time AS timestamp,
		processed_by_user.id AS processed_by_user_id,

		1 AS num_created,
		0 AS num_processed,
		0 AS num_preferred,
		0 AS num_not_preferred

	FROM queue_item

	INNER JOIN queue_subject
		ON queue_item.queue_subject_id
			= queue_subject.id

	INNER JOIN queue
		ON queue_subject.queue_id
			= queue.id

	LEFT JOIN "user" AS processed_by_user
		ON queue_item.processed_user_id
			= processed_by_user.id

UNION

	SELECT

		queue_item.id AS queue_item_id,
		queue.id AS queue_id,
		queue_item.processed_time AS timestamp,
		processed_by_user.id AS processed_by_user_id,

		0 AS num_created,
		1 AS num_processed,

		CASE
			WHEN queue_item.processed_by_preferred_user THEN 1
			ELSE 0
		END AS num_preferred,

		CASE
			WHEN queue_item.processed_by_preferred_user THEN 0
			ELSE 1
		END AS num_not_preferred

	FROM queue_item

	INNER JOIN queue_subject
		ON queue_item.queue_subject_id
			= queue_subject.id

	INNER JOIN queue
		ON queue_subject.queue_id
			= queue.id

	INNER JOIN "user" AS processed_by_user
		ON queue_item.processed_user_id
			= processed_by_user.id

;