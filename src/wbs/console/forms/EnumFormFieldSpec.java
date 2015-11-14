package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("enum-field")
@PrototypeComponent ("enumFormFieldSpec")
@ConsoleModuleData
public
class EnumFormFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute (
		name = "helper",
		required = true)
	String helperBeanName;

	/*
	@Override
	protected
	void doFormInputValue (
			PrintWriter out,
			Object object,
			Enum<?> value,
			String formValue) {


	}

	@Override
	public
	void doCell (
			PrintWriter out,
			Object object,
			boolean link) {

		Enum<?> value =
			getPropertyFromObject (object);

		out.write (sf (
			"%s\n",
			enumHelper.toTd (value)));

	}
	*/

}
