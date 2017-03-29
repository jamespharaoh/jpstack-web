package wbs.console.forms;

import static wbs.utils.time.TimeUtils.dateToInstantNullSafe;
import static wbs.utils.time.TimeUtils.instantToDateNullSafe;

import java.util.Date;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("dateFormFieldNativeMapping")
public
class DateFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, Instant, Date> {

	// implementation

	@Override
	public
	Optional<Date> genericToNative (
			@NonNull TaskLogger parentTaskLogger,
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
