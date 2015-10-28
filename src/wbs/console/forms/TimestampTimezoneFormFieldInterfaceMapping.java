package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;

import com.google.common.base.Optional;

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
	Optional<DateTime> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		if (interfaceValue.get ().isEmpty ())
			return null;

		try {

			return Optional.of (
				timeFormatter.timestampTimezoneToDateTime (
					interfaceValue.get ()));

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
	Optional<String> genericToInterface (
			Container container,
			Optional<DateTime> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<String>absent ();
		}

		return Optional.of (
			timeFormatter.dateTimeToTimestampTimezoneString (
				genericValue.get ()));

	}

}
