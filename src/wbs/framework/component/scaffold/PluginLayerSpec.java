package wbs.framework.component.scaffold;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("layer")
public
class PluginLayerSpec {

	@DataAttribute (
		required = true)
	@Getter @Setter
	String name;

	@DataChildren (
		direct = true,
		childElement = "bootstrap-component")
	@Getter @Setter
	List <PluginBootstrapComponentSpec> bootstrapComponents =
		new ArrayList<> ();

	@DataChildren (
		direct = true,
		childElement = "component")
	@Getter @Setter
	List <PluginComponentSpec> components =
		new ArrayList<> ();

}
