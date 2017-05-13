package wbs.console.forms.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("fields")
@PrototypeComponent ("multiFormContextFieldsSpec")
public
class MultiFormContextFieldsSpec
	implements ConsoleModuleData {

	// tree

	@DataParent
	MultiFormContextSpec multiFormContext;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "fields",
		required = true)
	String formFieldsName;

}
