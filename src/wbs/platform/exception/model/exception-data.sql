
SELECT object_type_insert (
	'exception_log_type',
	'exception log type',
	'root',
	3);

SELECT object_type_insert (
	'exception_log',
	'exception log',
	'root',
	4);

INSERT INTO exception_type (
	id,
	code,
	description)
VALUES (
	0,
	'unknown',
	'Unknown');

SELECT exception_type_insert (
	'daemon',
	'Daemon');

SELECT exception_type_insert (
	'console',
	'Console');

SELECT exception_type_insert (
	'webapi',
	'Web API');
