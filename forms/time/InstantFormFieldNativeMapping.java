package wbs.console.forms.time;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.time.TimeUtils.toInstant;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Instant;

import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("instantFormFieldNativeMapping")
public
class InstantFormFieldNativeMapping <Container>
	implements ConsoleFormNativeMapping <Container, DateTime, Instant> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	// implementation

	@Override
	public
	Optional <DateTime> nativeToGeneric (
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

			Instant nativeValue =
				optionalGetRequired (
					nativeValueOptional);

			return optionalOf (
				nativeValue.toDateTime (
					consoleUserHelper.timezone (
						transaction)));

		}

	}

	@Override
	public
	Optional <Instant> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <DateTime> genericValueOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToNative");

		) {

			if (
				optionalIsNotPresent (
					genericValueOptional)
			) {
				return optionalAbsent ();
			}

			DateTime genericValue =
				optionalGetRequired (
					genericValueOptional);

			return optionalOf (
				toInstant (
					genericValue));

		}

	}

}
