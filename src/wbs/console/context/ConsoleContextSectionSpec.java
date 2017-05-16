package wbs.console.context;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("section")
@PrototypeComponent ("consoleContextSectionSpec")
public
class ConsoleContextSectionSpec
	implements ConsoleContextContainerSpec {

	// tree attributes

	@DataParent
	ConsoleContextContainerSpec container;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String aliasOf;

	@DataAttribute
	String label;

	// children

	@DataChildren (
		direct = true)
	List<Object> children =
		new ArrayList<Object> ();

}
