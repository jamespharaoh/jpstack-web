package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.Misc.stringFormat;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public
class ProfileLogger {

	Logger logger;
	Level logLevel;

	long startTime;
	long lapTime;

	String name;
	String lapName;

	public
	ProfileLogger (
			Logger logger,
			Level logLevel,
			String name) {

		this.logger = logger;
		this.logLevel = logLevel;
		this.name = name;

		logger.log (
			logLevel,
			stringFormat (
				"---------- %s starting",
				name));

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

		logger.log (
			logLevel,
			stringFormat (
				"-- %s starting",
				lapName));

	}

	void endLap (
			long now) {

		if (lapName == null)
			return;

		logger.log (
			logLevel,
			stringFormat (
				"-- %s complete %dms",
				lapName,
				now - lapTime));

	}

	public
	void end () {

		long now =
			System.currentTimeMillis ();

		endLap (now);

		logger.log (
			logLevel,
			stringFormat (
				"---------- %s complete %dms",
				name,
				now - startTime));

	}

	public
	void error (Exception exception) {

		long now =
			System.currentTimeMillis ();

		endLap (now);

		logger.error (
			stringFormat (
				"---------- %s aborted %dms",
				name,
				now - startTime),
			exception);

	}

}