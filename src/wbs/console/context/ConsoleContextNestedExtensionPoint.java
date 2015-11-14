package wbs.console.context;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("nested-extension-point")
@PrototypeComponent ("consoleContextNestedExtensionPoint")
public
class ConsoleContextNestedExtensionPoint
	implements ConsoleContextExtensionPoint {

	// attributes

	@DataAttribute
	String name;

	@DataAttribute (
		name = "parent-extension-point")
	String parentExtensionPointName;

	// details

	@Getter
	boolean root = false;

	@Getter
	boolean nested = true;

	// unsupported

	@Override
	public
	List<String> contextTypeNames () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	List<String> contextLinkNames () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	List<String> parentContextNames () {
		throw new UnsupportedOperationException ();
	}

}
