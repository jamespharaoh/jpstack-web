package wbs.framework.application.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("api-module")
public
class PluginApiModuleSpec {
public

	@DataParent
	@Getter @Setter
	PluginSpec plugin;

	@DataAttribute
	@Getter @Setter
	String name;

}
