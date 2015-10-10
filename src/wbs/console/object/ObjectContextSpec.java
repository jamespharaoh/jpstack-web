package wbs.console.object;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

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
	ConsoleModuleSpec consoleModuleSpec;

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

	@DataAttribute ("default-file")
	String defaultFileName;

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
