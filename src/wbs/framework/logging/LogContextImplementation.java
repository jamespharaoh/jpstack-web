package wbs.framework.logging;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;

import com.google.common.base.Optional;

import lombok.NonNull;

public
class LogContextImplementation
	implements LogContext {

	private
	CharSequence staticContext;

	private
	LogTarget logTarget;

	public
	LogContextImplementation (
			@NonNull CharSequence staticContext,
			@NonNull LogTarget logTarget) {

		this.staticContext =
			staticContext;

		this.logTarget =
			logTarget;

	}

	@Override
	public
	OwnedTaskLogger createTaskLogger (
			@NonNull CharSequence dynamicContext,
			@NonNull Optional <Boolean> debugEnabled) {

		return new TaskLoggerImplementation (
			optionalAbsent (),
			logTarget,
			staticContext,
			dynamicContext,
			debugEnabled);

	}

	@Override
	public
	OwnedTaskLogger nestTaskLogger (
			@NonNull Optional <TaskLogger> parent,
			@NonNull CharSequence dynamicContext,
			@NonNull Optional <Boolean> debugEnabled) {

		return new TaskLoggerImplementation (
			optionalMapRequired (
				parent,
				TaskLogger::taskLoggerImplementation),
			logTarget,
			staticContext,
			dynamicContext,
			debugEnabled);

	}

}