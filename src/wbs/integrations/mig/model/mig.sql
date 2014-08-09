------------------------------------------------------------- TABLE mig_network

CREATE TABLE mig_network (
	network_id int PRIMARY KEY REFERENCES network,
	suffix text NOT NULL,
	virtual bool NOT NULL
);

CREATE UNIQUE INDEX mig_network_suffix
ON mig_network (suffix)
WHERE NOT virtual;

CREATE FUNCTION mig_network_insert (text, text, bool) RETURNS void AS $$
	DECLARE

		the_network_code ALIAS FOR $1;
		new_suffix ALIAS FOR $2;
		new_virtual ALIAS FOR $3;

		the_network_id int;

	BEGIN

		SELECT INTO the_network_id networkid
		FROM network
		WHERE code = the_network_code;

		INSERT INTO mig_network (network_id, suffix, virtual)
		VALUES (the_network_id, new_suffix, new_virtual);

	END;
$$ LANGUAGE 'plpgsql';
