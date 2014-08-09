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
@DataClass ("layer")
public
class PluginLayerSpec {

	@DataAttribute (required = true)
	@Getter @Setter
	String name;

	@DataChildren (direct = true)
	@Getter @Setter
	List<PluginBeanSpec> beans =
		new ArrayList<PluginBeanSpec> ();

}
