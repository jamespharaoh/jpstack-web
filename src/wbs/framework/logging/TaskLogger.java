package wbs.framework.logging;

import org.apache.log4j.Logger;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class TaskLogger {

	// properties

	@Getter @Setter
	Logger logger;

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
			@NonNull Logger logger) {

		this.logger =
			logger;

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

			logger.error (
				firstError);

		}

		logger.error (
			stringFormatArray (
				arguments));

		errorCount ++;

	}

	public
	void errorFormatException (
			@NonNull Throwable throwable,
			@NonNull Object ... arguments) {

		logger.error (
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

			logger.error (
				message);

			throw new LoggedErrorsException (
				this,
				message);

		} else {

			return new RuntimeException (
				"No errors");

		}

	}

}
