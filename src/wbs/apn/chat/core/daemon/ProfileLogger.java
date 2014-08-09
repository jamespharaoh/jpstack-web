package wbs.apn.chat.core.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import org.apache.log4j.Logger;

// TODO where does this belong?

public
class ProfileLogger {

	Logger logger;
	long startTime;
	long lapTime;
	String name;
	String lapName;

	public
	ProfileLogger (
			Logger logger,
			String name) {

		this.logger = logger;
		this.name = name;

		logger.debug (
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

		logger.debug (
			stringFormat (
				"-- %s starting",
				lapName));

	}

	void endLap (
			long now) {

		if (lapName == null)
			return;

		logger.debug (
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

		logger.debug (
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