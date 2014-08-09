
CREATE UNIQUE INDEX txt_nation_network_network
ON txt_nation_network (network_id)
WHERE NOT deleted;
