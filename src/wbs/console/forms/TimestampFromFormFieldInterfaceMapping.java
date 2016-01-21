package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
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
import wbs.framework.utils.TextualInterval;

@Accessors (fluent = true)
@PrototypeComponent ("timestampFromFormFieldInterfaceMapping")
public
class TimestampFromFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Instant,String> {

	// dependencies

	@Inject
	FormFieldPreferencesProvider formFieldPreferences;

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	String name;

	// implementation

	@Override
	public
	Either<Optional<Instant>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		if (

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

				// TODO timezone should not be hardcoded

				TextualInterval interval =
					TextualInterval.parseRequired (
						formFieldPreferences.timeZone (),
						interfaceValue.get (),
						formFieldPreferences.hourOffset ());

				return successResult (
					Optional.of (
						interval.value ().getStart ().toInstant ()));

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
				Optional.<String>absent ());

		} else {

			return successResult (
				Optional.of (
					timeFormatter.instantToTimestampString (
						timeFormatter.defaultTimezone (),
						genericValue.get ())));

		}

	}

}
