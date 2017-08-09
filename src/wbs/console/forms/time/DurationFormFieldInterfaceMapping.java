package wbs.console.forms.time;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResultAbsent;
import static wbs.utils.etc.ResultUtils.successResultPresent;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.console.forms.types.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.time.duration.DurationFormatter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("durationFormFieldInterfaceMapping")
public
class DurationFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, Duration, String> {

	// singleton dependencies

	@SingletonDependency
	DurationFormatter durationFormatter;

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String label;

	@Getter @Setter
	Format format;

	// implementation

	@Override
	public
	Either <Optional <Duration>, String> interfaceToGeneric (
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

				optionalIsNotPresent (
					interfaceValue)

				|| stringIsEmpty (
					optionalGetRequired (
						interfaceValue))

			) {

				return successResultAbsent ();

			} else {

				Optional <Duration> genericValue =
					durationFormatter.stringToDuration (
						optionalGetRequired (
							interfaceValue));

				if (
					optionalIsNotPresent (
						genericValue)
				) {

					return errorResultFormat (
						"Please enter a valid interval for '%s'",
						label);

				}

				return successResultPresent (
					genericValue.get ());

			}

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Duration> genericValue) {

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

			} else {

				switch (format) {

				case textual:

					return successResultPresent (
						durationFormatter.durationStringExact (
							optionalGetRequired (
								genericValue)));

				case numeric:

					return successResultPresent (
						durationFormatter.durationStringNumeric (
							optionalGetRequired (
								genericValue)));

				default:

					throw new RuntimeException ();

				}

			}

		}

	}

	public static
	enum Format {
		textual,
		numeric;
	}

}
