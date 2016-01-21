package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isNotPresent;
import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TextualInterval;

@PrototypeComponent ("intervalFormFieldNativeMapping")
public
class IntervalFormFieldNativeMapping<Container>
	implements FormFieldNativeMapping<Container,TextualInterval,Interval> {

	@Override
	public
	Optional<Interval> genericToNative (
			@NonNull Container container,
			@NonNull Optional<TextualInterval> genericValue) {

		// handle not present

		if (
			isNotPresent (
				genericValue)
		) {
			return Optional.absent ();
		}

		// return interval

		return Optional.of (
			genericValue.get ().value ());

	}

	@Override
	public
	Optional<TextualInterval> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<Interval> nativeValue) {

		// handle not present

		if (
			isNotPresent (
				nativeValue)
		) {
			return Optional.absent ();
		}

		// return textual interval

		return Optional.of (
			TextualInterval.forInterval (
				DateTimeZone.forID (
					"Europe/London"),
				nativeValue.get ()));

	}

}
