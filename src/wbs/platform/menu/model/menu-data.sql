SELECT priv_type_insert (
	'menu_group',
	'manage',
	'Full control',
	'Full control of menu group and all children',
	true);

SELECT priv_type_insert (
	'menu_group',
	'view',
	'View',
	'View and access all items in this menu group',
	true);

SELECT priv_type_insert (
	'menu_item',
	'manage',
	'Full control',
	'Full control of menu and all children',
	true);

SELECT priv_type_insert (
	'menu_item',
	'view',
	'View',
	'View and access this menu item',
	true);