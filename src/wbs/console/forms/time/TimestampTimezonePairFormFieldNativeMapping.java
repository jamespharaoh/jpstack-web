package wbs.console.forms.time;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import wbs.console.forms.types.FormFieldNativeMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("timestampTimezonePairFormFieldNativeMapping")
public
class TimestampTimezonePairFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <
		Container,
		DateTime,
		Pair <Instant, String>
	> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <DateTime> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Pair <Instant, String>> nativeValue) {

		if (
			optionalIsNotPresent (
				nativeValue)
		) {
			return optionalAbsent ();
		}

		DateTimeZone timeZone =
			DateTimeZone.forID (
				nativeValue.get ().getRight ());

		return optionalOf (
			new DateTime (
				nativeValue.get ().getLeft (),
				timeZone));

	}

	@Override
	public
	Optional <Pair <Instant, String>> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <DateTime> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToNative");

		) {

			if (! genericValue.isPresent ()) {
				return optionalAbsent ();
			}

			return optionalOf (
				Pair.of (
					genericValue.get ().toInstant (),
					genericValue.get ().getZone ().getID ()));

		}

	}

}
