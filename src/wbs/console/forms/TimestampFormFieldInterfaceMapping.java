package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import fj.data.Either;

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
	Either<Optional<Instant>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		if (
			notEqual (
				format,
				TimestampFormFieldSpec.Format.timestamp)
		) {

			throw new RuntimeException ();

		} else if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				optionalRequired (
					interfaceValue))

		) {

			return successResult (
				Optional.<Instant>absent ());

		} else {

			try {

				return successResult (
					Optional.of (
						timeFormatter.timestampStringToInstant (
							timeFormatter.defaultTimezone (),
							interfaceValue.get ())));

			} catch (IllegalArgumentException exception) {

				return errorResult (
					stringFormat (
						"Please enter a valid timestamp for %s",
						name ()));

			}

		}

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Instant> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.of (
					""));

		}

		switch (format) {

		case timestamp:

			return successResult (
				Optional.of (
					timeFormatter.instantToTimestampString (
						timeFormatter.defaultTimezone (),
						genericValue.get ())));

		case date:

			return successResult (
				Optional.of (
					timeFormatter.instantToDateStringShort (
						timeFormatter.defaultTimezone (),
						genericValue.get ())));

		case time:

			return successResult (
				Optional.of (
					timeFormatter.instantToTimeString (
						timeFormatter.defaultTimezone (),
						genericValue.get ())));

		default:

			throw new RuntimeException ();

		}

	}

}
