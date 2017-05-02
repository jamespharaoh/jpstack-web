package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.ResultUtils.successResultAbsent;
import static wbs.utils.etc.ResultUtils.successResultPresent;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.LocalDate;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.time.TimeFormatter;

import fj.data.Either;

@PrototypeComponent ("dateFormFieldInterfaceMapping")
public
class DateFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, LocalDate, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// implementation

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <LocalDate> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToInterface");

		) {

			// handle not present

			if (
				optionalIsNotPresent (
					genericValue)
			) {
				return successResultAbsent ();
			}

			// format date

			return successResultPresent (
				timeFormatter.dateString (
					genericValue.get ()));

		}

	}

	@Override
	public
	Either <Optional <LocalDate>, String> interfaceToGeneric (
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

			// handle not present and empty

			if (

				optionalIsNotPresent (
					interfaceValue)

				|| stringIsEmpty (
					interfaceValue.get ().trim ())

			) {

				return successResultAbsent ();

			}

			// parse date

			Optional <LocalDate> genericValue =
				timeFormatter.dateStringToLocalDate (
					interfaceValue.get ());

			if (
				optionalIsNotPresent (
					genericValue)
			) {

				return errorResultFormat (
					"You must enter a valid date in yyyy-mm-dd format");

			}

			return successResult (
				genericValue);

		}

	}

}
