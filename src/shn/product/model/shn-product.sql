CREATE TABLE shn_product_variant_has_variant_value (

	product_variant_id int NOT NULL REFERENCES shn_product_variant,
	variant_value_id int NOT NULL REFERENCES shn_product_variant_value,

	PRIMARY KEY (
		product_variant_id,
		variant_value_id
	)

);
