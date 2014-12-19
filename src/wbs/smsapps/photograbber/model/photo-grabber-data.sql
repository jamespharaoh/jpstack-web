SELECT priv_type_insert (
	'photo_grabber',
	'manage',
	'Photo grabber',
	'Full control of a photo grabber',
	true);

SELECT priv_type_insert (
	'photo_grabber',
	'messages',
	'View messages',
	'View and search message history for photo grabber',
	true);

SELECT priv_type_insert (
	'photo_grabber',
	'stats',
	'View stats',
	'View stats and figures',
	true);

SELECT priv_type_insert (
	'photo_grabber',
	'photo_grabber_history',
	'View request history',
	'View request history for photo grabber',
	true);

SELECT priv_type_insert (
	'photo_grabber',
	'photo_grabber_reissue',
	'Reissue photos for a photo grabber',
	'Resend photos to a user without billing',
	true);

SELECT priv_type_insert (
	'slice',
	'photo_grabber_create',
	'Create photo grabbers',
	'',
	true);

---------------------------------------- INSERT command_type

SELECT command_type_insert (
	'photo_grabber',
	'default',
	'Photo grabber');

---------------------------------------- INSERT service_type

SELECT service_type_insert (
	'photo_grabber',
	'default',
	'Photo grabber');

---------------------------------------- INSERT message_set_type

SELECT message_set_type_insert (
	'photo_grabber',
	'default',
	'Not found');

---------------------------------------- INSERT delivery_notice_type

SELECT delivery_type_insert (
	'photo_grabber',
	'Photo grabber');
