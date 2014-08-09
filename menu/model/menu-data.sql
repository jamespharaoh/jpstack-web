
-- menu group

SELECT object_type_insert (
	'menu_group',
	'menu group',
	'root',
	1);

SELECT priv_type_insert (
	'menu_group',
	'manage',
	'Full control',
	'Full control of menu group and all children',
	true);

-- menu

SELECT object_type_insert (
	'menu',
	'menu',
	'menu_group',
	1);

SELECT priv_type_insert (
	'menu',
	'manage',
	'Full control',
	'Full control of menu and all children',
	true);
