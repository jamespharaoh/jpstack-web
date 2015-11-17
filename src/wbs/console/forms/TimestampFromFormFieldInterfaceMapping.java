package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.parsePartialTimestamp;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;

import com.google.common.base.Optional;

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
	Optional<Instant> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				optionalRequired (
					interfaceValue))
		) {

			return Optional.<Instant>absent ();

		} else {

			try {

				// TODO timezone should not be hardcoded

				Interval interval =
					parsePartialTimestamp (
						DateTimeZone.forID (
							"Europe/London"),
						interfaceValue.get ());

				return Optional.of (
					interval.getStart ().toInstant ());

			} catch (IllegalArgumentException exception) {

				errors.add (
					stringFormat (
						"Please enter a valid timestamp for %s",
						name ()));

				return Optional.<Instant>absent ();

			}

		}

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Instant> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return Optional.<String>absent ();

		} else {

			return Optional.of (
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					genericValue.get ()));

		}

	}

}
