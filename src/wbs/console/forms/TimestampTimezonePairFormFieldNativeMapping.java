package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("timestampTimezonePairFormFieldNativeMapping")
public
class TimestampTimezonePairFormFieldNativeMapping<Container>
	implements FormFieldNativeMapping<Container,DateTime,Pair<Instant,String>> {

	@Override
	public
	Optional<DateTime> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<Pair<Instant,String>> nativeValue) {

		if (! nativeValue.isPresent ()) {
			return Optional.<DateTime>absent ();
		}

		DateTimeZone timeZone =
			DateTimeZone.forID (
				nativeValue.get ().getRight ());

		return Optional.of (
			new DateTime (
				nativeValue.get ().getLeft (),
				timeZone));

	}

	@Override
	public
	Optional <Pair <Instant, String>> genericToNative (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <DateTime> genericValue) {

		if (! genericValue.isPresent ()) {
			return optionalAbsent ();
		}

		return Optional.of (
			Pair.of (
				genericValue.get ().toInstant (),
				genericValue.get ().getZone ().getID ()));

	}

}
