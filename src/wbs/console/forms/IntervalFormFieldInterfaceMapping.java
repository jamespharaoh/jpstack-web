package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TextualInterval;

@PrototypeComponent ("intervalFormFieldInterfaceMapping")
public
class IntervalFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,TextualInterval,String> {

	// dependencies

	@Inject
	FormFieldPreferencesProvider formFieldPreferences;

	// implementation

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<TextualInterval> genericValue) {

		// allow null

		if (
			isNotPresent (
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
			isNotPresent (
				interfaceValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// parse interval

		Optional<TextualInterval> optionalInterval =
			TextualInterval.parse (
				formFieldPreferences.timeZone (),
				interfaceValue.get (),
				formFieldPreferences.hourOffset ());

		if (
			isNotPresent (
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
