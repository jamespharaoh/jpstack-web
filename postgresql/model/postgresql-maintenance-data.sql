DELETE FROM postgresql_maintenance
WHERE frequency = 'm';

SELECT postgresql_maintenance_insert ('m', 0, 'VACUUM VERBOSE');
SELECT $$
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE message');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE chat');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE chat_affiliate');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE chat_contact');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE chat_monitor_inbox');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE chat_user');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE command');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE "user"');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE forwarder_message_in');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE inbox');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE postgresql_maintenance');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE message_stats');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE message_stats_queue');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE outbox');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE pg_statistic');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE priv');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE queue');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE queue_item');
SELECT postgresql_maintenance_insert ('m', 2, 'REINDEX TABLE service');
$$;

DELETE FROM postgresql_maintenance
WHERE frequency = 'w';

SELECT postgresql_maintenance_insert ('w', 0, 'VACUUM VERBOSE');
SELECT $$
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE chat');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE chat_affiliate');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE chat_monitor_inbox');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE chat_user');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE chat_user_contact');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE command');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE "user"');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE forwarder_message_in');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE inbox');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE postgresql_maintenance');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE message_stats');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE message_stats_queue');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE outbox');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE pg_statistic');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE priv');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE queue');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE queue_item');
SELECT postgresql_maintenance_insert ('w', 1, 'REINDEX TABLE service');
$$;

DELETE FROM postgresql_maintenance
WHERE frequency = 'd';

SELECT postgresql_maintenance_insert ('d', 0, 'VACUUM VERBOSE');
SELECT $$
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE chat');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE chat_affiliate');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE chat_monitor_inbox');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE chat_user');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE chat_user_contact');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE command');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE "user"');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE forwarder_message_in');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE inbox');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE postgresql_maintenance');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE message_stats');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE message_stats_queue');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE outbox');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE pg_statistic');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE priv');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE queue');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE queue_item');
SELECT postgresql_maintenance_insert ('d', 1, 'REINDEX TABLE service');
$$;

DELETE FROM postgresql_maintenance
WHERE frequency = 'h';

SELECT postgresql_maintenance_insert ('h', 0, 'ANALYZE VERBOSE');

DELETE FROM postgresql_maintenance
WHERE frequency = '5';

DELETE FROM postgresql_maintenance
WHERE frequency = '1';

SELECT postgresql_maintenance_insert ('1', 0, 'SELECT message_stats_queue_process ()');
SELECT $$
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE chat');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE chat_affiliate');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE chat_monitor_inbox');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE chat_user');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE command');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE "user"');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE forwarder_message_in');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE inbox');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE postgresql_maintenance');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE message_stats');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE message_stats_queue');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE outbox');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE pg_statistic');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE priv');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE queue');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE queue_item');
SELECT postgresql_maintenance_insert ('1', 1, 'VACUUM VERBOSE service');
$$;
