package wbs.console.forms.core;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormType;
import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("form")
@PrototypeComponent ("consoleFormSpec")
public
class ConsoleFormSpec
	implements ConsoleSpec {

	// tree attributes

	@DataParent
	ConsoleFormsSpec formContexts;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "object-type")
	String objectTypeName;

	@DataAttribute (
		name = "class")
	String className;

	@DataAttribute (
		name = "type",
		required = true)
	FormType formType;

	@DataAttribute (
		name = "fields-provider")
	String fieldsProviderName;

	// children

	@DataChildren (
		childrenElement = "columns")
	List <Object> columnFields;

	@DataChildren (
		childrenElement = "rows")
	List <Object> rowFields;

	@DataChildren (
		direct = true,
		childElement = "section")
	List <ConsoleFormSectionSpec> sections;

}
