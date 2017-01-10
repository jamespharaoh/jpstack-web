package wbs.framework.logging;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.TypeUtils.classNameSimple;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.log4j.Logger;

public
class Log4jLogContext
	implements LogContext {

	private
	Logger logger;

	private
	Log4jLogTarget logTarget;

	private
	String staticContext;

	public
	Log4jLogContext (
			@NonNull Logger logger,
			@NonNull String staticContext) {

		this.logger =
			logger;

		this.staticContext =
			staticContext;

		logTarget =
			new Log4jLogTarget (
				this.logger);

	}

	@Override
	public
	TaskLogger createTaskLogger (
			@NonNull String dynamicContext,
			@NonNull Optional <Boolean> debugEnabled) {

		return new TaskLogger (
			optionalAbsent (),
			logTarget,
			staticContext,
			dynamicContext,
			debugEnabled);

	}

	@Override
	public
	TaskLogger nestTaskLogger (
			@NonNull Optional <TaskLogger> parent,
			@NonNull String dynamicContext,
			@NonNull Optional <Boolean> debugEnabled) {

		return new TaskLogger (
			parent,
			logTarget,
			staticContext,
			dynamicContext,
			debugEnabled);

	}

	public static
	LogContext forClass (
			@NonNull Class <?> contextClass) {

		return new Log4jLogContext (
			Logger.getLogger (
				contextClass),
			classNameSimple (
				contextClass));

	}

}