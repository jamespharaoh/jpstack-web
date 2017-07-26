package wbs.framework.logging;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

public
class LogContextImplementation
	implements LogContext {

	// state

	private final
	LoggingLogic loggingLogic;

	private final
	String staticContextName;

	private final
	LogTarget logTarget;

	// constructors

	public
	LogContextImplementation (
			@NonNull LoggingLogic loggingLogic,
			@NonNull String staticContextName,
			@NonNull LogTarget logTarget) {

		this.loggingLogic =
			loggingLogic;

		this.staticContextName =
			staticContextName;

		this.logTarget =
			logTarget;

	}

	// log context implementation

	@Override
	public
	OwnedTaskLogger createTaskLogger (
			@NonNull String dynamicContextName,
			@NonNull List <CharSequence> dynamicContextParameters,
			@NonNull Optional <Boolean> debugEnabled) {

		return new RealTaskLoggerImplementation (
			loggingLogic,
			optionalAbsent (),
			logTarget,
			staticContextName,
			dynamicContextName,
			dynamicContextParameters,
			debugEnabled);

	}

	@Override
	public
	OwnedTaskLogger nestTaskLogger (
			@NonNull Optional <TaskLogger> parent,
			@NonNull String dynamicContextName,
			@NonNull List <CharSequence> dynamicContextParameters,
			@NonNull Optional <Boolean> debugEnabled) {

		return new RealTaskLoggerImplementation (
			loggingLogic,
			optionalMapRequired (
				parent,
				TaskLogger::realTaskLogger),
			logTarget,
			staticContextName,
			dynamicContextName,
			dynamicContextParameters,
			debugEnabled);

	}

}