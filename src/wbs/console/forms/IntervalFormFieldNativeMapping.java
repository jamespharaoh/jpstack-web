package wbs.console.forms;

import javax.inject.Inject;

import lombok.NonNull;

import org.joda.time.Interval;

import com.google.common.base.Optional;

import wbs.console.misc.ConsoleUserHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.utils.TextualInterval;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;

@PrototypeComponent ("intervalFormFieldNativeMapping")
public
class IntervalFormFieldNativeMapping<Container>
	implements FormFieldNativeMapping<Container,TextualInterval,Interval> {

	// dependencies

	@Inject
	ConsoleUserHelper formFieldPreferences;

	@Inject
	WbsConfig wbsConfig;

	// implementation

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
				formFieldPreferences.timezone (),
				nativeValue.get ()));

	}

}
