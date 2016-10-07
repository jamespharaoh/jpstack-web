package wbs.framework.logging;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.string.StringUtils.stringSplitNewline;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.log4j.Logger;

import wbs.framework.exception.ExceptionUtilsImplementation;
import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
public
class TaskLogger {

	// properties

	@Getter @Setter
	LogTarget logTarget;

	@Getter @Setter
	long errorCount;

	@Getter @Setter
	String firstError;

	@Getter @Setter
	String lastError =
		"Aborting";

	// constructors

	public
	TaskLogger (
			@NonNull LogTarget logTarget) {

		this.logTarget =
			logTarget;

	}

	public
	TaskLogger (
			@NonNull Logger logger) {

		this (
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
	boolean errors () {

		return moreThanZero (
			errorCount);

	}

	public
	void firstErrorFormat (
			@NonNull Object ... arguments) {

		firstError =
			stringFormatArray (
				arguments);

	}

	public
	void lastErrorFormat (
			@NonNull Object ... arguments) {

		lastError =
			stringFormatArray (
				arguments);

	}

	// implementation

	public
	void errorFormat (
			@NonNull Object ... arguments) {

		if (

			equalToZero (
				errorCount)

			&& isNotNull (
				firstError)

		) {

			logTarget.error (
				firstError);

		}

		logTarget.error (
			stringFormatArray (
				arguments));

		errorCount ++;

	}

	public
	void errorFormatException (
			@NonNull Throwable throwable,
			@NonNull Object ... arguments) {

		logTarget.error (
			stringFormatArray (
				arguments),
			throwable);

		errorCount ++;

	}

	public
	RuntimeException makeException () {

		if (errors ()) {

			String message =
				stringFormat (
					"%s due to %s errors",
					lastError,
					errorCount ());

			logTarget.error (
				message);

			throw new LoggedErrorsException (
				this,
				message);

		} else {

			return new RuntimeException (
				"No errors");

		}

	}

	public static
	interface LogTarget {

		void error (
				String message);

		void error (
				String message,
				Throwable exception);

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
		void error (
				@NonNull String message) {

			logger.error (
				message);

		}

		@Override
		public
		void error (
				@NonNull String message,
				@NonNull Throwable exception) {

			logger.error (
				message,
				exception);

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
		void error (
				@NonNull String message) {

			formatWriter.writeLineFormat (
				"ERROR: %s",
				message);
			
		}

		@Override
		public void error (
				@NonNull String message,
				@NonNull Throwable exception) {

			formatWriter.writeLineFormat (
				"ERROR: %s",
				message);

			formatWriter.increaseIndent ();

			StringWriter stringWriter =
				new StringWriter ();

			ExceptionUtilsImplementation.writeThrowable (
				exception,
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

}
