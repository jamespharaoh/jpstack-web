package wbs.framework.logging;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalDo;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.log4j.Logger;

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
			new Log4jLogTarget (
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
	void logFormat (
			@NonNull LogSeverity severity,
			@NonNull String ... arguments) {

		switch (severity) {

		case error:

			errorFormat (
				arguments);

			break;

		case warning:

			warningFormat (
				arguments);

			break;

		case notice:

			noticeFormat (
				arguments);

			break;

		case debug:

			debugFormat (
				arguments);

			break;

		}

	}

	public
	void errorFormat (
			@NonNull String ... arguments) {

		writeFirstError (
			LogSeverity.error);

		logTarget.writeToLog (
			LogSeverity.error,
			stringFormatArray (
				arguments),
			optionalAbsent ());

		increaseErrorCount ();

	}

	public
	void errorFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		writeFirstError (
			LogSeverity.error);

		logTarget.writeToLog (
			LogSeverity.error,
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
			LogSeverity.warning);

		logTarget.writeToLog (
			LogSeverity.warning,
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
			LogSeverity.warning);

		logTarget.writeToLog (
			LogSeverity.warning,
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
			LogSeverity.notice);

		logTarget.writeToLog (
			LogSeverity.notice,
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
			LogSeverity.notice);

		logTarget.writeToLog (
			LogSeverity.notice,
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
			LogSeverity.debug);

		logTarget.writeToLog (
			LogSeverity.debug,
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
			LogSeverity.debug);

		logTarget.writeToLog (
			LogSeverity.debug,
			stringFormatArray (
				arguments),
			optionalOf (
				throwable));

		increaseDebugCount ();

	}

	public
	RuntimeException makeException () {

		if (errors ()) {

			String message =
				stringFormat (
					"%s due to %s errors",
					lastError,
					integerToDecimalString (
						errorCount));

			logTarget.writeToLog (
				LogSeverity.error,
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
			@NonNull LogSeverity severity) {

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
					LogSeverity.error,
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
					LogSeverity.warning,
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
					LogSeverity.notice,
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
					LogSeverity.debug,
					firstError,
					optionalAbsent ());

			}

			break;

		}

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

}
