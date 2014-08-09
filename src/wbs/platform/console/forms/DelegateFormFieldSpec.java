package wbs.platform.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("delegate-field")
@PrototypeComponent ("delegateFormFieldSpec")
@ConsoleModuleData
public
class DelegateFormFieldSpec {

	@DataAttribute
	String delegate;

	@DataAttribute
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	/*
	@Override
	public
	void doCell (
			PrintWriter out,
			Object object,
			boolean link) {

		Object delegate =
			BeanLogic.getProperty (
				object,
				delegate ());

		Object objectValue =
			BeanLogic.getProperty (
				delegate,
				name ());

		String stringValue = null;

		if (objectValue instanceof String)
			stringValue = (String) objectValue;

		if (objectValue instanceof TextRec)
			stringValue = ((TextRec) objectValue).getText ();

		if (stringValue == null)
			throw new ClassCastException (
				objectValue.getClass ().getName ());

		out.print (sf (
			"<td>%h</td>\n",
			stringValue));

	}
	*/

}
