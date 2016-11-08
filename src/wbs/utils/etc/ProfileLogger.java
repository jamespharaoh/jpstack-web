package wbs.utils.etc;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.logging.LogSeverity;
import wbs.framework.logging.TaskLogger;

public
class ProfileLogger {

	TaskLogger taskLogger;
	LogSeverity severity;

	long startTime;
	long lapTime;

	String name;
	String lapName;

	public
	ProfileLogger (
			@NonNull TaskLogger taskLogger,
			@NonNull LogSeverity severity,
			@NonNull String name) {

		this.taskLogger =
			taskLogger;

		this.severity =
			severity;

		this.name =
			name;

		taskLogger.logFormat (
			severity,
			"---------- %s starting",
			name);

		startTime =
			System.currentTimeMillis ();

	}

	public
	void lap (
			String nextLapName) {

		long now =
			System.currentTimeMillis ();

		endLap (now);

		lapName =
			stringFormat (
				"%s %s",
				name,
				nextLapName);

		lapTime = now;

		taskLogger.logFormat (
			severity,
			"-- %s starting",
			lapName);

	}

	void endLap (
			long now) {

		if (lapName == null)
			return;

		taskLogger.logFormat (
			severity,
			"-- %s complete %dms",
			lapName,
			integerToDecimalString (
				now - lapTime));

	}

	public
	void end () {

		long now =
			System.currentTimeMillis ();

		endLap (now);

		taskLogger.logFormat (
			severity,
			"---------- %s complete %dms",
			name,
			integerToDecimalString (
				now - startTime));

	}

	public
	void error (Exception exception) {

		long now =
			System.currentTimeMillis ();

		endLap (now);

		taskLogger.errorFormatException (
			exception,
			"---------- %s aborted %dms",
			name,
			integerToDecimalString (
				now - startTime));

	}

}