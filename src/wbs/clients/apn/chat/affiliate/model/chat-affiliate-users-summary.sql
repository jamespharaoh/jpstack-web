---------------------------------------- VIEW chat_affiliate_users_summary

CREATE VIEW chat_affiliate_users_summary
AS SELECT
	chat_affiliate_id,

	sum (CASE WHEN type = 'u' AND gender = 'm' AND orient = 'g' THEN 1 ELSE 0 END) AS num_users_gay_male,
	sum (CASE WHEN type = 'u' AND gender = 'f' AND orient = 'g' THEN 1 ELSE 0 END) AS num_users_gay_female,
	sum (CASE WHEN type = 'u' AND gender = 'm' AND orient = 'b' THEN 1 ELSE 0 END) AS num_users_bi_male,
	sum (CASE WHEN type = 'u' AND gender = 'f' AND orient = 'b' THEN 1 ELSE 0 END) AS num_users_bi_female,
	sum (CASE WHEN type = 'u' AND gender = 'm' AND orient = 's' THEN 1 ELSE 0 END) AS num_users_straight_male,
	sum (CASE WHEN type = 'u' AND gender = 'f' AND orient = 's' THEN 1 ELSE 0 END) AS num_users_straight_female,
	sum (CASE WHEN type = 'm' AND gender = 'm' AND orient = 'g' THEN 1 ELSE 0 END) AS num_monitors_gay_male,
	sum (CASE WHEN type = 'm' AND gender = 'f' AND orient = 'g' THEN 1 ELSE 0 END) AS num_monitors_gay_female,
	sum (CASE WHEN type = 'm' AND gender = 'm' AND orient = 'b' THEN 1 ELSE 0 END) AS num_monitors_bi_male,
	sum (CASE WHEN type = 'm' AND gender = 'f' AND orient = 'b' THEN 1 ELSE 0 END) AS num_monitors_bi_female,
	sum (CASE WHEN type = 'm' AND gender = 'm' AND orient = 's' THEN 1 ELSE 0 END) AS num_monitors_straight_male,
	sum (CASE WHEN type = 'm' AND gender = 'f' AND orient = 's' THEN 1 ELSE 0 END) AS num_monitors_straight_female,

	sum (CASE WHEN first_join > now () - '1 day'::interval THEN 1 ELSE 0 END) AS num_joined_last_day,
	sum (CASE WHEN first_join > now () - '7 day'::interval THEN 1 ELSE 0 END) AS num_joined_last_week,
	sum (CASE WHEN first_join > now () - '30 day'::interval THEN 1 ELSE 0 END) AS num_joined_last_month,

	sum (CASE WHEN last_join > now () - '1 day'::interval THEN 1 ELSE 0 END) AS num_online_last_day,
	sum (CASE WHEN last_join > now () - '7 day'::interval THEN 1 ELSE 0 END) AS num_online_last_week,
	sum (CASE WHEN last_join > now () - '30 day'::interval THEN 1 ELSE 0 END) AS num_online_last_month

FROM chat_affiliate
	LEFT JOIN chat_user ON chat_affiliate.id = chat_user.chat_affiliate_id

GROUP BY chat_affiliate_id;
