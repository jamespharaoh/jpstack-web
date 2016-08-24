package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.misc.ConsoleUserHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TextualInterval;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;

@PrototypeComponent ("intervalFormFieldInterfaceMapping")
public
class IntervalFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,TextualInterval,String> {

	// dependencies

	@Inject
	ConsoleUserHelper formFieldPreferences;

	// implementation

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<TextualInterval> genericValue) {

		// allow null

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// return textual part

		return successResult (
			Optional.of (
				genericValue.get ().genericText ()));

	}

	@Override
	public
	Either<Optional<TextualInterval>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		// allow null

		if (
			optionalIsNotPresent (
				interfaceValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// parse interval

		Optional<TextualInterval> optionalInterval =
			TextualInterval.parse (
				formFieldPreferences.timezone (),
				interfaceValue.get (),
				formFieldPreferences.hourOffset ());

		if (
			optionalIsNotPresent (
				optionalInterval)
		) {

			return errorResult (
				stringFormat (
					"You must enter a valid time, date, or range"));

		}

		return successResult (
			optionalInterval);

	}

}
