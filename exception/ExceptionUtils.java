package wbs.framework.exception;

import java.io.PrintWriter;

import org.json.simple.JSONObject;

import wbs.framework.logging.TaskLogger;

public
interface ExceptionUtils {

	String throwableSummary (
			TaskLogger parentTaskLogger,
			Throwable throwable);

	String throwableDump (
			TaskLogger parentTaskLogger,
			Throwable throwable);

	JSONObject throwableDumpJson (
			TaskLogger parentTaskLogger,
			Throwable throwable);

	void writeThrowable (
			TaskLogger parentTaskLogger,
			Throwable throwable,
			PrintWriter printWriter);

}
