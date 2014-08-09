package wbs.framework.application.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("project")
public
class BuildProjectSpec {

	@DataAttribute (
		value = "name",
		required = true)
	@Getter @Setter
	String projectName;

	@DataAttribute (
		value = "package",
		required = true)
	@Getter @Setter
	String projectPackageName;

}
