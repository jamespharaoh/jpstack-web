-- queue_type

SELECT object_type_insert (
	'queue_type',
	'queue type',
	'object_type',
	2);

-- queue

SELECT object_type_insert (
	'queue',
	'queue',
	NULL,
	2);

SELECT priv_type_insert (
	'queue',
	'reply',
	'Reply',
	'Reply to items in this queue',
	true);

-- queue subject

SELECT object_type_insert (
	'queue_subject',
	'queue subject',
	'queue',
	3);

-- queue item

SELECT object_type_insert (
	'queue_item',
	'queue item',
	'queue_subject',
	3);

-- queue item claim

SELECT object_type_insert (
	'queue_item_claim',
	'queue item claim',
	'queue_item',
	3);
