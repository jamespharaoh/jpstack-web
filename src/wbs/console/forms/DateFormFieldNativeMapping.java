package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.dateToInstantNullSafe;
import static wbs.framework.utils.etc.Misc.instantToDateNullSafe;

import java.util.Date;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("dateFormFieldNativeMapping")
public
class DateFormFieldNativeMapping<Container>
	implements FormFieldNativeMapping<Container,Instant,Date> {

	// implementation

	@Override
	public
	Optional<Date> genericToNative (
			@NonNull Container container,
			@NonNull Optional<Instant> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<Date>absent ();
		}

		return Optional.of (
			instantToDateNullSafe (
				genericValue.get ()));

	}

	@Override
	public
	Optional<Instant> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<Date> nativeValue) {

		if (! nativeValue.isPresent ()) {
			return Optional.<Instant>absent ();
		}

		return Optional.of (
			dateToInstantNullSafe (
				nativeValue.get ()));

	}

}
