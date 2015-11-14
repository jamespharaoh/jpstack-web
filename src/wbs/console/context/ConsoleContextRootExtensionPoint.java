package wbs.console.context;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("root-extension-point")
@PrototypeComponent ("consoleContextRootExtensionPoint")
public
class ConsoleContextRootExtensionPoint
	implements ConsoleContextExtensionPoint {

	// attributes

	@DataAttribute
	String name;

	// children

	@DataChildren
	List<String> contextTypeNames;

	@DataChildren
	List<String> contextLinkNames;

	@DataChildren
	List<String> parentContextNames;

	// details

	@Getter
	boolean root = true;

	@Getter
	boolean nested = false;

	// unsupported

	@Override
	public
	String parentExtensionPointName () {
		throw new UnsupportedOperationException ();
	}

}
