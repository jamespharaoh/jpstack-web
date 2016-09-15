package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Interval;

import wbs.console.misc.ConsoleUserHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.utils.time.TextualInterval;

@PrototypeComponent ("intervalFormFieldNativeMapping")
public
class IntervalFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, TextualInterval, Interval> {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper formFieldPreferences;

	@SingletonDependency
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	Optional <Interval> genericToNative (
			@NonNull Container container,
			@NonNull Optional <TextualInterval> genericValue) {

		// handle not present

		if (
			optionalIsNotPresent (
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
	Optional <TextualInterval> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<Interval> nativeValue) {

		// handle not present

		if (
			optionalIsNotPresent (
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
