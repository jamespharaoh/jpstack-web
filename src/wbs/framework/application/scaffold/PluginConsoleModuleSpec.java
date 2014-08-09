package wbs.framework.application.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("console-module")
public
class PluginConsoleModuleSpec {

	/*
	@DataAncestor
	@Getter @Setter
	ProjectSpec project;
	*/

	@DataParent
	@Getter @Setter
	PluginSpec plugin;

	@DataAttribute
	@Getter @Setter
	String name;

}
