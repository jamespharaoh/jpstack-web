package wbs.framework.application.scaffold;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("project")
public
class ProjectSpec
	implements Comparable<ProjectSpec> {

	@DataAttribute (
		required = true)
	@Getter @Setter
	String name;

	@DataAttribute (
		value = "package",
		required = true)
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

	@Override
	public
	int compareTo (
			ProjectSpec other) {

		return new CompareToBuilder ()

			.append (
				name (),
				other.name ())

			.toComparison ();

	}

}
