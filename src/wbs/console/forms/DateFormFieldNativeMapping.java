package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.Date;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("dateFormFieldNativeMapping")
public
class DateFormFieldNativeMapping
	implements FormFieldNativeMapping<Instant,Date> {

	// implementation

	@Override
	public
	Optional<Date> genericToNative (
			@NonNull Optional<Instant> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<Date>absent ();
		}

		return Optional.of (
			instantToDate (
				genericValue.get ()));

	}

	@Override
	public
	Optional<Instant> nativeToGeneric (
			@NonNull Optional<Date> nativeValue) {

		if (! nativeValue.isPresent ()) {
			return Optional.<Instant>absent ();
		}

		return Optional.of (
			dateToInstant (
				nativeValue.get ()));

	}

}
