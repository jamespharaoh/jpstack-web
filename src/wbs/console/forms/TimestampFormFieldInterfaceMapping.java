package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.misc.TimeFormatter;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("timestampFormFieldInterfaceMapping")
public
class TimestampFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Instant,String> {

	// dependencies

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	TimestampFormFieldSpec.Format format;

	// implementation

	@Override
	public
	Instant interfaceToGeneric (
			Container container,
			String interfaceValue,
			List<String> errors) {

		if (format != TimestampFormFieldSpec.Format.timestamp) {
			throw new RuntimeException ();
		}

		if (interfaceValue == null)
			return null;

		if (interfaceValue.isEmpty ())
			return null;

		try {

			return timeFormatter.timestampStringToInstant (
				timeFormatter.defaultTimezone (),
				interfaceValue);

		} catch (IllegalArgumentException exception) {

			errors.add (
				stringFormat (
					"Please enter a valid timestamp for %s",
					name ()));

			return null;

		}

	}

	@Override
	public
	String genericToInterface (
			Container container,
			Instant genericValue) {

		if (genericValue == null)
			return null;

		switch (format) {

		case timestamp:

			return timeFormatter.instantToTimestampString (
				timeFormatter.defaultTimezone (),
				genericValue);

		case date:

			return timeFormatter.instantToDateStringShort (
				timeFormatter.defaultTimezone (),
				genericValue);

		case time:

			return timeFormatter.instantToTimeString (
				timeFormatter.defaultTimezone (),
				genericValue);

		default:

			throw new RuntimeException ();

		}

	}

}
