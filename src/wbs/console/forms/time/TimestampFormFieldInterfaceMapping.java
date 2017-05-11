package wbs.console.forms.time;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.ResultUtils.successResultAbsent;
import static wbs.utils.etc.ResultUtils.successResultPresent;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("timestampFormFieldInterfaceMapping")
public
class TimestampFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, Instant, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleUserHelper preferences;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	TimestampFormFieldSpec.Format format;

	// implementation

	@Override
	public
	Either <Optional <Instant>, String> interfaceToGeneric (
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

			if (
				enumNotEqualSafe (
					format,
					TimestampFormFieldSpec.Format.timestamp)
			) {

				throw new RuntimeException ();

			} else if (

				optionalIsNotPresent (
					interfaceValue)

				|| stringIsEmpty (
					optionalGetRequired (
						interfaceValue))

			) {

				return successResult (
					optionalAbsent ());

			} else {

				try {

					return successResult (
						optionalOf (
							preferences.timestampStringToInstant (
								transaction,
								interfaceValue.get ())));

				} catch (IllegalArgumentException exception) {

					return errorResultFormat (
						"Please enter a valid timestamp for %s",
						name ());

				}

			}

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Instant> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToInterface");

		) {

			if (
				optionalIsNotPresent (
					genericValue)
			) {

				return successResultAbsent ();

			}

			switch (format) {

			case timestamp:

				return successResultPresent (
					preferences.timestampWithTimezoneString (
						transaction,
						genericValue.get ()));

			case date:

				return successResultPresent (
					preferences.dateStringShort (
						transaction,
						genericValue.get ()));

			case time:

				return successResultPresent (
					preferences.timeString (
						transaction,
						genericValue.get ()));

			default:

				throw new RuntimeException ();

			}

		}

	}

}
