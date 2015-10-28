package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.parsePartialTimestamp;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.Interval;

import com.google.common.base.Optional;

import wbs.console.misc.TimeFormatter;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("timestampToFormFieldInterfaceMapping")
public
class TimestampToFormFieldInterfaceMapping<Container>
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

		if (! interfaceValue.isPresent ()) {
			return Optional.<Instant>absent ();
		}

		if (interfaceValue.get ().isEmpty ()) {
			return Optional.<Instant>absent ();
		}

		try {

			Interval interval =
				parsePartialTimestamp (
					interfaceValue.get ());

			return Optional.of (
				interval.getEnd ().toInstant ());

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
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Instant> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<String>absent ();
		}

		return Optional.of (
			timeFormatter.instantToTimestampString (
				timeFormatter.defaultTimezone (),
				genericValue.get ()));

	}

}
