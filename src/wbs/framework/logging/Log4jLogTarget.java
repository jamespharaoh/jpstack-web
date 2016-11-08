package wbs.framework.logging;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.Map;
import java.util.function.Function;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public
class Log4jLogTarget
	implements LogTarget {

	private final
	Logger logger;
	// data

	public final static
	Map <LogSeverity, Level> severityToLog4jLevel =
		ImmutableMap.<LogSeverity, Level> builder ()

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
			@NonNull LogSeverity severity,
			@NonNull String message,
			@NonNull Optional <Throwable> exception) {

		logger.log (
			mapItemForKeyRequired (
				severityToLog4jLevel,
				severity),
			message,
			optionalOrNull (
				exception));

	}

	@Override
	public
	LogTarget nest (
			@NonNull Logger logger) {

		return new Log4jLogTarget (
			logger);

	}

	// utilities

	public static <Type>
	Type wrap (
			@NonNull Logger logger,
			@NonNull Function <TaskLogger, Type> function) {

		TaskLogger taskLogger =
			new TaskLogger (
				logger);

		return taskLogger.wrap (
			function);

	}

}