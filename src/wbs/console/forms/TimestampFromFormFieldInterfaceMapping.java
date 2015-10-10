package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.parseTimeAfter;
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
@PrototypeComponent ("timestampFromFormFieldInterfaceMapping")
public
class TimestampFromFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Instant,String> {

	// dependencies

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	String name;

	// implementation

	@Override
	public
	Instant interfaceToGeneric (
			Container container,
			String interfaceValue,
			List<String> errors) {

		if (interfaceValue == null)
			return null;

		if (interfaceValue.isEmpty ())
			return null;

		try {

			return parseTimeAfter (
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

		return timeFormatter.instantToTimestampString (
			timeFormatter.defaultTimezone (),
			genericValue);

	}

}
