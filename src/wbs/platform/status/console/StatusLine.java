package wbs.platform.status.console;

import java.util.concurrent.Future;

import wbs.console.part.PagePart;

import wbs.framework.logging.TaskLogger;

public
interface StatusLine {

	String getName ();

	PagePart get (
			TaskLogger parentTaskLogger);

	Future <String> getUpdateScript (
			TaskLogger parentTaskLogger);

}
