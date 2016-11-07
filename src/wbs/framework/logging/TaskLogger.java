package wbs.framework.logging;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalDo;
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
	Optional <TaskLogger> parent;

	private
	LogTarget logTarget;

	long errorCount;
	long warningCount;
	long noticeCount;
	long debugCount;

	String firstError;
	String lastError = "Aborting";

	// constructors

	public
	TaskLogger (
			@NonNull Optional <TaskLogger> parent,
			@NonNull LogTarget logTarget) {

		this.parent =
			parent;

		this.logTarget =
			logTarget;

	}

	public
	TaskLogger (
			@NonNull LogTarget logTarget) {

		this (
			optionalAbsent (),
			logTarget);

	}

	public
	TaskLogger (
			@NonNull Logger logger) {

		this (
			optionalAbsent (),
			new LoggerLogTarget (
				logger));

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

		return errorCount;

	}

	public
	boolean errors () {

		return moreThanZero (
			errorCount);

	}

	public
	void firstErrorFormat (
			@NonNull String ... arguments) {

		firstError =
			stringFormatArray (
				arguments);

	}

	public
	void lastErrorFormat (
			@NonNull String ... arguments) {

		lastError =
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

		increaseErrorCount ();

	}

	private
	void increaseErrorCount () {

		errorCount ++;

		optionalDo (
			parent,
			TaskLogger::increaseErrorCount);

	}

	private
	void increaseWarningCount () {

		warningCount ++;

		optionalDo (
			parent,
			TaskLogger::increaseWarningCount);

	}

	private
	void increaseNoticeCount () {

		noticeCount ++;

		optionalDo (
			parent,
			TaskLogger::increaseNoticeCount);

	}

	private
	void increaseDebugCount () {

		debugCount ++;

		optionalDo (
			parent,
			TaskLogger::increaseDebugCount);

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

		increaseErrorCount ();

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

		increaseWarningCount ();

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

		increaseWarningCount ();

	}

	public
	void noticeFormat (
			@NonNull String ... arguments) {

		writeFirstError (
			Severity.notice);

		logTarget.writeToLog (
			Severity.notice,
			stringFormatArray (
				arguments),
			optionalAbsent ());

		increaseNoticeCount ();

	}

	public
	void noticeFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		writeFirstError (
			Severity.notice);

		logTarget.writeToLog (
			Severity.notice,
			stringFormatArray (
				arguments),
			optionalOf (
				throwable));

		increaseNoticeCount ();

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

		increaseDebugCount ();

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

		increaseDebugCount ();

	}

	public
	RuntimeException makeException () {

debugFormat (
	"MAKE EXCEPTION %s",
	integerToDecimalString (
		errorCount ()));

		if (errors ()) {

			String message =
				stringFormat (
					"%s due to %s errors",
					lastError,
					integerToDecimalString (
						errorCount));

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

		if (
			optionalIsPresent (
				parent)
		) {

			parent.get ().writeFirstError (
				severity);

			return;

		}

		switch (severity) {

		case error:

			if (

				equalToZero (
					errorCount)

				&& isNotNull (
					firstError)

			) {

				logTarget.writeToLog (
					Severity.error,
					firstError,
					optionalAbsent ());

			}

			break;

		case warning:

			if (

				equalToZero (
					+ errorCount
					+ warningCount)

				&& isNotNull (
					firstError)

			) {

				logTarget.writeToLog (
					Severity.warning,
					firstError,
					optionalAbsent ());

			}

			break;

		case notice:

			if (

				equalToZero (
					+ errorCount
					+ warningCount
					+ noticeCount)

				&& isNotNull (
					firstError)

			) {

				logTarget.writeToLog (
					Severity.notice,
					firstError,
					optionalAbsent ());

			}

			break;

		case debug:

			if (

				equalToZero (
					+ errorCount
					+ warningCount
					+ noticeCount
					+ debugCount)

				&& isNotNull (
					firstError)

			) {

				logTarget.writeToLog (
					Severity.debug,
					firstError,
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
		notice,
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
			Severity.notice,
			Level.INFO)

		.put (
			Severity.debug,
			Level.DEBUG)

		.build ();

}
