package wbs.platform.background.logic;

import org.joda.time.Duration;

import wbs.framework.logging.TaskLogger;

public
interface BackgroundProcessHelper {

	String threadName ();

	Boolean debugEnabled ();

	Duration frequency ();

	boolean setBackgroundProcessStart (
			TaskLogger parentTaskLogger);

	void setBackgroundProcessStop (
			TaskLogger parentTaskLogger);

}
