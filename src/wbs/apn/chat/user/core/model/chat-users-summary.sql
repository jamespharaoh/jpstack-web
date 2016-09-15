---------------------------------------- VIEW chat_users_summary

CREATE VIEW chat_users_summary AS
SELECT

	chat.id AS chat_id,

	sum (CASE WHEN chat_user.type = 'u'
		AND chat_user.gender = 'm'
		AND chat_user.orient = 'g'
	THEN 1 ELSE 0 END) AS num_users_gay_male,

	sum (CASE WHEN chat_user.type = 'u'
		AND chat_user.gender = 'f'
		AND chat_user.orient = 'g'
	THEN 1 ELSE 0 END) AS num_users_gay_female,

	sum (CASE WHEN chat_user.type = 'u'
		AND chat_user.gender = 'm'
		AND chat_user.orient = 'b'
	THEN 1 ELSE 0 END) AS num_users_bi_male,

	sum (CASE WHEN chat_user.type = 'u'
		AND chat_user.gender = 'f'
		AND chat_user.orient = 'b'
	THEN 1 ELSE 0 END) AS num_users_bi_female,

	sum (CASE WHEN chat_user.type = 'u'
		AND chat_user.gender = 'm'
		AND chat_user.orient = 's'
	THEN 1 ELSE 0 END) AS num_users_straight_male,

	sum (CASE WHEN chat_user.type = 'u'
		AND chat_user.gender = 'f'
		AND chat_user.orient = 's'
	THEN 1 ELSE 0 END) AS num_users_straight_female,

	sum (CASE WHEN chat_user.type = 'm'
		AND chat_user.gender = 'm'
		AND chat_user.orient = 'g'
		THEN 1 ELSE 0 END) AS num_monitors_gay_male,

	sum (CASE WHEN chat_user.type = 'm'
		AND chat_user.gender = 'f'
		AND chat_user.orient = 'g'
	THEN 1 ELSE 0 END) AS num_monitors_gay_female,

	sum (CASE WHEN chat_user.type = 'm'
		AND chat_user.gender = 'm'
		AND chat_user.orient = 'b'
	THEN 1 ELSE 0 END) AS num_monitors_bi_male,

	sum (CASE WHEN chat_user.type = 'm'
		AND chat_user.gender = 'f'
		AND chat_user.orient = 'b'
	THEN 1 ELSE 0 END) AS num_monitors_bi_female,

	sum (CASE WHEN chat_user.type = 'm'
		AND chat_user.gender = 'm'
		AND chat_user.orient = 's'
	THEN 1 ELSE 0 END) AS num_monitors_straight_male,

	sum (CASE WHEN chat_user.type = 'm'
		AND chat_user.gender = 'f'
		AND chat_user.orient = 's'
	THEN 1 ELSE 0 END) AS num_monitors_straight_female,

	sum (CASE WHEN first_join > now () - '1 day'::interval
	THEN 1 ELSE 0 END) AS num_joined_last_day,

	sum (CASE WHEN first_join > now () - '7 day'::interval
	THEN 1 ELSE 0 END) AS num_joined_last_week,

	sum (CASE WHEN first_join > now () - '30 day'::interval
	THEN 1 ELSE 0 END) AS num_joined_last_month,

	sum (CASE WHEN last_join > now () - '1 day'::interval
	THEN 1 ELSE 0 END) AS num_online_last_day,

	sum (CASE WHEN last_join > now () - '7 day'::interval
	THEN 1 ELSE 0 END) AS num_online_last_week,

	sum (CASE WHEN last_join > now () - '30 day'::interval
	THEN 1 ELSE 0 END) AS num_online_last_month

FROM chat
LEFT JOIN chat_user
	ON chat.id = chat_user.chat_id

GROUP BY chat.id;
