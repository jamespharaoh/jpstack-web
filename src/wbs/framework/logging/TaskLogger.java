package wbs.framework.logging;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.string.StringUtils.stringSplitNewline;
import static wbs.utils.string.StringUtils.uppercase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import wbs.framework.exception.ExceptionUtilsImplementation;
import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
public
class TaskLogger {

	// state

	private
	LogTarget logTarget;

	private
	State state;

	// constructors

	public
	TaskLogger (
			@NonNull LogTarget logTarget,
			@NonNull State state) {

		this.logTarget =
			logTarget;

		this.state =
			state;

	}

	public
	TaskLogger (
			@NonNull LogTarget logTarget) {

		this (
			logTarget,
			new State ());

	}

	public
	TaskLogger (
			@NonNull Logger logger) {

		this (
			new LoggerLogTarget (
				logger),
			new State ());

	}

	public
	TaskLogger (
			@NonNull FormatWriter formatWriter) {

		this (
			new FormatWriterLogTarget (
				formatWriter));

	}

	// accessors

	public
	long errorCount () {

		return state.errorCount;

	}

	public
	boolean errors () {

		return moreThanZero (
			state.errorCount);

	}

	public
	void firstErrorFormat (
			@NonNull Object ... arguments) {

		state.firstError =
			stringFormatArray (
				arguments);

	}

	public
	void lastErrorFormat (
			@NonNull Object ... arguments) {

		state.lastError =
			stringFormatArray (
				arguments);

	}

	// implementation

	public
	void errorFormat (
			@NonNull String ... arguments) {

		writeFirstError (
			Severity.error);

		logTarget.writeToLog (
			Severity.error,
			stringFormatArray (
				arguments),
			optionalAbsent ());

		state.errorCount ++;

	}

	public
	void errorFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		writeFirstError (
			Severity.error);

		logTarget.writeToLog (
			Severity.error,
			stringFormatArray (
				arguments),
			optionalOf (
				throwable));

		state.errorCount ++;

	}

	public
	void warningFormat (
			@NonNull String ... arguments) {

		writeFirstError (
			Severity.warning);

		logTarget.writeToLog (
			Severity.warning,
			stringFormatArray (
				arguments),
			optionalAbsent ());

		state.warningCount ++;

	}

	public
	void warningFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		writeFirstError (
			Severity.warning);

		logTarget.writeToLog (
			Severity.warning,
			stringFormatArray (
				arguments),
			optionalOf (
				throwable));

		state.warningCount ++;

	}

	public
	void debugFormat (
			@NonNull String ... arguments) {

		writeFirstError (
			Severity.debug);

		logTarget.writeToLog (
			Severity.debug,
			stringFormatArray (
				arguments),
			optionalAbsent ());

		state.debugCount ++;

	}

	public
	void debugFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		writeFirstError (
			Severity.debug);

		logTarget.writeToLog (
			Severity.debug,
			stringFormatArray (
				arguments),
			optionalOf (
				throwable));

		state.debugCount ++;

	}

	public
	RuntimeException makeException () {

		if (errors ()) {

			String message =
				stringFormat (
					"%s due to %s errors",
					state.lastError,
					state.errorCount);

			logTarget.writeToLog (
				Severity.error,
				message,
				optionalAbsent ());

			throw new LoggedErrorsException (
				this,
				message);

		} else {

			return new RuntimeException (
				"No errors");

		}

	}

	public <Type>
	Type wrap (
			@NonNull Function <TaskLogger, Type> function) {

		Type returnValue =
			function.apply (
				this);

		makeException ();

		return returnValue;

	}

	public
	TaskLogger nest (
			@NonNull Object owner,
			@NonNull String methodName,
			@NonNull Logger logger) {

		return new TaskLogger (
			logTarget.nest (
				logger));

	}

	// private implementation

	private
	void writeFirstError (
			@NonNull Severity severity) {

		switch (severity) {

		case error:

			if (

				equalToZero (
					state.errorCount)

				&& isNotNull (
					state.firstError)

			) {

				logTarget.writeToLog (
					Severity.error,
					state.firstError,
					optionalAbsent ());

			}

			break;

		case warning:

			if (

				equalToZero (
					+ state.errorCount
					+ state.warningCount)

				&& isNotNull (
					state.firstError)

			) {

				logTarget.writeToLog (
					Severity.warning,
					state.firstError,
					optionalAbsent ());

			}

			break;

		case debug:

			if (

				equalToZero (
					+ state.errorCount
					+ state.warningCount
					+ state.debugCount)

				&& isNotNull (
					state.firstError)

			) {

				logTarget.writeToLog (
					Severity.debug,
					state.firstError,
					optionalAbsent ());

			}

			break;

		}

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

	// state

	private static
	class State {

		long errorCount;
		long warningCount;
		long debugCount;

		String firstError;
		String lastError = "Aborting";

	}

	// log target

	public static
	interface LogTarget {

		void writeToLog (
				Severity severity,
				String message,
				Optional <Throwable> exception);

		LogTarget nest (
				Logger logger);

	}

	public static
	class LoggerLogTarget
		implements LogTarget {

		private final
		Logger logger;

		public
		LoggerLogTarget (
				@NonNull Logger logger) {

			this.logger =
				logger;

		}

		@Override
		public
		void writeToLog (
				@NonNull Severity severity,
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

			return new LoggerLogTarget (
				logger);

		}

	}

	public static
	class FormatWriterLogTarget
		implements LogTarget {

		private final
		FormatWriter formatWriter;

		public
		FormatWriterLogTarget (
				@NonNull FormatWriter formatWriter) {

			this.formatWriter =
				formatWriter;

		}

		@Override
		public
		void writeToLog (
				@NonNull Severity severity,
				@NonNull String message,
				@NonNull Optional <Throwable> exception) {

			formatWriter.writeLineFormat (
				"%s: %s",
				uppercase (
					severity.name ()),
				message);

			if (
				optionalIsPresent (
					exception)
			) {

				formatWriter.increaseIndent ();

				StringWriter stringWriter =
					new StringWriter ();

				ExceptionUtilsImplementation.writeThrowable (
					exception.get (),
					new PrintWriter (
						stringWriter));

				List <String> exceptionLines =
					stringSplitNewline (
						stringWriter.toString ());

				exceptionLines.forEach (
					exceptionLine ->
						formatWriter.writeLineFormat (
							"%s",
							exceptionLine));

				formatWriter.decreaseIndent ();

			}

		}

		@Override
		public
		LogTarget nest (
				@NonNull Logger logger) {

			return this;

		}

	}

	// severity

	public static
	enum Severity {
		debug,
		warning,
		error;
	}

	// data

	public final static
	Map <Severity, Level> severityToLog4jLevel =
		ImmutableMap.<Severity, Level> builder ()

		.put (
			Severity.error,
			Level.ERROR)

		.put (
			Severity.warning,
			Level.WARN)

		.put (
			Severity.debug,
			Level.DEBUG)

		.build ();

}
