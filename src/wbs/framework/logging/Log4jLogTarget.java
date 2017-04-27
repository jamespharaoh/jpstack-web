package wbs.framework.logging;

import static wbs.utils.collection.MapUtils.mapItemForKeyOrThrow;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("log4jLogTarget")
public
class Log4jLogTarget
	implements LogTarget {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// state

	private final
	Logger logger;

	// data

	public final static
	Map <LogSeverity, Level> severityToLog4jLevel =
		ImmutableMap.<LogSeverity, Level> builder ()

		.put (
			LogSeverity.fatal,
			Level.FATAL)

		.put (
			LogSeverity.error,
			Level.ERROR)

		.put (
			LogSeverity.warning,
			Level.WARN)

		.put (
			LogSeverity.notice,
			Level.INFO)

		.put (
			LogSeverity.trace,
			Level.DEBUG)

		.put (
			LogSeverity.debug,
			Level.DEBUG)

		.build ();

	public
	Log4jLogTarget (
			@NonNull Logger logger) {

		this.logger =
			logger;

	}

	@Override
	public
	void writeToLog (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull LogSeverity severity,
			@NonNull String message,
			@NonNull Optional <Throwable> exception) {

		logger.log (
			mapItemForKeyOrThrow (
				severityToLog4jLevel,
				severity,
				() -> new NoSuchElementException (
					stringFormat (
						"Unknown log severity: %s",
						enumName (
							severity)))),
			message,
			optionalOrNull (
				exception));

	}

	@Override
	public
	boolean debugEnabled () {

		return logger.isDebugEnabled ();

	}

}