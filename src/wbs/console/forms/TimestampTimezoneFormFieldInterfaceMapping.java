package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.utils.time.TimeFormatter;

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
				optionalAbsent ());

		}

		try {

			return successResult (
				optionalOf (
					timeFormatter.timestampTimezoneToDateTime (
						optionalGetRequired (
							interfaceValue))));

		} catch (IllegalArgumentException exception) {

			return errorResultFormat (
				"Please enter a valid timestamp with timezone for %s",
				name ());

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
