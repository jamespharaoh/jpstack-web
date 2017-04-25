package wbs.platform.status.console;

import java.util.concurrent.Future;

import com.google.gson.JsonObject;

import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.logging.TaskLogger;

public
interface StatusLine {

	String typeName ();

	PagePart createPagePart (
			TaskLogger parentTaskLogger);

	Future <JsonObject> getUpdateData (
			TaskLogger parentTaskLogger,
			UserPrivChecker privChecker);

}
