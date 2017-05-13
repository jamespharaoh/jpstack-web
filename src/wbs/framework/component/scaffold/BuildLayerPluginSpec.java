package wbs.framework.component.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("plugin")
public
class BuildLayerPluginSpec {

	@DataParent
	@Getter @Setter
	BuildLayerSpec layer;

	@DataAttribute (
		required = true)
	@Getter @Setter
	String name;

}
