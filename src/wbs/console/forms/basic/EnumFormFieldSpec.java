package wbs.console.forms.basic;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("enum-field")
@PrototypeComponent ("enumFormFieldSpec")
public
class EnumFormFieldSpec
	implements ConsoleSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean hidden;

	@DataAttribute (
		name = "helper")
	String helperBeanName;

	@DataAttribute (
		name = "implicit")
	String implicitValue;

}
