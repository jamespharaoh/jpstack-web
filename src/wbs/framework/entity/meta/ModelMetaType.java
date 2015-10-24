package wbs.framework.entity.meta;

public
enum ModelMetaType {

	common,
	component,
	ephemeral,
	event,
	major,
	minor,
	root,
	type;

	public
	boolean component () {
		return this == component;
	}

	public
	boolean record () {
		return this != component;
	}

}
