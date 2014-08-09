package wbs.platform.daemon;

import java.util.List;

import lombok.extern.log4j.Log4j;

@Log4j
public
class DaemonRunner {

	public static
	void runDaemon (
			List<String> args)
		throws InterruptedException {

		log.info ("Daemon started");

		try {

			for (;;) { Thread.sleep (1000); }

		} finally {

			log.info ("Daemon shutting down");

		}

	}

}
