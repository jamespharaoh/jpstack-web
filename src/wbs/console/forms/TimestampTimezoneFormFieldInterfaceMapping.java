package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.utils.TimeFormatter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("timestampTimezoneFormFieldInterfaceMapping")
public
class TimestampTimezoneFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, DateTime, String> {

	// singleton dependencies

	@SingletonDependency
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	String name;

	// implementation

	@Override
	public
	Either<Optional<DateTime>,String> interfaceToGeneric (
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
				Optional.<DateTime>absent ());

		}

		try {

			return successResult (
				Optional.of (
					timeFormatter.timestampTimezoneToDateTime (
						optionalGetRequired (
							interfaceValue))));

		} catch (IllegalArgumentException exception) {

			return errorResult (
				stringFormat (
					"Please enter a valid timestamp with timezone for %s",
					name ()));

		}

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<DateTime> genericValue) {

		if (! genericValue.isPresent ()) {

			return successResult (
				Optional.<String>absent ());

		}

		return successResult (
			Optional.of (
				timeFormatter.timestampTimezoneString (
					genericValue.get ())));

	}

}
