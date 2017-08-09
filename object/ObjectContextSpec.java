package wbs.console.object;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextSpec;
import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
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
public
class ObjectContextSpec
	implements ConsoleContextSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleModuleSpec;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String componentName;

	@DataAttribute (
		required = true)
	String objectName;

	@DataAttribute
	String objectTitle;

	@DataAttribute (
		name = "cryptor")
	String cryptorBeanName;

	@DataAttribute (
		name = "default-file")
	String defaultFileName;

	// children

	@DataChildren (
		childrenElement = "list")
	List <Object> listChildren =
		emptyList ();

	@DataChildren (
		childrenElement = "object")
	List <Object> objectChildren =
		emptyList ();

}
