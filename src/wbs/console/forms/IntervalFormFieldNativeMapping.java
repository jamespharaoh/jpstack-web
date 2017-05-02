package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Interval;

import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.time.TextualInterval;

@PrototypeComponent ("intervalFormFieldNativeMapping")
public
class IntervalFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, TextualInterval, Interval> {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper formFieldPreferences;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	Optional <Interval> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <TextualInterval> genericValue) {

		// handle not present

		if (
			optionalIsNotPresent (
				genericValue)
		) {
			return optionalAbsent ();
		}

		// return interval

		return optionalOf (
			genericValue.get ().value ());

	}

	@Override
	public
	Optional <TextualInterval> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Interval> nativeValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"nativeToGeneric");

		) {

			// handle not present

			if (
				optionalIsNotPresent (
					nativeValue)
			) {
				return optionalAbsent ();
			}

			// return textual interval

			return optionalOf (
				TextualInterval.forInterval (
					formFieldPreferences.timezone (
						transaction),
					nativeValue.get ()));

		}

	}

}
