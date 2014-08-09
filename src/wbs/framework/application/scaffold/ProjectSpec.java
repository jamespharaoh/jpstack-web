package wbs.framework.application.scaffold;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("project")
public
class ProjectSpec {

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute ("package")
	@Getter @Setter
	String packageName;

	@DataChildren
	@Getter @Setter
	List<BuildProjectSpec> dependsProjects =
		new ArrayList<BuildProjectSpec> ();

	@DataChildren (
		direct = true,
		childElement = "plugin")
	@Getter @Setter
	List<ProjectPluginSpec> projectPlugins =
		new ArrayList<ProjectPluginSpec> ();

	@Getter @Setter
	List<PluginSpec> plugins;

}
