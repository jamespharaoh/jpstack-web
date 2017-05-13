package wbs.console.forms.context;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormType;
import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("multi-form-context")
@PrototypeComponent ("multiFormContextSpec")
public
class MultiFormContextSpec
	implements ConsoleModuleData {

	// tree attributes

	@DataParent
	FormContextsSpec formContexts;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "class",
		required = true)
	String className;

	@DataAttribute (
		name = "type",
		required = true)
	FormType formType;

	// children

	@DataChildren (
		direct = true,
		childElement = "fields")
	List <MultiFormContextFieldsSpec> fields;

}
