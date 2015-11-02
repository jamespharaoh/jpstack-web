package wbs.console.tab;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("tab-placement")
public
class ContextTabPlacement {

	@DataAttribute (
		name = "location")
	String tabLocation;

	@DataAttribute (
		name = "name")
	String tabName;

}
