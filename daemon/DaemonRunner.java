package wbs.platform.daemon;

import java.util.List;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.logging.TaskLogger;

import info.faljse.SDNotify.SDNotify;

@Log4j
public
class DaemonRunner {

	public static
	void runDaemon (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments)
		throws InterruptedException {

		log.info ("Daemon started");

		SDNotify.sendNotify ();

		try {

			for (;;) { Thread.sleep (1000); }

		} finally {

			log.info ("Daemon shutting down");

		}

	}

}
