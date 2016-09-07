package wbs.framework.component.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("plugin")
public
class BuildPluginSpec {

	@DataAttribute (
		required = true)
	@Getter @Setter
	String name;

	@DataAttribute (
		name = "package",
		required = true)
	@Getter @Setter
	String packageName;

}
