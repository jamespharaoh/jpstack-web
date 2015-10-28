package wbs.console.forms;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("timestampTimezonePairFormFieldNativeMapping")
public
class TimestampTimezonePairFormFieldNativeMapping
	implements FormFieldNativeMapping<DateTime,Pair<Instant,String>> {

	@Override
	public
	Optional<DateTime> nativeToGeneric (
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
	Optional<Pair<Instant,String>> genericToNative (
			@NonNull Optional<DateTime> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<Pair<Instant,String>>absent ();
		}

		return Optional.of (
			Pair.of (
				genericValue.get ().toInstant (),
				genericValue.get ().getZone ().getID ()));

	}

}
