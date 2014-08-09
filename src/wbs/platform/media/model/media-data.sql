
SELECT object_type_insert (
	'media_type',
	'media type',
	'root',
	1);

SELECT object_type_insert (
	'media',
	'media',
	'root',
	3);

SELECT object_type_insert (
	'content',
	'content',
	'root',
	3);

-- INSERT media_type

SELECT media_type_insert ('text/plain', 'Plain text', 'txt');

SELECT media_type_insert ('image/jpeg', 'JPEG image', 'jpg');
SELECT media_type_insert ('image/gif', 'GIF image', 'gif');
SELECT media_type_insert ('image/png', 'PNG image', 'png');
SELECT media_type_insert ('image/mp4', 'MPEG-4 image', 'mp4');

SELECT media_type_insert ('video/3gpp', '3GPP video', '3gp');
SELECT media_type_insert ('video/mpeg', 'MPEG video', '3gp');
