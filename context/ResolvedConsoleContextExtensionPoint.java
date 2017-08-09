package wbs.console.context;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("resolved-extension-point")
@PrototypeComponent ("resolvedConsoleContextExtensionPoint")
public
class ResolvedConsoleContextExtensionPoint {

	// attributes

	@DataAttribute
	String name;

	@DataAttribute
	List <String> parentContextNames;

	// children

	@DataChildren
	List <String> contextTypeNames;

	@DataChildren
	List <String> contextLinkNames;

}
