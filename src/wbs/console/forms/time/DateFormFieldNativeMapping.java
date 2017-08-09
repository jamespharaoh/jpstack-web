package wbs.console.forms.time;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.time.TimeUtils.dateToInstantNullSafe;
import static wbs.utils.time.TimeUtils.instantToDateNullSafe;

import java.util.Date;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.forms.types.ConsoleFormNativeMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@PrototypeComponent ("dateFormFieldNativeMapping")
public
class DateFormFieldNativeMapping <Container>
	implements ConsoleFormNativeMapping <Container, Instant, Date> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <Date> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Instant> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToNative");

		) {

			if (
				optionalIsNotPresent (
					genericValue)
			) {
				return optionalAbsent ();
			}

			return optionalOf (
				instantToDateNullSafe (
					genericValue.get ()));

		}

	}

	@Override
	public
	Optional <Instant> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Date> nativeValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"nativeToGeneric");

		) {

			if (
				optionalIsNotPresent (
					nativeValue)
			) {
				return optionalAbsent ();
			}

			return optionalOf (
				dateToInstantNullSafe (
					nativeValue.get ()));

		}

	}

}
