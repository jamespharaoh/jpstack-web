package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.time.TimeUtils.secondsToDuration;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("secondsFormFieldNativeMapping")
public
class SecondsFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, Duration, Long> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <Duration> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Long> nativeValueOptional) {

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

			Long nativeValue =
				optionalGetRequired (
					nativeValueOptional);

			return optionalOf (
				secondsToDuration (
					nativeValue));

		}

	}

	@Override
	public Optional <Long> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Duration> genericValueOptional) {

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

			Duration genericValue =
				optionalGetRequired (
					genericValueOptional);

			if (genericValue.getMillis () % 1000l != 0) {
				throw new RuntimeException ();
			}

			return optionalOf (
				genericValue.getMillis () / 1000l);

		}

	}

}
