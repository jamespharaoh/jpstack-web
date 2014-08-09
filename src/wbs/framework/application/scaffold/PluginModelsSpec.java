package wbs.framework.application.scaffold;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("models")
public
class PluginModelsSpec {

	/*
	@DataAncestor
	@Getter @Setter
	ProjectSpec project;
	*/

	@DataParent
	@Getter @Setter
	PluginSpec plugin;

	@DataChildren (
		direct = true,
		childElement = "type")
	@Getter @Setter
	List<PluginCustomTypeSpec> types =
		new ArrayList<PluginCustomTypeSpec> ();

	@DataChildren (
		direct = true,
		childElement = "model")
	@Getter @Setter
	List<PluginModelSpec> models =
		new ArrayList<PluginModelSpec> ();

}
