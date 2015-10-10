package wbs.console.forms;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("timestampTimezonePairFormFieldNativeMapping")
public
class TimestampTimezonePairFormFieldNativeMapping
	implements FormFieldNativeMapping<DateTime,Pair<Instant,String>> {

	@Override
	public
	DateTime nativeToGeneric (
			Pair<Instant,String> nativeValue) {

		if (nativeValue == null)
			return null;

		DateTimeZone timeZone =
			DateTimeZone.forID (
				nativeValue.getRight ());

		return new DateTime (
			nativeValue.getLeft (),
			timeZone);

	}

	@Override
	public
	Pair<Instant,String> genericToNative (
			DateTime genericValue) {

		if (genericValue == null)
			return null;

		return Pair.of (
			genericValue.toInstant (),
			genericValue.getZone ().getID ());

	}

}
