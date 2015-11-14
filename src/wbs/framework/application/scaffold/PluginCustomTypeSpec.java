package wbs.framework.application.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("custom-type")
public
class PluginCustomTypeSpec {

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
	PluginModelsSpec models;

	@DataAttribute
	@Getter @Setter
	String name;

}
