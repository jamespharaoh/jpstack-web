package wbs.framework.component.manager;

import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.function.Consumer;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
interface ComponentProvider <Type> {

	Type provide (
			TaskLogger parentTaskLogger);

	default
	Type provideUninitialised (
			@NonNull TaskLogger parentTaskLogger) {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.provideUninitialised (taskLogger)",
				classNameFull (
					getClass ())));

	}

	default
	void initialise (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Type component) {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.initialise (taskLogger, component)",
				classNameFull (
					getClass ())));

	}

	default
	Type provide (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Consumer <? super Type> propertyIniitialiser) {

		Type component =
			provideUninitialised (
				parentTaskLogger);

		propertyIniitialiser.accept (
			component);

		initialise (
			parentTaskLogger,
			component);

		return component;

	}

}
