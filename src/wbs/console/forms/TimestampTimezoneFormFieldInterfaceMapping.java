package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;

import wbs.console.misc.TimeFormatter;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("timestampTimezoneFormFieldInterfaceMapping")
public
class TimestampTimezoneFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,DateTime,String> {

	// dependencies

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	String name;

	// implementation

	@Override
	public
	DateTime interfaceToGeneric (
			Container container,
			String interfaceValue,
			List<String> errors) {

		if (interfaceValue.isEmpty ())
			return null;

		try {

			return timeFormatter.timestampTimezoneToDateTime (
				interfaceValue);

		} catch (IllegalArgumentException exception) {

			errors.add (
				stringFormat (
					"Please enter a valid timestamp with timezone for %s",
					name ()));

			return null;

		}

	}

	@Override
	public
	String genericToInterface (
			Container container,
			DateTime genericValue) {

		if (genericValue == null)
			return null;

		return timeFormatter.dateTimeToTimestampTimezoneString (
			genericValue);

	}

}
