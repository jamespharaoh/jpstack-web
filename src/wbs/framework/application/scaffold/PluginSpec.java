package wbs.framework.application.scaffold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataIgnore;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("plugin")
public
class PluginSpec
	implements Comparable<PluginSpec> {

	@DataParent
	@Getter @Setter
	ProjectSpec project;

	@DataAttribute (
		required = true)
	@Getter @Setter
	String name;

	@DataAttribute (
		value = "package",
		required = true)
	@Getter @Setter
	String packageName;

	@DataChild
	@Getter @Setter
	PluginDependenciesSpec dependencies =
		new PluginDependenciesSpec ();

	@DataChild
	@Getter @Setter
	PluginModelsSpec models =
		new PluginModelsSpec ();

	@DataChildren
	@Getter @Setter
	List<PluginFixtureSpec> fixtures =
		new ArrayList<PluginFixtureSpec> ();

	@DataIgnore
	Object sqlScripts;

	@DataChildren (
		direct = true,
		childElement = "layer")
	@Getter @Setter
	List<PluginLayerSpec> layers =
		new ArrayList<PluginLayerSpec> ();

	@DataChildrenIndex
	@Getter @Setter
	Map<String,PluginLayerSpec> layersByName =
		new HashMap<String,PluginLayerSpec> ();

	@DataChildren
	@Getter @Setter
	List<PluginConsoleModuleSpec> consoleModules =
		new ArrayList<PluginConsoleModuleSpec> ();

	@Override
	public
	int compareTo (
			PluginSpec other) {

		return new CompareToBuilder ()

			.append (
				project (),
				other.project ())

			.append (
				name (),
				other.name ())

			.toComparison ();

	}

}
