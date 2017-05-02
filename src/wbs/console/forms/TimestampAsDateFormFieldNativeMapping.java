package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("timestampAsDateFormFieldNativeMapping")
public
class TimestampAsDateFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, LocalDate, Instant> {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <LocalDate> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Instant> nativeValueOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"nativeToGeneric");

		) {

			if (
				optionalIsNotPresent (
					nativeValueOptional)
			) {
				return optionalAbsent ();
			}

			Instant nativeInstant =
				optionalGetRequired (
					nativeValueOptional);

			DateTime nativeDateTime =
				nativeInstant.toDateTime (
					consoleUserHelper.timezone (
						transaction));

			return optionalOf (
				nativeDateTime.toLocalDate ());

		}

	}

	@Override
	public
	Optional <Instant> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <LocalDate> genericValue) {

		throw new UnsupportedOperationException ();

	}

}
