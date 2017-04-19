package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.ResultUtils.successResultPresent;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.TextualInterval;

import fj.data.Either;

@PrototypeComponent ("intervalFormFieldInterfaceMapping")
public
class IntervalFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, TextualInterval, String> {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper formFieldPreferences;

	// implementation

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <TextualInterval> genericValue) {

		// allow null

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				optionalAbsent ());

		}

		// return textual part

		return successResultPresent (
			genericValue.get ().genericText ());

	}

	@Override
	public
	Either <Optional <TextualInterval>, String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		// allow null

		if (
			optionalIsNotPresent (
				interfaceValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// parse interval

		Optional <TextualInterval> optionalInterval =
			TextualInterval.parse (
				formFieldPreferences.timezone (),
				interfaceValue.get (),
				formFieldPreferences.hourOffset ());

		if (
			optionalIsNotPresent (
				optionalInterval)
		) {

			return errorResultFormat (
				"You must enter a valid time, date, or range");

		}

		return successResult (
			optionalInterval);

	}

}
