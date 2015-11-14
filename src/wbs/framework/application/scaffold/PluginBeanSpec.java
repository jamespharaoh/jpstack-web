package wbs.framework.application.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("bean")
public
class PluginBeanSpec {

	/*
	@DataAncestor
	@Getter @Setter
	ProjectSpec project;
	*/

	@DataAncestor
	@Getter @Setter
	PluginSpec plugin;

	@DataParent
	@Getter @Setter
	PluginLayerSpec layer;

	@DataAttribute (
		name = "class",
		required = true)
	@Getter @Setter
	String className;

}
