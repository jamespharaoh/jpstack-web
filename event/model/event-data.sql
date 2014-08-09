
SELECT object_type_insert (
	'event_type',
	'event type',
	'root',
	1);

SELECT object_type_insert (
	'event',
	'event',
	'root',
	3);

SELECT object_type_insert (
	'event_link',
	'event link',
	'event',
	2);

SELECT event_type_insert (
	'object_created',
	'%0 created %1 in %2');

SELECT event_type_insert (
	'object_created_in',
	'%0 created %1 of type %2 in %3');

SELECT event_type_insert (
	'object_removed_in',
	'%0 removed %1 of type %2 in %3');

SELECT event_type_insert (
	'object_field_updated',
	'%0 set %1 for %2 to %3');

SELECT event_type_insert (
	'object_field_updated_in',
	'%0 set %1 for %2 of type %3 in %4 to %5');

SELECT event_type_insert (
	'object_field_nulled',
	'%0 unset %1 for %2');

SELECT event_type_insert (
	'object_field_nulled_in',
	'%0 unset %1 for %2 of type %3 in %4');

SELECT event_type_insert (
	'object_code_changed',
	'%0 changed code for %1 from %2 to %3');

SELECT event_type_insert (
	'object_code_changed_in',
	'%0 changed code for %1 of type %2 in %3 from %4 to %5');

SELECT event_type_insert (
	'object_name_changed',
	'%0 changed name for %1 from %2 to %3');

SELECT event_type_insert (
	'object_name_changed_in',
	'%0 changed name for %1 of type %2 in %3 from %4 to %5');

SELECT event_type_insert (
	'user_priv_granted',
	'%0 granted %1 to %2');

SELECT event_type_insert (
	'user_priv_revoked',
	'%0 revoked %1 from %2');

SELECT event_type_insert (
	'user_priv_grant_granted',
	'%0 granted grant %1 to %2');

SELECT event_type_insert (
	'user_priv_grant_revoked',
	'%0 revoked grant %1 from %2');

SELECT event_type_insert (
	'user_password_reset',
	'%0 reset password for %1');

SELECT event_type_insert (
	'group_user_added',
	'%0 added %1 to %2');

SELECT event_type_insert (
	'group_user_removed',
	'%0 removed %1 from %2');

SELECT event_type_insert (
	'group_added',
	'%0 added %1 to %2');

SELECT event_type_insert (
	'group_grant',
	'%0 granted %1 to group %2');

SELECT event_type_insert (
	'group_removed',
	'%0 removed user %1 from %2');

SELECT event_type_insert (
	'group_revoke',
	'%0 revoked %1 from %2');

SELECT event_type_insert (
	'user_grant',
	'%0 granted %1 to %2');

SELECT event_type_insert (
	'user_grant_grant',
	'%0 granted grant %1 to %2');

SELECT event_type_insert (
	'user_revoke',
	'%0 revoked %1 from %2');

SELECT event_type_insert (
	'user_revoke_grant',
	'%0 revoked grant %1 from %2');

SELECT event_type_insert (
	'user_password_reset',
	'%0 reset password for %1');
