package wbs.console.forms;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name", callSuper = false)
@ToString (of = "name")
@DataClass
@PrototypeComponent ("dateFormFieldSpec")
public
class DateFormFieldSpec {

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
	protected
	String typeToString (
			LocalDate value) {

		if (value == null)
			return null;

		return ISODateTimeFormat
			.date ()
			.print (value);

	}

	@Override
	protected
	LocalDate stringToType (
			String stringValue)
		throws InvalidFormValueException {

		if (stringValue.isEmpty ())
			return null;

		try {

			return
				ISODateTimeFormat
					.date ()
					.parseLocalDate (stringValue);

		} catch (IllegalArgumentException exception) {

			requestContext.addError (sf (
				"Please enter a valid date for %s",
				name ()));

			throw new InvalidFormValueException ();

		}

	}
	*/

}
