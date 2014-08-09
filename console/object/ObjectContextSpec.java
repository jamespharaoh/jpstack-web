package wbs.platform.console.object;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.spec.ConsoleModuleData;
import wbs.platform.console.spec.ConsoleSpec;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("object-context")
@PrototypeComponent ("objectContextSpec")
@ConsoleModuleData
public
class ObjectContextSpec {

	// attributes

	@DataAncestor
	ConsoleSpec consoleModuleSpec;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String beanName;

	@DataAttribute (
		required = true)
	String objectName;

	@DataAttribute
	String objectTitle;

	@DataAttribute ("cryptor")
	String cryptorBeanName;

	// children

	@DataChildren (
		childrenElement = "list")
	List<Object> listChildren =
		new ArrayList<Object> ();

	@DataChildren (
		childrenElement = "object")
	List<Object> objectChildren =
		new ArrayList<Object> ();

}
