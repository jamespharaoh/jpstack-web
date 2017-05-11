package wbs.console.forms.time;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.ResultUtils.successResultPresent;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.time.TextualInterval;

import fj.data.Either;

@PrototypeComponent ("intervalFormFieldInterfaceMapping")
public
class IntervalFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, TextualInterval, String> {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper formFieldPreferences;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <TextualInterval> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToInterface");

		) {

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

	}

	@Override
	public
	Either <Optional <TextualInterval>, String> interfaceToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"interfaceToGeneric");

		) {

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
					formFieldPreferences.timezone (
						transaction),
					interfaceValue.get (),
					formFieldPreferences.hourOffset (
						transaction));

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

}
