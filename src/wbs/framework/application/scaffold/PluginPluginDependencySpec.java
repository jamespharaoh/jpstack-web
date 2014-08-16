package wbs.framework.application.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("plugin")
public
class PluginPluginDependencySpec {

	@DataParent
	@Getter @Setter
	PluginProjectDependencySpec project;

	@DataAttribute (
		required = true)
	@Getter @Setter
	String name;

}
