package wbs.framework.application.scaffold;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("project")
public
class PluginProjectDependencySpec {

	@DataParent
	@Getter @Setter
	PluginDependenciesSpec dependencies;

	@DataAttribute
	@Getter @Setter
	String name;

	@DataChildren (
		direct = true)
	@Getter @Setter
	List<PluginPluginDependencySpec> plugins;

}
