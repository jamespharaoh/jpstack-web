package wbs.console.forms;

import static wbs.utils.etc.Misc.errorResult;
import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.utils.time.TextualInterval;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("timestampToFormFieldInterfaceMapping")
public
class TimestampToFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, Instant, String> {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper formFieldPreferences;

	// properties

	@Getter @Setter
	String name;

	// implementation

	@Override
	public
	Either<Optional<Instant>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		if (

			optionalIsNotPresent (
				interfaceValue)

			|| stringIsEmpty (
				optionalGetRequired (
					interfaceValue))

		) {

			return successResult (
				Optional.<Instant>absent ());

		}

		try {

			TextualInterval interval =
				TextualInterval.parseRequired (
					formFieldPreferences.timezone (),
					interfaceValue.get (),
					formFieldPreferences.hourOffset ());

			return successResult (
				Optional.of (
					interval.value ().getEnd ().toInstant ()));

		} catch (IllegalArgumentException exception) {

			return errorResult (
				stringFormat (
					"Please enter a valid timestamp for %s",
					name ()));

		}

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Instant> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.<String>absent ());

		}

		return successResult (
			Optional.of (
				formFieldPreferences.timestampWithTimezoneString (
					genericValue.get ())));

	}

}
